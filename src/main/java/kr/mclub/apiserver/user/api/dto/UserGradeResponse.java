package kr.mclub.apiserver.user.api.dto;

import kr.mclub.apiserver.user.domain.UserGrade;

/**
 * 사용자 등급 응답 DTO
 * User grade response DTO
 */
public record UserGradeResponse(
        Long id,
        String code,
        String name,
        Integer permissionLevel,
        boolean isExecutive,
        boolean isStaff,
        boolean isMember,
        boolean requiresAnnualFee,
        boolean isSystemGrade,
        String displaySuffix,
        Integer displayOrder
) {
    /**
     * UserGrade 엔티티에서 변환
     * Convert from UserGrade entity
     */
    public static UserGradeResponse from(UserGrade grade) {
        return new UserGradeResponse(
                grade.getId(),
                grade.getCode(),
                grade.getName(),
                grade.getPermissionLevel(),
                grade.isExecutive(),
                grade.isStaff(),
                grade.isMember(),
                grade.isRequiresAnnualFee(),
                grade.isSystemGrade(),
                grade.getDisplaySuffix(),
                grade.getDisplayOrder()
        );
    }
}
