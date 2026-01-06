package kr.mclub.apiserver.user.event;

import kr.mclub.apiserver.user.domain.User;
import kr.mclub.apiserver.user.domain.UserGrade;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * 사용자 도메인 이벤트 발행자
 * User domain event publisher
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserEventPublisher {

    private final ApplicationEventPublisher eventPublisher;

    /**
     * 사용자 등록 이벤트 발행
     * Publish user registered event
     */
    public void publishUserRegistered(User user) {
        UserRegisteredEvent event = new UserRegisteredEvent(
                user.getId(),
                user.getEmail(),
                user.getRealName(),
                user.getGrade().getCode()
        );

        eventPublisher.publishEvent(event);
        log.info("Published UserRegisteredEvent: userId={}, email={}", user.getId(), user.getEmail());
    }

    /**
     * 사용자 등급 변경 이벤트 발행
     * Publish user grade changed event
     */
    public void publishGradeChanged(User user, UserGrade oldGrade, Long changedByAdminId) {
        UserGradeChangedEvent event = new UserGradeChangedEvent(
                user.getId(),
                user.getEmail(),
                oldGrade.getCode(),
                user.getGrade().getCode(),
                changedByAdminId
        );

        eventPublisher.publishEvent(event);
        log.info("Published UserGradeChangedEvent: userId={}, {} -> {}",
                user.getId(), oldGrade.getCode(), user.getGrade().getCode());
    }

    /**
     * 사용자 탈퇴 이벤트 발행
     * Publish user withdrawn event
     */
    public void publishUserWithdrawn(User user) {
        UserWithdrawnEvent event = new UserWithdrawnEvent(
                user.getId(),
                user.getEmail(),
                user.getMemberNumber(),
                user.getGrade().getCode(),
                user.getWithdrawalReason()
        );

        eventPublisher.publishEvent(event);
        log.info("Published UserWithdrawnEvent: userId={}, memberNumber={}",
                user.getId(), user.getMemberNumber());
    }
}
