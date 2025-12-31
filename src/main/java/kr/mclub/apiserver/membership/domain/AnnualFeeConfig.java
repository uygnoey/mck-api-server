package kr.mclub.apiserver.membership.domain;

import jakarta.persistence.*;
import kr.mclub.apiserver.shared.domain.BaseTimeEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 연회비 설정 / Annual Fee Configuration
 *
 * <p>연도별 연회비 금액 및 갱신 일정을 관리합니다.</p>
 *
 * <h3>연회비 갱신 일정</h3>
 * <ul>
 *   <li>이월 마감일: 전년도 연회비를 당해 연도로 인정하는 마지막 날 (예: 1월 15일)</li>
 *   <li>갱신 시작일: 당해 연도 연회비 납부 시작일 (예: 1월 1일)</li>
 *   <li>갱신 마감일: 당해 연도 연회비 납부 마감일 (예: 익년 1월 31일)</li>
 * </ul>
 *
 * @since 1.0
 */
@Entity
@Table(name = "annual_fee_configs", uniqueConstraints = {
        @UniqueConstraint(name = "uk_annual_fee_year", columnNames = {"target_year"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AnnualFeeConfig extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 대상 연도
     * 예: 2025년 연회비 설정 → 2025
     */
    @Column(name = "target_year", nullable = false, unique = true)
    private Integer targetYear;

    /**
     * 이월 마감일
     * 전년도 연회비를 당해 연도로 인정하는 마지막 날
     * 예: 2025-01-15 (1월 15일까지 2024년 연회비로 인정)
     */
    @Column(name = "carry_over_deadline", nullable = false)
    private LocalDate carryOverDeadline;

    /**
     * 갱신 시작일
     * 당해 연도 연회비 납부 시작일
     * 예: 2025-01-01
     */
    @Column(name = "renewal_start_date", nullable = false)
    private LocalDate renewalStartDate;

    /**
     * 갱신 마감일
     * 당해 연도 연회비 납부 마감일
     * 예: 2026-01-31 (익년 1월 말까지)
     */
    @Column(name = "renewal_deadline", nullable = false)
    private LocalDate renewalDeadline;

    /**
     * 연회비 금액 (원)
     * 기본값: 200,000원
     * 총회 결의로 변경 가능
     */
    @Column(name = "annual_fee_amount", nullable = false, precision = 10, scale = 0)
    private BigDecimal annualFeeAmount;

    /**
     * 설정한 관리자 ID (User 테이블 참조)
     */
    @Column(name = "configured_by_admin_id", nullable = false)
    private Long configuredByAdminId;

    /**
     * 설정 일시
     */
    @Column(name = "configured_at", nullable = false)
    private LocalDateTime configuredAt;

    /**
     * 비고
     * 연회비 금액 변경 사유 등
     */
    @Column(length = 500)
    private String notes;

    // === 생성자 ===

    /**
     * 연회비 설정 생성
     *
     * @param targetYear 대상 연도
     * @param carryOverDeadline 이월 마감일
     * @param renewalStartDate 갱신 시작일
     * @param renewalDeadline 갱신 마감일
     * @param annualFeeAmount 연회비 금액
     * @param adminId 설정한 관리자 ID
     * @param notes 비고
     */
    public AnnualFeeConfig(
            Integer targetYear,
            LocalDate carryOverDeadline,
            LocalDate renewalStartDate,
            LocalDate renewalDeadline,
            BigDecimal annualFeeAmount,
            Long adminId,
            String notes
    ) {
        this.targetYear = targetYear;
        this.carryOverDeadline = carryOverDeadline;
        this.renewalStartDate = renewalStartDate;
        this.renewalDeadline = renewalDeadline;
        this.annualFeeAmount = annualFeeAmount;
        this.configuredByAdminId = adminId;
        this.configuredAt = LocalDateTime.now();
        this.notes = notes;
    }

    // === 비즈니스 메서드 ===

    /**
     * 연회비 설정 수정
     *
     * @param carryOverDeadline 이월 마감일
     * @param renewalStartDate 갱신 시작일
     * @param renewalDeadline 갱신 마감일
     * @param annualFeeAmount 연회비 금액
     * @param notes 비고
     */
    public void update(
            LocalDate carryOverDeadline,
            LocalDate renewalStartDate,
            LocalDate renewalDeadline,
            BigDecimal annualFeeAmount,
            String notes
    ) {
        this.carryOverDeadline = carryOverDeadline;
        this.renewalStartDate = renewalStartDate;
        this.renewalDeadline = renewalDeadline;
        this.annualFeeAmount = annualFeeAmount;
        this.notes = notes;
    }

    /**
     * 특정 날짜가 이월 기간에 해당하는지 확인
     *
     * @param paymentDate 납부 일자
     * @return 이월 기간 여부 (전년도 연회비로 인정)
     */
    public boolean isCarryOverPeriod(LocalDate paymentDate) {
        // 예: 2025-01-01 ~ 2025-01-15 사이는 2024년 연회비로 인정
        return !paymentDate.isBefore(this.renewalStartDate) &&
                !paymentDate.isAfter(this.carryOverDeadline);
    }
}
