package kr.mclub.apiserver.membership.event;

import java.time.LocalDateTime;

/**
 * 정회원 신청서 승인 이벤트 / Membership Application Approved Event
 *
 * @param applicationId 신청서 ID
 * @param userId 사용자 ID
 * @param realName 실명
 * @param reviewedBy 승인한 관리자 ID
 * @param approvedAt 승인 시각
 * @since 1.0
 */
public record MembershipApplicationApprovedEvent(
        Long applicationId,
        Long userId,
        String realName,
        Long reviewedBy,
        LocalDateTime approvedAt
) {
    public static MembershipApplicationApprovedEvent of(
            Long applicationId,
            Long userId,
            String realName,
            Long reviewedBy
    ) {
        return new MembershipApplicationApprovedEvent(
                applicationId,
                userId,
                realName,
                reviewedBy,
                LocalDateTime.now()
        );
    }
}
