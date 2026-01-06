package kr.mclub.apiserver.user.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 등급 생성 요청 DTO
 * User grade creation request DTO
 */
public record UserGradeCreateRequest(

        @NotBlank(message = "등급 코드는 필수입니다.")
        String code,

        @NotBlank(message = "등급 이름은 필수입니다.")
        String name,

        String roleName,

        @NotNull(message = "권한 레벨은 필수입니다.")
        Integer permissionLevel,

        Boolean isExecutive,

        Boolean isStaff,

        Boolean isMember,

        Boolean requiresAnnualFee,

        String displaySuffix,

        Integer displayOrder
) {
    /**
     * 기본값 적용
     * Apply default values
     */
    public UserGradeCreateRequest {
        isExecutive = isExecutive != null ? isExecutive : false;
        isStaff = isStaff != null ? isStaff : false;
        isMember = isMember != null ? isMember : false;
        requiresAnnualFee = requiresAnnualFee != null ? requiresAnnualFee : false;
        displayOrder = displayOrder != null ? displayOrder : 100;
    }
}
