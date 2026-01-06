package kr.mclub.apiserver.membership.domain;

/**
 * 서류 검증 상태
 * Document verification status
 */
public enum VerificationStatus {

    /**
     * 검증 대기
     * Pending verification
     */
    PENDING,

    /**
     * 검증 완료
     * Verified
     */
    VERIFIED,

    /**
     * 검증 반려
     * Rejected
     */
    REJECTED
}
