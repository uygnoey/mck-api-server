package kr.mclub.apiserver.user.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import kr.mclub.apiserver.shared.exception.BusinessException;
import kr.mclub.apiserver.shared.exception.ErrorCode;
import kr.mclub.apiserver.shared.security.JwtTokenProvider;
import kr.mclub.apiserver.user.domain.AssociateStatus;
import kr.mclub.apiserver.user.domain.OAuthProvider;
import kr.mclub.apiserver.user.domain.User;
import kr.mclub.apiserver.user.domain.UserGrade;
import kr.mclub.apiserver.user.event.UserEventPublisher;
import kr.mclub.apiserver.user.oauth.OAuth2Client;
import kr.mclub.apiserver.user.oauth.OAuth2ClientFactory;
import kr.mclub.apiserver.user.repository.UserRepository;

/**
 * 인증 서비스 / Authentication Service
 *
 * <p>자체 로그인 (이메일 + 비밀번호) 인증을 처리합니다.</p>
 *
 * @since 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final UserGradeService userGradeService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserEventPublisher eventPublisher;
    private final OAuth2UserService oAuth2UserService;
    private final OAuth2ClientFactory oAuth2ClientFactory;
    private final UserService userService;

    /**
     * 회원가입 / Sign up
     *
     * @param email 이메일
     * @param password 비밀번호 (평문)
     * @param realName 실명
     * @param phoneNumber 전화번호
     * @return 가입된 사용자 정보 및 JWT 토큰
     */
    @Transactional
    public SignUpResult signUp(String email, String password, String realName, String phoneNumber) {
        // 이메일 중복 확인
        if (userRepository.existsByEmail(email)) {
            throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
        }

        // 비밀번호 암호화
        String encryptedPassword = passwordEncoder.encode(password);

        // 기본 등급 조회 (ASSOCIATE)
        UserGrade defaultGrade = userGradeService.getDefaultGradeForNewUser();

        // 사용자 생성
        User user = User.builder()
                .email(email)
                .password(encryptedPassword)
                .realName(realName)
                .phoneNumber(phoneNumber)
                .grade(defaultGrade)
                .associateStatus(AssociateStatus.PENDING)
                .build();

        User savedUser = userRepository.save(user);

        // 이벤트 발행
        eventPublisher.publishUserRegistered(savedUser);

        log.info("New user signed up: {}", email);

        // JWT 토큰 생성
        String accessToken = jwtTokenProvider.createAccessToken(
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getGrade().getCode()
        );
        String refreshToken = jwtTokenProvider.createRefreshToken(savedUser.getId());

        return new SignUpResult(accessToken, refreshToken, savedUser);
    }

    /**
     * 로그인 / Sign in
     *
     * @param email 이메일
     * @param password 비밀번호 (평문)
     * @return JWT 토큰 및 사용자 정보
     */
    @Transactional
    public SignInResult signIn(String email, String password) {
        // 사용자 조회
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CREDENTIALS));

        // 탈퇴한 사용자 확인
        if (user.isWithdrawn()) {
            throw new BusinessException(ErrorCode.USER_WITHDRAWN);
        }

        // 비활성 계정 확인
        if (!user.isActive()) {
            throw new BusinessException(ErrorCode.USER_INACTIVE);
        }

        // 비밀번호 설정 여부 확인
        if (!user.hasPassword()) {
            throw new BusinessException(ErrorCode.PASSWORD_NOT_SET, "소셜 로그인 전용 계정입니다. OAuth로 로그인하세요.");
        }

        // 비밀번호 검증
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }

        // 마지막 로그인 시간 업데이트
        user.updateLastLoginAt();

        log.info("User signed in: {}", email);

        // JWT 토큰 생성
        String accessToken = jwtTokenProvider.createAccessToken(
                user.getId(),
                user.getEmail(),
                user.getGrade().getCode()
        );
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getId());

        return new SignInResult(accessToken, refreshToken, user);
    }

    /**
     * 비밀번호 변경 / Change password
     *
     * @param userId 사용자 ID
     * @param currentPassword 현재 비밀번호 (평문)
     * @param newPassword 새 비밀번호 (평문)
     */
    @Transactional
    public void changePassword(Long userId, String currentPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 현재 비밀번호 확인
        if (!user.hasPassword()) {
            throw new BusinessException(ErrorCode.PASSWORD_NOT_SET, "비밀번호가 설정되지 않았습니다.");
        }

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS, "현재 비밀번호가 일치하지 않습니다.");
        }

        // 새 비밀번호 암호화 및 저장
        String encryptedPassword = passwordEncoder.encode(newPassword);
        user.changePassword(encryptedPassword);

        log.info("User password changed: userId={}", userId);
    }

    /**
     * 소셜 로그인 계정에 비밀번호 설정 / Set password for OAuth account
     *
     * @param userId 사용자 ID
     * @param newPassword 새 비밀번호 (평문)
     */
    @Transactional
    public void setPassword(Long userId, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 이미 비밀번호가 있는 경우
        if (user.hasPassword()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "이미 비밀번호가 설정되어 있습니다. 비밀번호 변경을 사용하세요.");
        }

        // 비밀번호 암호화 및 저장
        String encryptedPassword = passwordEncoder.encode(newPassword);
        user.changePassword(encryptedPassword);

        log.info("Password set for OAuth user: userId={}", userId);
    }

    /**
     * 비밀번호 재설정 (관리자용) / Reset password (admin only)
     *
     * @param userId 사용자 ID
     * @param newPassword 새 비밀번호 (평문)
     * @param adminId 관리자 ID
     */
    @Transactional
    public void resetPassword(Long userId, String newPassword, Long adminId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 비밀번호 암호화 및 저장
        String encryptedPassword = passwordEncoder.encode(newPassword);
        user.changePassword(encryptedPassword);

        log.info("Password reset by admin: userId={}, adminId={}", userId, adminId);
    }

    /**
     * 회원가입 결과 / Sign up result
     */
    public record SignUpResult(
            String accessToken,
            String refreshToken,
            User user
    ) {}

    /**
     * 로그인 결과 / Sign in result
     */
    public record SignInResult(
            String accessToken,
            String refreshToken,
            User user
    ) {}

    /**
     * OAuth 로그인 / OAuth login
     *
     * @param provider OAuth 제공자 (google, naver, apple)
     * @param code Authorization code
     * @param redirectUri Redirect URI
     * @return OAuth 로그인 결과
     */
    @Transactional
    public OAuthLoginResult oauthLogin(String provider, String code, String redirectUri) {
        // Provider 파싱
        OAuthProvider oAuthProvider;
        try {
            oAuthProvider = OAuthProvider.valueOf(provider.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException(
                    ErrorCode.OAUTH_PROVIDER_NOT_SUPPORTED,
                    "지원하지 않는 OAuth 제공자입니다: " + provider
            );
        }

        // OAuth2 클라이언트 가져오기
        OAuth2Client oAuth2Client = oAuth2ClientFactory.getClient(oAuthProvider);

        // Authorization code를 access token으로 교환
        OAuth2Client.OAuth2TokenResponse tokenResponse = oAuth2Client.exchangeCodeForToken(
                code,
                redirectUri
        );

        // Access token으로 사용자 정보 조회
        OAuth2Client.OAuth2UserInfo userInfo = oAuth2Client.getUserInfo(tokenResponse.accessToken());

        // OAuth 로그인 처리 (신규 사용자 등록 또는 기존 사용자 로그인)
        OAuth2UserService.LoginResult loginResult = oAuth2UserService.processOAuthLogin(
                oAuthProvider,
                userInfo.providerId(),
                userInfo.email(),
                userInfo.name(),
                userInfo.profileImageUrl(),
                tokenResponse.accessToken(),
                tokenResponse.refreshToken(),
                tokenResponse.expiresAt()
        );

        log.info("OAuth login successful: provider={}, email={}, isNewUser={}",
                provider, userInfo.email(), loginResult.isNewUser());

        return new OAuthLoginResult(
                loginResult.accessToken(),
                loginResult.refreshToken(),
                loginResult.user(),
                loginResult.isNewUser()
        );
    }

    /**
     * 토큰 갱신 / Refresh token
     *
     * @param refreshToken 리프레시 토큰
     * @return 새로운 액세스 토큰 및 리프레시 토큰
     */
    @Transactional
    public TokenRefreshResult refreshToken(String refreshToken) {
        // 리프레시 토큰 유효성 검증
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }

        // 토큰 유형 확인
        if (!"refresh".equals(jwtTokenProvider.getTokenType(refreshToken))) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN, "리프레시 토큰이 아닙니다.");
        }

        // 토큰 만료 확인
        if (jwtTokenProvider.isExpired(refreshToken)) {
            throw new BusinessException(ErrorCode.TOKEN_EXPIRED);
        }

        // 사용자 정보 조회
        Long userId = jwtTokenProvider.getUserId(refreshToken);
        User user = userService.getUserById(userId);

        // 새 토큰 발급
        JwtTokenProvider.TokenPair tokenPair = jwtTokenProvider.refreshTokens(
                refreshToken,
                user.getEmail(),
                user.getGrade().getCode()
        );

        log.info("Token refreshed for user: userId={}", userId);

        return new TokenRefreshResult(
                tokenPair.accessToken(),
                tokenPair.refreshToken()
        );
    }

    /**
     * OAuth 로그인 결과 / OAuth login result
     */
    public record OAuthLoginResult(
            String accessToken,
            String refreshToken,
            User user,
            boolean isNewUser
    ) {}

    /**
     * 토큰 갱신 결과 / Token refresh result
     */
    public record TokenRefreshResult(
            String accessToken,
            String refreshToken
    ) {}
}
