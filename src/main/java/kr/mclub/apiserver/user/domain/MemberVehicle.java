package kr.mclub.apiserver.user.domain;

import jakarta.persistence.*;
import kr.mclub.apiserver.shared.domain.BaseTimeEntity;
import kr.mclub.apiserver.shared.domain.CommonCode;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

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

    @Column(name = "license_plate", nullable = false, length = 20)
    private String licensePlate;  // 차량번호

    @Column(name = "vin_number", nullable = false, unique = true, length = 50)
    private String vinNumber;  // 차대번호 (중복 불가)

    @Column(name = "model_name", nullable = false, length = 100)
    private String modelName;  // 차종 (예: M3, M4, M5 등)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ownership_type_code_id", nullable = false)
    private CommonCode ownershipType;  // 소유 유형 (CommonCode)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "status_code_id", nullable = false)
    private CommonCode status;  // 상태 (CommonCode)

    @Column(name = "registered_at", nullable = false)
    private LocalDate registeredAt;  // 등록일

    @Column(name = "sold_at")
    private LocalDate soldAt;  // 매각일

    @Column(name = "grace_period_end_at")
    private LocalDate gracePeriodEndAt;  // 유예 종료일

    @Column(name = "is_primary", nullable = false)
    private boolean isPrimary = false;  // 대표 차량 여부

    @Builder
    public MemberVehicle(Long userId, String licensePlate, String vinNumber, String modelName,
                         CommonCode ownershipType, CommonCode status, LocalDate registeredAt) {
        this.userId = userId;
        this.licensePlate = licensePlate;
        this.vinNumber = vinNumber;
        this.modelName = modelName;
        this.ownershipType = ownershipType;
        this.status = status;
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
    public void markAsSold(LocalDate soldAt, CommonCode gracePeriodStatus) {
        this.soldAt = soldAt;
        this.status = gracePeriodStatus;
        this.gracePeriodEndAt = soldAt.plusMonths(6);
        this.isPrimary = false;
    }

    /**
     * 유예 기간 만료 확인
     * Check if grace period is expired
     */
    public boolean isGracePeriodExpired() {
        if (status == null || !"GRACE_PERIOD".equals(status.getCode())) {
            return false;
        }
        return gracePeriodEndAt != null && gracePeriodEndAt.isBefore(LocalDate.now());
    }

    /**
     * 차량 정보 업데이트
     * Update vehicle information
     */
    public void updateInfo(String licensePlate, String modelName) {
        this.licensePlate = licensePlate;
        this.modelName = modelName;
    }

    /**
     * 차량 상태 변경
     * Change vehicle status
     */
    public void changeStatus(CommonCode newStatus) {
        this.status = newStatus;
    }
}
