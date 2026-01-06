package kr.mclub.apiserver.membership.domain;

/**
 * 신청 서류 유형
 * Application document type
 */
public enum DocumentType {

    /**
     * 차량등록증
     * Vehicle registration certificate
     */
    VEHICLE_REGISTRATION,

    /**
     * 신분증
     * ID card
     */
    ID_CARD,

    /**
     * 사업자등록증
     * Business license
     */
    BUSINESS_LICENSE,

    /**
     * 재직증명서
     * Employment certificate
     */
    EMPLOYMENT_CERTIFICATE,

    /**
     * 리스 계약서
     * Lease contract
     */
    LEASE_CONTRACT,

    /**
     * 렌트 계약서
     * Rental contract
     */
    RENTAL_CONTRACT
}
