package kr.mclub.apiserver.membership.domain;

import java.time.LocalDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import kr.mclub.apiserver.shared.domain.BaseTimeEntity;

/**
 * 멤버십 기간 엔티티
 * Membership period entity
 */
@Entity
@Table(name = "membership_periods")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MembershipPeriod extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    // 기간 정보
    @Column(name = "start_year", nullable = false)
    private Integer startYear;  // 시작 년도

    @Column(name = "end_year", nullable = false)
    private Integer endYear;  // 종료 년도 (해당 년도 12월 31일까지 유효)

    // 상태
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MembershipPeriodStatus status = MembershipPeriodStatus.ACTIVE;

    // 갱신 정보
    @Column(name = "is_renewed", nullable = false)
    private boolean isRenewed = false;  // 갱신 여부

    @Column(name = "renewed_at")
    private LocalDateTime renewedAt;

    @Column(name = "renewal_payment_id")
    private Long renewalPaymentId;  // 갱신 결제 ID

    // 만료 처리
    @Column(name = "expired_at")
    private LocalDateTime expiredAt;

    @Column(name = "expiration_notified_at")
    private LocalDateTime expirationNotifiedAt;  // 만료 알림 발송 시각

    @Builder
    public MembershipPeriod(Long userId, Integer startYear, Integer endYear) {
        this.userId = userId;
        this.startYear = startYear;
        this.endYear = endYear;
        this.status = MembershipPeriodStatus.ACTIVE;
        this.isRenewed = false;
    }

    /**
     * 멤버십 갱신
     * Renew membership
     */
    public void renew(Long paymentId) {
        this.isRenewed = true;
        this.renewedAt = LocalDateTime.now();
        this.renewalPaymentId = paymentId;
    }

    /**
     * 멤버십 만료 처리
     * Expire membership
     */
    public void expire() {
        this.status = MembershipPeriodStatus.EXPIRED;
        this.expiredAt = LocalDateTime.now();
    }

    /**
     * 멤버십 취소
     * Cancel membership
     */
    public void cancel() {
        this.status = MembershipPeriodStatus.CANCELLED;
    }

    /**
     * 만료 알림 발송 기록
     * Mark expiration notification as sent
     */
    public void markExpirationNotified() {
        this.expirationNotifiedAt = LocalDateTime.now();
    }

    /**
     * 상태 변경
     * Change status
     */
    public void changeStatus(MembershipPeriodStatus newStatus) {
        this.status = newStatus;
    }

    /**
     * 현재 활성 상태인지 확인
     * Check if currently active
     */
    public boolean isActive() {
        return this.status == MembershipPeriodStatus.ACTIVE;
    }
}
