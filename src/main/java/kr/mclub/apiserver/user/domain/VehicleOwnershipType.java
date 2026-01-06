package kr.mclub.apiserver.user.domain;

/**
 * 차량 소유 유형
 * Vehicle ownership type
 */
public enum VehicleOwnershipType {

    /**
     * 개인 소유
     * Personal ownership
     */
    PERSONAL,

    /**
     * 법인 소유
     * Corporate ownership
     */
    CORPORATE,

    /**
     * 개인 리스
     * Personal lease
     */
    LEASE,

    /**
     * 개인 렌트
     * Personal rental
     */
    RENTAL,

    /**
     * 법인 리스
     * Corporate lease
     */
    CORPORATE_LEASE,

    /**
     * 법인 렌트
     * Corporate rental
     */
    CORPORATE_RENTAL
}
