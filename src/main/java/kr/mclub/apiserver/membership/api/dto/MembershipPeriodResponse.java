package kr.mclub.apiserver.membership.api.dto;

import java.time.LocalDateTime;

import kr.mclub.apiserver.membership.domain.MembershipPeriod;
import kr.mclub.apiserver.membership.domain.MembershipPeriodStatus;

/**
 * 멤버십 기간 응답 DTO
 * Membership period response DTO
 */
public record MembershipPeriodResponse(
        Long id,
        Long userId,
        Integer startYear,
        Integer endYear,
        MembershipPeriodStatus status,
        boolean isRenewed,
        LocalDateTime renewedAt,
        Long renewalPaymentId,
        LocalDateTime expiredAt,
        LocalDateTime expirationNotifiedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    /**
     * 엔티티로부터 응답 DTO 생성
     * Create response DTO from entity
     */
    public static MembershipPeriodResponse from(MembershipPeriod period) {
        return new MembershipPeriodResponse(
                period.getId(),
                period.getUserId(),
                period.getStartYear(),
                period.getEndYear(),
                period.getStatus(),
                period.isRenewed(),
                period.getRenewedAt(),
                period.getRenewalPaymentId(),
                period.getExpiredAt(),
                period.getExpirationNotifiedAt(),
                period.getCreatedAt(),
                period.getUpdatedAt()
        );
    }
}
