package kr.mclub.apiserver.shared.domain;

import java.time.LocalDateTime;

/**
 * 도메인 이벤트 인터페이스
 * Domain event interface for event-driven communication between modules
 */
public interface DomainEvent {

    /**
     * 이벤트 발생 시각
     * @return the timestamp when the event occurred
     */
    default LocalDateTime occurredAt() {
        return LocalDateTime.now();
    }

    /**
     * 이벤트 이름 (기본값: 클래스 이름)
     * @return the event name (defaults to class simple name)
     */
    default String eventName() {
        return this.getClass().getSimpleName();
    }
}
