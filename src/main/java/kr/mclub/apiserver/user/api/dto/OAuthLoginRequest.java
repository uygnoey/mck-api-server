package kr.mclub.apiserver.user.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import kr.mclub.apiserver.user.domain.OAuthProvider;

/**
 * OAuth 로그인 요청 DTO
 * OAuth login request DTO
 */
public record OAuthLoginRequest(
        @NotNull(message = "OAuth 제공자를 선택해주세요.")
        OAuthProvider provider,

        @NotBlank(message = "인증 코드가 필요합니다.")
        String code,

        String redirectUri
) {}
