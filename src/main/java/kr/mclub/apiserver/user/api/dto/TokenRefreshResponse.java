package kr.mclub.apiserver.user.api.dto;

/**
 * 토큰 갱신 응답 DTO
 * Token refresh response DTO
 */
public record TokenRefreshResponse(
        String accessToken,
        String refreshToken
) {}
