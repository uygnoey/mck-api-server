package kr.mclub.apiserver.membership.domain;

/**
 * 정회원 신청 상태
 * Membership application status
 */
public enum ApplicationStatus {

    /**
     * 서류 제출 대기
     * Waiting for document submission
     */
    DOCUMENT_PENDING,

    /**
     * 서류 제출 완료
     * Documents submitted
     */
    DOCUMENT_SUBMITTED,

    /**
     * 서류 검토 중
     * Documents under review
     */
    UNDER_REVIEW,

    /**
     * 서류 승인
     * Documents approved
     */
    DOCUMENT_APPROVED,

    /**
     * 서류 반려
     * Documents rejected
     */
    DOCUMENT_REJECTED,

    /**
     * 결제 대기
     * Waiting for payment
     */
    PAYMENT_PENDING,

    /**
     * 결제 확인 완료
     * Payment confirmed
     */
    PAYMENT_CONFIRMED,

    /**
     * 신청 완료 (정회원 전환)
     * Application completed (became regular member)
     */
    COMPLETED,

    /**
     * 신청 취소
     * Application cancelled
     */
    CANCELLED
}
