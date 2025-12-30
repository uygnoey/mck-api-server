package kr.mclub.apiserver.user.api.dto;

/**
 * OAuth 로그인 응답 DTO
 * OAuth login response DTO
 */
public record OAuthLoginResponse(
        String accessToken,
        String refreshToken,
        UserProfileResponse user,
        boolean isNewUser
) {}
