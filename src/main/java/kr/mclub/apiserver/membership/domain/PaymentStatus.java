package kr.mclub.apiserver.membership.domain;

/**
 * 결제 상태
 * Payment status
 */
public enum PaymentStatus {

    /**
     * 입금 확인 대기
     * Pending confirmation
     */
    PENDING,

    /**
     * 입금 확인 완료
     * Payment confirmed
     */
    CONFIRMED,

    /**
     * 결제 취소
     * Payment cancelled
     */
    CANCELLED,

    /**
     * 환불 완료
     * Refunded
     */
    REFUNDED
}
