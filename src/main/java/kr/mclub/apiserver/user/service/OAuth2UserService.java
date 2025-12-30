package kr.mclub.apiserver.user.service;

import kr.mclub.apiserver.shared.exception.BusinessException;
import kr.mclub.apiserver.shared.exception.ErrorCode;
import kr.mclub.apiserver.shared.security.JwtTokenProvider;
import kr.mclub.apiserver.user.domain.*;
import kr.mclub.apiserver.user.event.UserEventPublisher;
import kr.mclub.apiserver.user.repository.OAuthAccountRepository;
import kr.mclub.apiserver.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * OAuth2 사용자 서비스
 * OAuth2 user service for social login
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OAuth2UserService {

    private final UserRepository userRepository;
    private final OAuthAccountRepository oAuthAccountRepository;
    private final UserGradeService userGradeService;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserEventPublisher eventPublisher;

    /**
     * OAuth 로그인 처리
     * Process OAuth login
     */
    @Transactional
    public LoginResult processOAuthLogin(OAuthProvider provider, String providerId,
                                         String email, String name, String profileImageUrl,
                                         String accessToken, String refreshToken,
                                         LocalDateTime tokenExpiresAt) {

        // 기존 OAuth 계정 조회
        var existingAccount = oAuthAccountRepository
                .findByProviderAndProviderId(provider, providerId);

        if (existingAccount.isPresent()) {
            // 기존 사용자 - 토큰 업데이트
            OAuthAccount account = existingAccount.get();
            account.updateTokens(accessToken, refreshToken, tokenExpiresAt);

            User user = account.getUser();
            user.updateLastLoginAt();

            return createLoginResult(user, false);
        }

        // 이메일로 기존 사용자 확인 (다른 OAuth로 가입한 경우)
        var existingUser = userRepository.findByEmail(email);

        if (existingUser.isPresent()) {
            // 기존 사용자에게 OAuth 계정 추가
            User user = existingUser.get();
            linkOAuthAccount(user, provider, providerId, email, accessToken, refreshToken, tokenExpiresAt);
            user.updateLastLoginAt();

            return createLoginResult(user, false);
        }

        // 신규 사용자 등록
        User newUser = registerNewUser(provider, providerId, email, name, profileImageUrl,
                accessToken, refreshToken, tokenExpiresAt);

        return createLoginResult(newUser, true);
    }

    /**
     * 신규 사용자 등록
     * Register new user
     */
    @Transactional
    public User registerNewUser(OAuthProvider provider, String providerId,
                                String email, String name, String profileImageUrl,
                                String accessToken, String refreshToken,
                                LocalDateTime tokenExpiresAt) {

        UserGrade defaultGrade = userGradeService.getDefaultGradeForNewUser();

        User user = User.builder()
                .realName(name)
                .email(email)
                .profileImageUrl(profileImageUrl)
                .grade(defaultGrade)
                .associateStatus(AssociateStatus.PENDING)
                .build();

        OAuthAccount oAuthAccount = OAuthAccount.builder()
                .provider(provider)
                .providerId(providerId)
                .email(email)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenExpiresAt(tokenExpiresAt)
                .build();

        user.addOAuthAccount(oAuthAccount);

        User savedUser = userRepository.save(user);

        // 이벤트 발행
        eventPublisher.publishUserRegistered(savedUser);

        log.info("New user registered: {} via {}", email, provider);

        return savedUser;
    }

    /**
     * OAuth 계정 연결
     * Link OAuth account to existing user
     */
    @Transactional
    public void linkOAuthAccount(Long userId, OAuthProvider provider, String providerId,
                                 String email, String accessToken, String refreshToken,
                                 LocalDateTime tokenExpiresAt) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        linkOAuthAccount(user, provider, providerId, email, accessToken, refreshToken, tokenExpiresAt);
    }

    private void linkOAuthAccount(User user, OAuthProvider provider, String providerId,
                                  String email, String accessToken, String refreshToken,
                                  LocalDateTime tokenExpiresAt) {

        // 이미 연결된 제공자인지 확인
        if (oAuthAccountRepository.findByUserIdAndProvider(user.getId(), provider).isPresent()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "이미 연결된 " + provider + " 계정이 있습니다.");
        }

        // 다른 사용자가 사용 중인지 확인
        if (oAuthAccountRepository.existsByProviderAndProviderId(provider, providerId)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "이미 다른 계정에 연결된 " + provider + " 계정입니다.");
        }

        OAuthAccount oAuthAccount = OAuthAccount.builder()
                .provider(provider)
                .providerId(providerId)
                .email(email)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenExpiresAt(tokenExpiresAt)
                .build();

        user.addOAuthAccount(oAuthAccount);

        log.info("OAuth account linked: user={}, provider={}", user.getId(), provider);
    }

    /**
     * OAuth 계정 연결 해제
     * Unlink OAuth account
     */
    @Transactional
    public void unlinkOAuthAccount(Long userId, OAuthProvider provider) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 최소 하나의 인증 수단이 필요
        long oauthCount = oAuthAccountRepository.countByUserId(userId);
        long passkeyCount = user.getPasskeyCredentials().stream()
                .filter(PasskeyCredential::isActive)
                .count();

        if (oauthCount <= 1 && passkeyCount == 0) {
            throw new BusinessException(ErrorCode.CANNOT_DELETE_LAST_CREDENTIAL);
        }

        oAuthAccountRepository.deleteByUserIdAndProvider(userId, provider);

        log.info("OAuth account unlinked: user={}, provider={}", userId, provider);
    }

    /**
     * 사용자의 OAuth 계정 목록
     * Get user's OAuth accounts
     */
    public List<OAuthAccount> getUserOAuthAccounts(Long userId) {
        return oAuthAccountRepository.findByUserId(userId);
    }

    /**
     * 로그인 결과 생성
     * Create login result with JWT tokens
     */
    private LoginResult createLoginResult(User user, boolean isNewUser) {
        String accessToken = jwtTokenProvider.createAccessToken(
                user.getId(),
                user.getEmail(),
                user.getGrade().getCode()
        );
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getId());

        return new LoginResult(accessToken, refreshToken, user, isNewUser);
    }

    /**
     * 로그인 결과
     * Login result record
     */
    public record LoginResult(
            String accessToken,
            String refreshToken,
            User user,
            boolean isNewUser
    ) {}
}
