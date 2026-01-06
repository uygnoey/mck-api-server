package kr.mclub.apiserver.membership.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
 * 연회비 설정 엔티티
 * Annual fee configuration entity
 */
@Entity
@Table(name = "annual_fee_configs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AnnualFeeConfig extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 대상 년도
    @Column(name = "target_year", nullable = false, unique = true)
    private Integer targetYear;  // 대상 년도 (예: 2025)

    // 이월 정책
    @Column(name = "carry_over_deadline", nullable = false)
    private LocalDate carryOverDeadline;  // 이월 마감일 (예: 2025-01-15)

    // 갱신 기간
    @Column(name = "renewal_start_date", nullable = false)
    private LocalDate renewalStartDate;  // 갱신 시작일 (예: 2025-01-01)

    @Column(name = "renewal_deadline", nullable = false)
    private LocalDate renewalDeadline;  // 갱신 마감일 (예: 2025-01-31)

    // 금액
    @Column(name = "enrollment_fee_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal enrollmentFeeAmount = new BigDecimal("200000");  // 입회비 (기본 20만원)

    @Column(name = "annual_fee_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal annualFeeAmount = new BigDecimal("200000");  // 연회비 (기본 20만원)

    // 설정 관리
    @Column(name = "configured_by", nullable = false)
    private Long configuredBy;  // 설정한 임원 ID

    @Column(name = "configured_at", nullable = false)
    private LocalDateTime configuredAt = LocalDateTime.now();

    @Column(length = 500)
    private String notes;  // 비고 (예: "설 연휴로 마감일 연장")

    @Builder
    public AnnualFeeConfig(Integer targetYear, LocalDate carryOverDeadline,
                           LocalDate renewalStartDate, LocalDate renewalDeadline,
                           BigDecimal enrollmentFeeAmount, BigDecimal annualFeeAmount,
                           Long configuredBy, String notes) {
        this.targetYear = targetYear;
        this.carryOverDeadline = carryOverDeadline;
        this.renewalStartDate = renewalStartDate;
        this.renewalDeadline = renewalDeadline;
        this.enrollmentFeeAmount = enrollmentFeeAmount != null ? enrollmentFeeAmount : new BigDecimal("200000");
        this.annualFeeAmount = annualFeeAmount != null ? annualFeeAmount : new BigDecimal("200000");
        this.configuredBy = configuredBy;
        this.configuredAt = LocalDateTime.now();
        this.notes = notes;
    }

    /**
     * 설정 업데이트
     * Update configuration
     */
    public void update(LocalDate carryOverDeadline, LocalDate renewalStartDate,
                       LocalDate renewalDeadline, BigDecimal enrollmentFeeAmount,
                       BigDecimal annualFeeAmount, String notes) {
        this.carryOverDeadline = carryOverDeadline;
        this.renewalStartDate = renewalStartDate;
        this.renewalDeadline = renewalDeadline;
        this.enrollmentFeeAmount = enrollmentFeeAmount;
        this.annualFeeAmount = annualFeeAmount;
        this.notes = notes;
    }

    /**
     * 이월 기간 여부 확인
     * Check if date is in carry-over period
     */
    public boolean isCarryOverPeriod(LocalDate date) {
        // 예: 2025-01-01 ~ 2025-01-15 사이는 2024년 연회비로 인정
        return !date.isBefore(renewalStartDate) && !date.isAfter(carryOverDeadline);
    }

    /**
     * 갱신 기간 여부 확인
     * Check if date is in renewal period
     */
    public boolean isRenewalPeriod(LocalDate date) {
        return !date.isBefore(renewalStartDate) && !date.isAfter(renewalDeadline);
    }

    /**
     * 갱신 마감일 경과 여부 확인
     * Check if renewal deadline has passed
     */
    public boolean isRenewalOverdue(LocalDate date) {
        return date.isAfter(renewalDeadline);
    }
}
