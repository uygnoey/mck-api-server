package kr.mclub.apiserver.user.api.dto;

/**
 * 로그인 응답 DTO / Sign in response
 *
 * @param accessToken JWT 액세스 토큰
 * @param refreshToken JWT 리프레시 토큰
 * @param user 사용자 정보
 */
public record SignInResponse(
        String accessToken,
        String refreshToken,
        UserProfileResponse user
) {}
