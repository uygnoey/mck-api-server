package kr.mclub.apiserver.membership.domain;

import jakarta.persistence.*;
import kr.mclub.apiserver.shared.domain.BaseTimeEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 멤버십 기간 / Membership Period
 *
 * <p>정회원의 연회비 납부 기간 정보를 관리합니다.</p>
 * <p>가입비 + 최초 연회비 납부 시 첫 기간 생성, 이후 매년 연회비 납부 시 새 기간 추가</p>
 *
 * <h3>멤버십 기간 관리</h3>
 * <ul>
 *   <li>기간: 매년 1월 1일 ~ 12월 31일</li>
 *   <li>갱신 기한: 익년 1월 31일까지 (이월 기간)</li>
 *   <li>만료: 갱신 기한까지 연회비 미납 시 준회원으로 전환</li>
 * </ul>
 *
 * @since 1.0
 */
@Entity
@Table(name = "membership_periods", uniqueConstraints = {
        @UniqueConstraint(name = "uk_membership_period", columnNames = {"user_id", "year"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MembershipPeriod extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 회원 ID (User 테이블 참조)
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * 대상 연도
     * 예: 2025년 멤버십 → 2025
     */
    @Column(nullable = false)
    private Integer year;

    /**
     * 기간 시작일
     * 예: 2025-01-01
     */
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    /**
     * 기간 종료일
     * 예: 2025-12-31
     */
    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    /**
     * 갱신 마감일
     * 예: 2026-01-31 (익년 1월 말까지 이월 가능)
     */
    @Column(name = "renewal_deadline", nullable = false)
    private LocalDate renewalDeadline;

    /**
     * 활성 여부
     * true: 현재 유효한 기간
     * false: 만료된 기간
     */
    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    /**
     * 연회비 결제 기록 ID (PaymentRecord 테이블 참조)
     * 해당 연도의 연회비 납부 기록
     */
    @Column(name = "payment_record_id")
    private Long paymentRecordId;

    // === 생성자 ===

    /**
     * 멤버십 기간 생성
     *
     * @param userId 회원 ID
     * @param year 대상 연도
     * @param startDate 시작일
     * @param endDate 종료일
     * @param renewalDeadline 갱신 마감일
     * @param paymentRecordId 연회비 결제 기록 ID
     */
    public MembershipPeriod(
            Long userId,
            Integer year,
            LocalDate startDate,
            LocalDate endDate,
            LocalDate renewalDeadline,
            Long paymentRecordId
    ) {
        this.userId = userId;
        this.year = year;
        this.startDate = startDate;
        this.endDate = endDate;
        this.renewalDeadline = renewalDeadline;
        this.isActive = true;
        this.paymentRecordId = paymentRecordId;
    }

    // === 비즈니스 메서드 ===

    /**
     * 멤버십 만료 처리
     */
    public void expire() {
        this.isActive = false;
    }

    /**
     * 멤버십 활성화 (재가입 등)
     */
    public void activate() {
        this.isActive = true;
    }

    /**
     * 현재 유효한 기간인지 확인
     *
     * @param currentDate 기준 일자
     * @return 유효 여부
     */
    public boolean isValidOn(LocalDate currentDate) {
        return this.isActive &&
                !currentDate.isBefore(this.startDate) &&
                !currentDate.isAfter(this.renewalDeadline);
    }

    /**
     * 갱신 기한이 지났는지 확인
     *
     * @param currentDate 기준 일자
     * @return 갱신 기한 초과 여부
     */
    public boolean isRenewalOverdue(LocalDate currentDate) {
        return currentDate.isAfter(this.renewalDeadline);
    }
}
