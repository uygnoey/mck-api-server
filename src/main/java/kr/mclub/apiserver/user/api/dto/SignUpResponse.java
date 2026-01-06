package kr.mclub.apiserver.user.api.dto;

/**
 * 회원가입 응답 DTO / Sign up response
 *
 * @param accessToken JWT 액세스 토큰
 * @param refreshToken JWT 리프레시 토큰
 * @param user 사용자 정보
 */
public record SignUpResponse(
        String accessToken,
        String refreshToken,
        UserProfileResponse user
) {}
