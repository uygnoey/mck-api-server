package kr.mclub.apiserver.user.api.dto;

import kr.mclub.apiserver.user.domain.AssociateStatus;
import kr.mclub.apiserver.user.domain.ExemptionType;
import kr.mclub.apiserver.user.domain.User;

import java.time.LocalDateTime;

/**
 * 사용자 프로필 응답 DTO
 * User profile response DTO
 */
public record UserProfileResponse(
        Long id,
        Integer memberNumber,
        String realName,
        String email,
        String phoneNumber,
        String profileImageUrl,
        String displayName,
        GradeInfo grade,
        AssociateStatus associateStatus,
        ExemptionInfo exemption,
        LocalDateTime lastLoginAt,
        LocalDateTime createdAt
) {
    /**
     * User 엔티티에서 변환
     * Convert from User entity
     */
    public static UserProfileResponse from(User user) {
        return new UserProfileResponse(
                user.getId(),
                user.getMemberNumber(),
                user.getRealName(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getProfileImageUrl(),
                user.getDisplayName(),
                new GradeInfo(
                        user.getGrade().getCode(),
                        user.getGrade().getName(),
                        user.getGrade().getPermissionLevel(),
                        user.getGrade().isExecutive(),
                        user.getGrade().isStaff()
                ),
                user.getAssociateStatus(),
                new ExemptionInfo(
                        user.getExemptionType(),
                        user.getExemptionReason(),
                        user.getExemptionYear()
                ),
                user.getLastLoginAt(),
                user.getCreatedAt()
        );
    }

    /**
     * 등급 정보
     * Grade info
     */
    public record GradeInfo(
            String code,
            String name,
            Integer permissionLevel,
            boolean isExecutive,
            boolean isStaff
    ) {}

    /**
     * 면제 정보
     * Exemption info
     */
    public record ExemptionInfo(
            ExemptionType type,
            String reason,
            Integer year
    ) {}
}
