package kr.mclub.apiserver.user.api;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import kr.mclub.apiserver.shared.security.CurrentUser;
import kr.mclub.apiserver.shared.util.ApiResponse;
import kr.mclub.apiserver.user.api.dto.ChangePasswordRequest;
import kr.mclub.apiserver.user.api.dto.OAuthLoginRequest;
import kr.mclub.apiserver.user.api.dto.OAuthLoginResponse;
import kr.mclub.apiserver.user.api.dto.SignInRequest;
import kr.mclub.apiserver.user.api.dto.SignInResponse;
import kr.mclub.apiserver.user.api.dto.SignUpRequest;
import kr.mclub.apiserver.user.api.dto.SignUpResponse;
import kr.mclub.apiserver.user.api.dto.TokenRefreshRequest;
import kr.mclub.apiserver.user.api.dto.TokenRefreshResponse;
import kr.mclub.apiserver.user.api.dto.UserProfileResponse;
import kr.mclub.apiserver.user.service.AuthService;

/**
 * 인증 API 컨트롤러
 * Authentication API controller
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

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

        AuthService.OAuthLoginResult result = authService.oauthLogin(
                provider,
                request.code(),
                request.redirectUri()
        );

        return ApiResponse.success(new OAuthLoginResponse(
                result.accessToken(),
                result.refreshToken(),
                UserProfileResponse.from(result.user()),
                result.isNewUser()
        ));
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

        AuthService.TokenRefreshResult result = authService.refreshToken(request.refreshToken());

        return ApiResponse.success(new TokenRefreshResponse(
                result.accessToken(),
                result.refreshToken()
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

    /**
     * 회원가입 / Sign up
     *
     * POST /api/v1/auth/signup
     */
    @PostMapping("/signup")
    public ApiResponse<SignUpResponse> signUp(@Valid @RequestBody SignUpRequest request) {
        AuthService.SignUpResult result = authService.signUp(
                request.email(),
                request.password(),
                request.realName(),
                request.phoneNumber()
        );

        return ApiResponse.success(new SignUpResponse(
                result.accessToken(),
                result.refreshToken(),
                UserProfileResponse.from(result.user())
        ));
    }

    /**
     * 로그인 / Sign in
     *
     * POST /api/v1/auth/signin
     */
    @PostMapping("/signin")
    public ApiResponse<SignInResponse> signIn(@Valid @RequestBody SignInRequest request) {
        AuthService.SignInResult result = authService.signIn(
                request.email(),
                request.password()
        );

        return ApiResponse.success(new SignInResponse(
                result.accessToken(),
                result.refreshToken(),
                UserProfileResponse.from(result.user())
        ));
    }

    /**
     * 비밀번호 변경 / Change password
     *
     * POST /api/v1/auth/password/change
     */
    @PostMapping("/password/change")
    public ApiResponse<Void> changePassword(
            @CurrentUser Long userId,
            @Valid @RequestBody ChangePasswordRequest request) {

        authService.changePassword(userId, request.currentPassword(), request.newPassword());

        return ApiResponse.success("비밀번호가 변경되었습니다.");
    }
}
