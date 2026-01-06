package kr.mclub.apiserver.user.event;

import kr.mclub.apiserver.shared.domain.DomainEvent;

import java.time.LocalDateTime;

/**
 * 사용자 등록 이벤트
 * Event published when a new user is registered
 */
public record UserRegisteredEvent(
        Long userId,
        String email,
        String realName,
        String gradeCode,
        LocalDateTime occurredAt
) implements DomainEvent {

    public UserRegisteredEvent(Long userId, String email, String realName, String gradeCode) {
        this(userId, email, realName, gradeCode, LocalDateTime.now());
    }
}
