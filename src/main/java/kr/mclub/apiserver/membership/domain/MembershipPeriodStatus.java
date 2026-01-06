package kr.mclub.apiserver.membership.domain;

/**
 * 멤버십 기간 상태
 * Membership period status
 */
public enum MembershipPeriodStatus {

    /**
     * 활성 상태
     * Active membership
     */
    ACTIVE,

    /**
     * 만료됨
     * Expired
     */
    EXPIRED,

    /**
     * 취소됨
     * Cancelled
     */
    CANCELLED
}
