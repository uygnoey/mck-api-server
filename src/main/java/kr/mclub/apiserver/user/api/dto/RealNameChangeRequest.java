package kr.mclub.apiserver.user.api.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 실명 변경 요청 DTO
 * Real name change request DTO
 */
public record RealNameChangeRequest(

        @NotBlank(message = "새로운 실명은 필수입니다.")
        String newRealName,

        @NotBlank(message = "변경 사유는 필수입니다.")
        String reason
) {
}
