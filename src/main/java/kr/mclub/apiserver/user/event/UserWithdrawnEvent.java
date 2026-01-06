package kr.mclub.apiserver.user.event;

import kr.mclub.apiserver.shared.domain.DomainEvent;

import java.time.LocalDateTime;

/**
 * 사용자 탈퇴 이벤트
 * Event published when a user withdraws their membership
 */
public record UserWithdrawnEvent(
        Long userId,
        String email,
        Integer memberNumber,
        String gradeCode,
        String withdrawalReason,
        LocalDateTime occurredAt
) implements DomainEvent {

    public UserWithdrawnEvent(Long userId, String email, Integer memberNumber,
                              String gradeCode, String withdrawalReason) {
        this(userId, email, memberNumber, gradeCode, withdrawalReason, LocalDateTime.now());
    }
}
