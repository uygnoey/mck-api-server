package kr.mclub.apiserver.user.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 등급 수정 요청 DTO
 * User grade update request DTO
 */
public record UserGradeUpdateRequest(

        @NotBlank(message = "등급 이름은 필수입니다.")
        String name,

        @NotNull(message = "권한 레벨은 필수입니다.")
        Integer permissionLevel,

        @NotNull(message = "임원 여부는 필수입니다.")
        Boolean isExecutive,

        @NotNull(message = "운영진 여부는 필수입니다.")
        Boolean isStaff,

        String displaySuffix,

        @NotNull(message = "표시 순서는 필수입니다.")
        Integer displayOrder
) {
}
