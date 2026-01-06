package kr.mclub.apiserver.membership.event;

import java.time.LocalDateTime;

/**
 * 정회원 신청서 제출 이벤트 / Membership Application Submitted Event
 *
 * @param applicationId 신청서 ID
 * @param userId 사용자 ID
 * @param realName 실명
 * @param submittedAt 제출 시각
 * @since 1.0
 */
public record MembershipApplicationSubmittedEvent(
        Long applicationId,
        Long userId,
        String realName,
        LocalDateTime submittedAt
) {
    public static MembershipApplicationSubmittedEvent of(
            Long applicationId,
            Long userId,
            String realName
    ) {
        return new MembershipApplicationSubmittedEvent(
                applicationId,
                userId,
                realName,
                LocalDateTime.now()
        );
    }
}
