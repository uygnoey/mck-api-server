package kr.mclub.apiserver.user.domain;

import java.time.LocalDate;
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
 * 회원 차량
 * Member vehicle entity (supports multiple vehicles per user)
 */
@Entity
@Table(name = "member_vehicles")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberVehicle extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "car_number", nullable = false, length = 20)
    private String carNumber;  // 차량번호

    @Column(name = "vin_number", nullable = false, unique = true, length = 50)
    private String vinNumber;  // 차대번호 (중복 불가)

    @Column(name = "car_model", nullable = false, length = 100)
    private String carModel;  // 차종 (예: M3, M4, M5 등)

    @Enumerated(EnumType.STRING)
    @Column(name = "ownership_type", nullable = false, length = 30)
    private VehicleOwnershipType ownershipType;  // 소유 유형

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private VehicleStatus status = VehicleStatus.ACTIVE;  // 상태

    @Column(name = "registered_at", nullable = false)
    private LocalDate registeredAt = LocalDate.now();  // 등록일

    @Column(name = "sold_at")
    private LocalDate soldAt;  // 매각일

    @Column(name = "grace_period_end_at")
    private LocalDate gracePeriodEndAt;  // 유예 종료일

    @Column(name = "is_primary", nullable = false)
    private boolean isPrimary = false;  // 대표 차량 여부

    @Builder
    public MemberVehicle(Long userId, String carNumber, String vinNumber, String carModel,
                         VehicleOwnershipType ownershipType, VehicleStatus status, LocalDate registeredAt) {
        this.userId = userId;
        this.carNumber = carNumber;
        this.vinNumber = vinNumber;
        this.carModel = carModel;
        this.ownershipType = ownershipType;
        this.status = status != null ? status : VehicleStatus.ACTIVE;
        this.registeredAt = registeredAt != null ? registeredAt : LocalDate.now();
    }

    /**
     * 대표 차량으로 설정
     * Set as primary vehicle
     */
    public void setAsPrimary() {
        this.isPrimary = true;
    }

    /**
     * 대표 차량 해제
     * Unset as primary vehicle
     */
    public void unsetPrimary() {
        this.isPrimary = false;
    }

    /**
     * 차량 매각 처리 (6개월 유예 기간)
     * Mark vehicle as sold with 6-month grace period
     */
    public void markAsSold(LocalDate soldAt) {
        this.soldAt = soldAt;
        this.status = VehicleStatus.GRACE_PERIOD;
        this.gracePeriodEndAt = soldAt.plusMonths(6);
        this.isPrimary = false;
    }

    /**
     * 유예 기간 만료 확인
     * Check if grace period is expired
     */
    public boolean isGracePeriodExpired() {
        if (status != VehicleStatus.GRACE_PERIOD) {
            return false;
        }
        return gracePeriodEndAt != null && gracePeriodEndAt.isBefore(LocalDate.now());
    }

    /**
     * 차량 정보 업데이트
     * Update vehicle information
     */
    public void updateInfo(String carNumber, String carModel) {
        this.carNumber = carNumber;
        this.carModel = carModel;
    }

    /**
     * 차량 상태 변경
     * Change vehicle status
     */
    public void changeStatus(VehicleStatus newStatus) {
        this.status = newStatus;
    }
}
