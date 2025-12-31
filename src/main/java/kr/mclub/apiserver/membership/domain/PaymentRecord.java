package kr.mclub.apiserver.membership.domain;

import jakarta.persistence.*;
import kr.mclub.apiserver.shared.domain.BaseTimeEntity;
import kr.mclub.apiserver.shared.domain.CommonCode;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 입금 기록 / Payment Record
 *
 * <p>가입비 및 연회비 입금 내역을 관리합니다.</p>
 *
 * <h3>결제 유형 (PaymentType)</h3>
 * <ul>
 *   <li>ENROLLMENT_FEE - 가입비 (최초 1회, 기본 200,000원)</li>
 *   <li>ANNUAL_FEE - 연회비 (매년 갱신, 기본 200,000원)</li>
 * </ul>
 *
 * <h3>결제 상태 (PaymentStatus)</h3>
 * <ul>
 *   <li>PENDING - 입금 대기</li>
 *   <li>CONFIRMED - 입금 확인 완료</li>
 *   <li>CANCELLED - 취소됨 (환불 등)</li>
 * </ul>
 *
 * @since 1.0
 */
@Entity
@Table(name = "payment_records")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentRecord extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 납부자 ID (User 테이블 참조)
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * 결제 유형 (CommonCode: PAYMENT_TYPE)
     * - ENROLLMENT_FEE: 가입비
     * - ANNUAL_FEE: 연회비
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_type_code_id", nullable = false)
    private CommonCode paymentType;

    /**
     * 납부 금액 (원)
     */
    @Column(nullable = false, precision = 10, scale = 0)
    private BigDecimal amount;

    /**
     * 입금자명
     * 계좌 이체 시 입금자 이름
     */
    @Column(name = "depositor_name", nullable = false, length = 50)
    private String depositorName;

    /**
     * 입금 일자
     */
    @Column(name = "deposit_date", nullable = false)
    private LocalDate depositDate;

    /**
     * 결제 상태 (CommonCode: PAYMENT_STATUS)
     * - PENDING: 입금 대기
     * - CONFIRMED: 입금 확인 완료
     * - CANCELLED: 취소됨
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_status_code_id", nullable = false)
    private CommonCode status;

    /**
     * 확인한 관리자 ID (User 테이블 참조)
     * 입금을 확인한 관리자
     */
    @Column(name = "confirmed_by_admin_id")
    private Long confirmedByAdminId;

    /**
     * 확인 일시
     * 입금이 확인된 시각
     */
    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;

    /**
     * 자동 확인 여부
     * true: 오픈뱅킹 API로 자동 확인
     * false: 관리자가 수동 확인
     */
    @Column(name = "auto_confirmed", nullable = false)
    private Boolean autoConfirmed;

    /**
     * 은행 거래 ID
     * 오픈뱅킹 API 사용 시 거래 고유 번호
     */
    @Column(name = "bank_transaction_id", length = 100)
    private String bankTransactionId;

    /**
     * 연회비 대상 연도
     * 연회비인 경우에만 사용
     * 예: 2025년 연회비 → 2025
     */
    @Column(name = "target_year")
    private Integer targetYear;

    // === 생성자 ===

    /**
     * 입금 기록 생성 (수동 등록)
     *
     * @param userId 납부자 ID
     * @param paymentType 결제 유형
     * @param amount 납부 금액
     * @param depositorName 입금자명
     * @param depositDate 입금 일자
     * @param initialStatus 초기 상태 (PENDING)
     */
    public PaymentRecord(
            Long userId,
            CommonCode paymentType,
            BigDecimal amount,
            String depositorName,
            LocalDate depositDate,
            CommonCode initialStatus
    ) {
        this.userId = userId;
        this.paymentType = paymentType;
        this.amount = amount;
        this.depositorName = depositorName;
        this.depositDate = depositDate;
        this.status = initialStatus;
        this.autoConfirmed = false;
    }

    /**
     * 입금 기록 생성 (오픈뱅킹 자동 확인)
     *
     * @param userId 납부자 ID
     * @param paymentType 결제 유형
     * @param amount 납부 금액
     * @param depositorName 입금자명
     * @param depositDate 입금 일자
     * @param confirmedStatus 확인 완료 상태 (CONFIRMED)
     * @param bankTransactionId 은행 거래 ID
     */
    public PaymentRecord(
            Long userId,
            CommonCode paymentType,
            BigDecimal amount,
            String depositorName,
            LocalDate depositDate,
            CommonCode confirmedStatus,
            String bankTransactionId
    ) {
        this.userId = userId;
        this.paymentType = paymentType;
        this.amount = amount;
        this.depositorName = depositorName;
        this.depositDate = depositDate;
        this.status = confirmedStatus;
        this.autoConfirmed = true;
        this.bankTransactionId = bankTransactionId;
        this.confirmedAt = LocalDateTime.now();
    }

    // === 비즈니스 메서드 ===

    /**
     * 입금 확인 (관리자 수동 확인)
     *
     * @param adminId 확인한 관리자 ID
     * @param confirmedStatus 확인 완료 상태 (CONFIRMED)
     */
    public void confirm(Long adminId, CommonCode confirmedStatus) {
        this.status = confirmedStatus;
        this.confirmedByAdminId = adminId;
        this.confirmedAt = LocalDateTime.now();
    }

    /**
     * 입금 취소
     *
     * @param cancelledStatus 취소 상태 (CANCELLED)
     */
    public void cancel(CommonCode cancelledStatus) {
        this.status = cancelledStatus;
    }

    /**
     * 연회비 대상 연도 설정
     *
     * @param year 대상 연도
     */
    public void setTargetYear(Integer year) {
        this.targetYear = year;
    }
}
