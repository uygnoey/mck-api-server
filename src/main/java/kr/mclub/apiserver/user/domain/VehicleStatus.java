package kr.mclub.apiserver.user.domain;

/**
 * 차량 상태 Enum
 * Vehicle status enumeration
 */
public enum VehicleStatus {
    ACTIVE,         // 현재 소유 중
    SOLD,           // 매각
    GRACE_PERIOD    // 유예 기간
}
