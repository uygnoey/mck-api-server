package kr.mclub.apiserver.user.api;

import jakarta.validation.Valid;
import kr.mclub.apiserver.shared.exception.BusinessException;
import kr.mclub.apiserver.shared.exception.ErrorCode;
import kr.mclub.apiserver.shared.security.JwtTokenProvider;
import kr.mclub.apiserver.shared.util.ApiResponse;
import kr.mclub.apiserver.user.api.dto.*;
import kr.mclub.apiserver.user.service.OAuth2UserService;
import kr.mclub.apiserver.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 인증 API 컨트롤러
 * Authentication API controller
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final OAuth2UserService oAuth2UserService;
    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * OAuth 로그인
     * OAuth login endpoint
     *
     * POST /api/v1/auth/oauth/{provider}
     */
    @PostMapping("/oauth/{provider}")
    public ApiResponse<OAuthLoginResponse> oauthLogin(
            @PathVariable String provider,
            @Valid @RequestBody OAuthLoginRequest request) {

        // TODO: OAuth 제공자별 토큰 교환 및 사용자 정보 조회 구현
        // 현재는 클라이언트에서 이미 토큰 교환을 완료하고 사용자 정보를 전달받는 방식으로 구현

        throw new UnsupportedOperationException("OAuth 로그인은 별도의 OAuth2 콜백 처리가 필요합니다.");
    }

    /**
     * 토큰 갱신
     * Refresh token endpoint
     *
     * POST /api/v1/auth/refresh
     */
    @PostMapping("/refresh")
    public ApiResponse<TokenRefreshResponse> refreshToken(
            @Valid @RequestBody TokenRefreshRequest request) {

        String refreshToken = request.refreshToken();

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
        var user = userService.getUserById(userId);

        // 새 토큰 발급
        JwtTokenProvider.TokenPair tokenPair = jwtTokenProvider.refreshTokens(
                refreshToken,
                user.getEmail(),
                user.getGrade().getCode()
        );

        return ApiResponse.success(new TokenRefreshResponse(
                tokenPair.accessToken(),
                tokenPair.refreshToken()
        ));
    }

    /**
     * 로그아웃
     * Logout endpoint (클라이언트에서 토큰 삭제)
     *
     * POST /api/v1/auth/logout
     */
    @PostMapping("/logout")
    public ApiResponse<Void> logout() {
        // JWT 기반 인증에서는 서버에서 토큰 무효화가 어려우므로
        // 클라이언트에서 토큰을 삭제하도록 안내
        // 필요시 Redis 블랙리스트 등으로 토큰 무효화 구현 가능
        return ApiResponse.success("로그아웃 되었습니다.");
    }
}
