package kr.mclub.apiserver.membership.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
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
 * 결제 기록 엔티티
 * Payment record entity
 */
@Entity
@Table(name = "payment_records")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentRecord extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "application_id")
    private Long applicationId;  // 정회원 신청 ID (nullable, 연회비는 null)

    // 결제 유형
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_type", nullable = false, length = 20)
    private PaymentType paymentType;

    @Column(name = "target_year", nullable = false)
    private Integer targetYear;  // 연회비 대상 년도

    // 금액
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    // 입금 정보
    @Column(name = "depositor_name", nullable = false, length = 50)
    private String depositorName;  // 입금자명

    @Column(name = "deposit_date", nullable = false)
    private LocalDate depositDate;  // 입금일

    // 상태
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status = PaymentStatus.PENDING;

    // 확인 정보
    @Column(name = "confirmed_by")
    private Long confirmedBy;  // 확인한 관리자 ID

    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;

    @Column(name = "auto_confirmed", nullable = false)
    private boolean autoConfirmed = false;  // 오픈뱅킹 자동 확인 여부

    // 오픈뱅킹 연동
    @Column(name = "bank_transaction_id", length = 100)
    private String bankTransactionId;  // 은행 거래 ID

    @Column(name = "bank_account_number", length = 50)
    private String bankAccountNumber;  // 입금 계좌번호 (마스킹)

    // 취소/환불 정보
    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "cancelled_by")
    private Long cancelledBy;

    @Column(name = "cancellation_reason", length = 500)
    private String cancellationReason;

    @Column(name = "refunded_at")
    private LocalDateTime refundedAt;

    @Column(name = "refund_amount", precision = 10, scale = 2)
    private BigDecimal refundAmount;

    @Builder
    public PaymentRecord(Long userId, Long applicationId, PaymentType paymentType,
                         Integer targetYear, BigDecimal amount,
                         String depositorName, LocalDate depositDate) {
        this.userId = userId;
        this.applicationId = applicationId;
        this.paymentType = paymentType;
        this.targetYear = targetYear;
        this.amount = amount;
        this.depositorName = depositorName;
        this.depositDate = depositDate;
        this.status = PaymentStatus.PENDING;
        this.autoConfirmed = false;
    }

    /**
     * 결제 확인 (관리자)
     * Confirm payment by admin
     */
    public void confirm(Long adminId) {
        this.status = PaymentStatus.CONFIRMED;
        this.confirmedBy = adminId;
        this.confirmedAt = LocalDateTime.now();
    }

    /**
     * 결제 자동 확인 (오픈뱅킹)
     * Auto-confirm payment via open banking
     */
    public void autoConfirm(String bankTransactionId, String bankAccountNumber) {
        this.status = PaymentStatus.CONFIRMED;
        this.confirmedAt = LocalDateTime.now();
        this.autoConfirmed = true;
        this.bankTransactionId = bankTransactionId;
        this.bankAccountNumber = bankAccountNumber;
    }

    /**
     * 결제 취소
     * Cancel payment
     */
    public void cancel(String reason, Long adminId) {
        this.status = PaymentStatus.CANCELLED;
        this.cancellationReason = reason;
        this.cancelledAt = LocalDateTime.now();
        this.cancelledBy = adminId;
    }

    /**
     * 환불 처리
     * Process refund
     */
    public void refund(BigDecimal refundAmount) {
        this.status = PaymentStatus.REFUNDED;
        this.refundAmount = refundAmount;
        this.refundedAt = LocalDateTime.now();
    }

    /**
     * 상태 변경
     * Change status
     */
    public void changeStatus(PaymentStatus newStatus) {
        this.status = newStatus;
    }
}
