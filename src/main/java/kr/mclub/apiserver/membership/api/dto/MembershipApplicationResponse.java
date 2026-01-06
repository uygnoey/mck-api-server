package kr.mclub.apiserver.membership.api.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import kr.mclub.apiserver.membership.domain.ApplicationStatus;
import kr.mclub.apiserver.membership.domain.MembershipApplication;
import kr.mclub.apiserver.user.domain.VehicleOwnershipType;

/**
 * 정회원 신청 응답 DTO
 * Membership application response DTO
 */
public record MembershipApplicationResponse(
        Long id,
        Long userId,
        String applicationNumber,
        ApplicationStatus status,
        VehicleOwnershipType vehicleOwnershipType,
        String applicantName,
        String applicantPhone,
        String applicantEmail,
        String carNumber,
        String vinNumber,
        String carModel,
        Long reviewedBy,
        LocalDateTime reviewedAt,
        String rejectionReason,
        BigDecimal paymentAmount,
        Integer targetYear,
        LocalDateTime approvedAt,
        Integer assignedMemberNumber,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    /**
     * 엔티티로부터 응답 DTO 생성
     * Create response DTO from entity
     */
    public static MembershipApplicationResponse from(MembershipApplication application) {
        return new MembershipApplicationResponse(
                application.getId(),
                application.getUserId(),
                application.getApplicationNumber(),
                application.getStatus(),
                application.getVehicleOwnershipType(),
                application.getApplicantName(),
                application.getApplicantPhone(),
                application.getApplicantEmail(),
                application.getCarNumber(),
                application.getVinNumber(),
                application.getCarModel(),
                application.getReviewedBy(),
                application.getReviewedAt(),
                application.getRejectionReason(),
                application.getPaymentAmount(),
                application.getTargetYear(),
                application.getApprovedAt(),
                application.getAssignedMemberNumber(),
                application.getCreatedAt(),
                application.getUpdatedAt()
        );
    }
}
