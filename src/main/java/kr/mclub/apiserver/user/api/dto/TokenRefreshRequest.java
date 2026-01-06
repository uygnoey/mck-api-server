package kr.mclub.apiserver.user.api.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 토큰 갱신 요청 DTO
 * Token refresh request DTO
 */
public record TokenRefreshRequest(
        @NotBlank(message = "리프레시 토큰이 필요합니다.")
        String refreshToken
) {}
