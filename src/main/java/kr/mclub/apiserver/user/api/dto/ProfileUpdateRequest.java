package kr.mclub.apiserver.user.api.dto;

import jakarta.validation.constraints.Pattern;

/**
 * 프로필 수정 요청 DTO
 * Profile update request DTO
 */
public record ProfileUpdateRequest(
        @Pattern(regexp = "^01[0-9]-?[0-9]{3,4}-?[0-9]{4}$", message = "올바른 전화번호 형식이 아닙니다.")
        String phoneNumber,

        String profileImageUrl
) {}
