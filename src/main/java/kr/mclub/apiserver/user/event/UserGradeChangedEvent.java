package kr.mclub.apiserver.user.event;

import kr.mclub.apiserver.shared.domain.DomainEvent;

import java.time.LocalDateTime;

/**
 * 사용자 등급 변경 이벤트
 * Event published when a user's grade is changed
 */
public record UserGradeChangedEvent(
        Long userId,
        String email,
        String oldGradeCode,
        String newGradeCode,
        Long changedByAdminId,
        LocalDateTime occurredAt
) implements DomainEvent {

    public UserGradeChangedEvent(Long userId, String email, String oldGradeCode,
                                 String newGradeCode, Long changedByAdminId) {
        this(userId, email, oldGradeCode, newGradeCode, changedByAdminId, LocalDateTime.now());
    }
}
