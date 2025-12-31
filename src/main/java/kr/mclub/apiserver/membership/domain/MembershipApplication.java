package kr.mclub.apiserver.membership.domain;

import jakarta.persistence.*;
import kr.mclub.apiserver.shared.domain.BaseTimeEntity;
import kr.mclub.apiserver.shared.domain.CommonCode;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 정회원 가입 신청서 / Membership Application
 *
 * <p>준회원이 정회원으로 승급하기 위한 신청서 정보를 관리합니다.</p>
 *
 * <h3>신청 프로세스</h3>
 * <ol>
 *   <li>신청서 작성 및 제출 (PENDING_DOCUMENTS)</li>
 *   <li>서류 업로드 완료 (PENDING_REVIEW)</li>
 *   <li>관리자 심사 시작 (UNDER_REVIEW)</li>
 *   <li>서류 승인 후 입금 대기 (PENDING_PAYMENT)</li>
 *   <li>입금 확인 및 최종 승인 (APPROVED)</li>
 * </ol>
 *
 * @since 1.0
 */
@Entity
@Table(name = "membership_applications")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MembershipApplication extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 신청자 ID (User 테이블 참조)
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * 심사 상태 (CommonCode: VERIFICATION_STATUS)
     * - PENDING_DOCUMENTS: 서류 제출 대기
     * - PENDING_REVIEW: 심사 대기
     * - UNDER_REVIEW: 심사 중
     * - PENDING_PAYMENT: 입금 대기
     * - APPROVED: 승인 완료
     * - REJECTED: 반려
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "status_code_id", nullable = false)
    private CommonCode status;

    /**
     * 신청자 실명
     * User 테이블의 realName과 일치해야 함
     */
    @Column(name = "real_name", nullable = false, length = 50)
    private String realName;

    /**
     * 신청자 전화번호
     * User 테이블의 phoneNumber와 일치해야 함
     */
    @Column(name = "phone_number", nullable = false, length = 20)
    private String phoneNumber;

    /**
     * 차량 번호
     * 예: 12가3456
     */
    @Column(name = "car_number", nullable = false, length = 20)
    private String carNumber;

    /**
     * 차대번호 (VIN)
     * 차량 고유 식별 번호
     */
    @Column(name = "vin_number", nullable = false, length = 50)
    private String vinNumber;

    /**
     * 차량 소유 형태 (CommonCode: VEHICLE_OWNERSHIP_TYPE)
     * - PERSONAL: 개인
     * - CORPORATE: 법인
     * - LEASE: 리스
     * - RENTAL: 렌트
     * - CORPORATE_LEASE: 법인 리스
     * - CORPORATE_RENTAL: 법인 렌트
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ownership_type_code_id", nullable = false)
    private CommonCode ownershipType;

    /**
     * 반려 사유
     * 심사 반려 시 관리자가 작성
     */
    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;

    /**
     * 심사자 ID (User 테이블 참조)
     * 신청서를 승인/반려한 관리자
     */
    @Column(name = "reviewed_by_admin_id")
    private Long reviewedByAdminId;

    /**
     * 심사 완료 일시
     * 승인 또는 반려된 시각
     */
    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    // === 생성자 ===

    /**
     * 정회원 신청서 생성
     *
     * @param userId 신청자 ID
     * @param initialStatus 초기 상태 (PENDING_DOCUMENTS)
     * @param realName 실명
     * @param phoneNumber 전화번호
     * @param carNumber 차량 번호
     * @param vinNumber 차대번호
     * @param ownershipType 차량 소유 형태
     */
    public MembershipApplication(
            Long userId,
            CommonCode initialStatus,
            String realName,
            String phoneNumber,
            String carNumber,
            String vinNumber,
            CommonCode ownershipType
    ) {
        this.userId = userId;
        this.status = initialStatus;
        this.realName = realName;
        this.phoneNumber = phoneNumber;
        this.carNumber = carNumber;
        this.vinNumber = vinNumber;
        this.ownershipType = ownershipType;
    }

    // === 비즈니스 메서드 ===

    /**
     * 심사 상태 변경
     *
     * @param newStatus 새로운 상태
     */
    public void updateStatus(CommonCode newStatus) {
        this.status = newStatus;
    }

    /**
     * 신청 승인
     *
     * @param adminId 승인한 관리자 ID
     */
    public void approve(Long adminId) {
        this.reviewedByAdminId = adminId;
        this.reviewedAt = LocalDateTime.now();
        // status는 별도로 updateStatus()로 변경
    }

    /**
     * 신청 반려
     *
     * @param reason 반려 사유
     * @param adminId 반려한 관리자 ID
     */
    public void reject(String reason, Long adminId) {
        this.rejectionReason = reason;
        this.reviewedByAdminId = adminId;
        this.reviewedAt = LocalDateTime.now();
        // status는 별도로 updateStatus()로 변경
    }

    /**
     * 차량 소유 형태 변경
     * 서류 제출 전에만 가능
     *
     * @param newOwnershipType 새로운 소유 형태
     */
    public void updateOwnershipType(CommonCode newOwnershipType) {
        this.ownershipType = newOwnershipType;
    }
}
