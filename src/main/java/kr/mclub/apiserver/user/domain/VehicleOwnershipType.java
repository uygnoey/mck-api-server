package kr.mclub.apiserver.user.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 차량 소유 유형
 * Vehicle ownership type
 */
@Getter
@RequiredArgsConstructor
public enum VehicleOwnershipType {

    PERSONAL("개인"),             // 개인 소유
    CORPORATE("법인"),            // 법인 소유
    LEASE("리스"),                // 개인 리스
    RENTAL("렌트"),               // 개인 렌트
    CORPORATE_LEASE("법인리스"),   // 법인 리스
    CORPORATE_RENTAL("법인렌트");  // 법인 렌트

    private final String description;

    /**
     * 법인/법인리스/법인렌트인지 확인
     * Check if corporate ownership
     */
    public boolean isCorporate() {
        return this == CORPORATE || this == CORPORATE_LEASE || this == CORPORATE_RENTAL;
    }

    /**
     * 리스/렌트인지 확인
     * Check if lease or rental
     */
    public boolean isLeaseOrRental() {
        return this == LEASE || this == RENTAL || this == CORPORATE_LEASE || this == CORPORATE_RENTAL;
    }
}
