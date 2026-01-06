package kr.mclub.apiserver.membership.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import kr.mclub.apiserver.membership.domain.ApplicationStatus;
import kr.mclub.apiserver.membership.domain.MembershipPeriodStatus;
import kr.mclub.apiserver.membership.domain.PaymentStatus;
import kr.mclub.apiserver.membership.domain.PaymentType;
import kr.mclub.apiserver.membership.repository.MembershipApplicationRepository;
import kr.mclub.apiserver.membership.repository.MembershipPeriodRepository;
import kr.mclub.apiserver.membership.repository.PaymentRecordRepository;

/**
 * 멤버십 통계 Service / Membership Statistics Service
 *
 * <p>정회원 가입, 결제, 갱신 등의 통계 정보를 제공합니다.</p>
 * <p>Provides statistics for membership applications, payments, renewals, etc.</p>
 *
 * @since 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MembershipStatisticsService {

    private final MembershipApplicationRepository applicationRepository;
    private final PaymentRecordRepository paymentRepository;
    private final MembershipPeriodRepository periodRepository;

    /**
     * 신청서 상태별 통계 / Get application statistics by status
     *
     * @return 상태별 신청서 개수
     */
    public Map<String, Long> getApplicationStatsByStatus() {
        log.info("신청서 상태별 통계 조회");

        Map<String, Long> stats = new HashMap<>();

        for (ApplicationStatus status : ApplicationStatus.values()) {
            long count = applicationRepository.countByStatus(status);
            stats.put(status.name(), count);
        }

        log.info("신청서 상태별 통계: {}", stats);
        return stats;
    }

    /**
     * 특정 년도 신청서 통계 / Get application statistics by year
     *
     * @param year 년도
     * @return 통계 정보
     */
    public Map<String, Object> getApplicationStatsByYear(Integer year) {
        log.info("{}년도 신청서 통계 조회", year);

        LocalDate startDate = LocalDate.of(year, 1, 1).atStartOfDay().toLocalDate();
        LocalDate endDate = LocalDate.of(year, 12, 31).atTime(23, 59, 59).toLocalDate();

        Map<String, Object> stats = new HashMap<>();

        // 전체 신청서 수
        long totalApplications = applicationRepository.countByCreatedAtBetween(
                startDate.atStartOfDay(), endDate.atTime(23, 59, 59));
        stats.put("totalApplications", totalApplications);

        // 승인된 신청서 수
        long approvedApplications = applicationRepository.countByStatusAndCreatedAtBetween(
                ApplicationStatus.COMPLETED, startDate.atStartOfDay(), endDate.atTime(23, 59, 59));
        stats.put("approvedApplications", approvedApplications);

        // 대기 중인 신청서 수
        long pendingApplications = applicationRepository.countByStatus(ApplicationStatus.DOCUMENT_PENDING) +
                                    applicationRepository.countByStatus(ApplicationStatus.DOCUMENT_APPROVED);
        stats.put("pendingApplications", pendingApplications);

        // 반려된 신청서 수
        long rejectedApplications = applicationRepository.countByStatus(ApplicationStatus.DOCUMENT_REJECTED);
        stats.put("rejectedApplications", rejectedApplications);

        log.info("{}년도 신청서 통계: {}", year, stats);
        return stats;
    }

    /**
     * 결제 통계 / Get payment statistics
     *
     * @return 결제 통계 정보
     */
    public Map<String, Object> getPaymentStats() {
        log.info("결제 통계 조회");

        Map<String, Object> stats = new HashMap<>();

        // 상태별 결제 개수
        for (PaymentStatus status : PaymentStatus.values()) {
            long count = paymentRepository.countByStatus(status);
            stats.put(status.name().toLowerCase() + "Count", count);
        }

        // 타입별 결제 개수
        for (PaymentType type : PaymentType.values()) {
            long count = paymentRepository.countByPaymentType(type);
            stats.put(type.name().toLowerCase() + "Count", count);
        }

        // 대기 중인 결제 금액 합계
        BigDecimal pendingAmount = paymentRepository.findByStatusOrderByCreatedAtDesc(PaymentStatus.PENDING)
                .stream()
                .map(payment -> payment.getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        stats.put("pendingAmount", pendingAmount);

        // 확인된 결제 금액 합계
        BigDecimal confirmedAmount = paymentRepository.findByStatusOrderByCreatedAtDesc(PaymentStatus.CONFIRMED)
                .stream()
                .map(payment -> payment.getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        stats.put("confirmedAmount", confirmedAmount);

        log.info("결제 통계: {}", stats);
        return stats;
    }

    /**
     * 특정 년도 결제 통계 / Get payment statistics by year
     *
     * @param year 년도
     * @return 년도별 결제 통계
     */
    public Map<String, Object> getPaymentStatsByYear(Integer year) {
        log.info("{}년도 결제 통계 조회", year);

        Map<String, Object> stats = new HashMap<>();

        // 년도별 결제 통계 (타입별 개수 및 합계)
        List<Object[]> yearStats = paymentRepository.findPaymentStatisticsByYear(year);

        BigDecimal enrollmentTotal = BigDecimal.ZERO;
        BigDecimal annualTotal = BigDecimal.ZERO;
        long enrollmentCount = 0;
        long annualCount = 0;

        for (Object[] row : yearStats) {
            PaymentType type = (PaymentType) row[0];
            long count = (Long) row[1];
            BigDecimal amount = (BigDecimal) row[2];

            if (type == PaymentType.ENROLLMENT_FEE) {
                enrollmentCount = count;
                enrollmentTotal = amount;
            } else if (type == PaymentType.ANNUAL_FEE) {
                annualCount = count;
                annualTotal = amount;
            }
        }

        stats.put("enrollmentFeeCount", enrollmentCount);
        stats.put("enrollmentFeeTotal", enrollmentTotal);
        stats.put("annualFeeCount", annualCount);
        stats.put("annualFeeTotal", annualTotal);
        stats.put("totalAmount", enrollmentTotal.add(annualTotal));

        log.info("{}년도 결제 통계: {}", year, stats);
        return stats;
    }

    /**
     * 멤버십 기간 통계 / Get membership period statistics
     *
     * @return 멤버십 기간 통계
     */
    public Map<String, Long> getMembershipPeriodStats() {
        log.info("멤버십 기간 통계 조회");

        Map<String, Long> stats = new HashMap<>();

        for (MembershipPeriodStatus status : MembershipPeriodStatus.values()) {
            long count = periodRepository.findAll().stream()
                    .filter(period -> period.getStatus() == status)
                    .count();
            stats.put(status.name().toLowerCase(), count);
        }

        log.info("멤버십 기간 통계: {}", stats);
        return stats;
    }

    /**
     * 특정 년도 멤버십 통계 / Get membership statistics by year
     *
     * @param year 년도
     * @return 년도별 멤버십 통계
     */
    public Map<String, Object> getMembershipStatsByYear(Integer year) {
        log.info("{}년도 멤버십 통계 조회", year);

        Map<String, Object> stats = new HashMap<>();

        // 해당 년도 활성 멤버십 개수
        long activeCount = periodRepository.countByStartYearAndStatus(year, MembershipPeriodStatus.ACTIVE);
        stats.put("activeMemberships", activeCount);

        // 해당 년도 만료된 멤버십 개수
        long expiredCount = periodRepository.countByStartYearAndStatus(year, MembershipPeriodStatus.EXPIRED);
        stats.put("expiredMemberships", expiredCount);

        // 해당 년도 취소된 멤버십 개수
        long cancelledCount = periodRepository.countByStartYearAndStatus(year, MembershipPeriodStatus.CANCELLED);
        stats.put("cancelledMemberships", cancelledCount);

        // 총 멤버십 개수
        stats.put("totalMemberships", activeCount + expiredCount + cancelledCount);

        // 갱신률 계산 (이전 년도 대비)
        if (year > 2020) {  // 최소 년도 체크
            long previousYearActive = periodRepository.countByStartYearAndStatus(
                    year - 1, MembershipPeriodStatus.ACTIVE);
            long previousYearExpired = periodRepository.countByStartYearAndStatus(
                    year - 1, MembershipPeriodStatus.EXPIRED);
            long previousYearTotal = previousYearActive + previousYearExpired;

            if (previousYearTotal > 0) {
                double renewalRate = (double) activeCount / previousYearTotal * 100;
                stats.put("renewalRate", Math.round(renewalRate * 100.0) / 100.0);
            }
        }

        log.info("{}년도 멤버십 통계: {}", year, stats);
        return stats;
    }

    /**
     * 전체 통계 요약 / Get overall statistics summary
     *
     * @return 전체 통계 요약
     */
    public Map<String, Object> getOverallStatistics() {
        log.info("전체 통계 요약 조회");

        Map<String, Object> stats = new HashMap<>();

        // 신청서 통계
        stats.put("applicationStats", getApplicationStatsByStatus());

        // 결제 통계
        stats.put("paymentStats", getPaymentStats());

        // 멤버십 기간 통계
        stats.put("membershipPeriodStats", getMembershipPeriodStats());

        // 현재 년도 통계
        Integer currentYear = LocalDate.now().getYear();
        stats.put("currentYearApplicationStats", getApplicationStatsByYear(currentYear));
        stats.put("currentYearPaymentStats", getPaymentStatsByYear(currentYear));
        stats.put("currentYearMembershipStats", getMembershipStatsByYear(currentYear));

        log.info("전체 통계 요약 조회 완료");
        return stats;
    }

    /**
     * 월별 신청서 추이 / Get monthly application trends
     *
     * @param year 년도
     * @return 월별 신청서 개수
     */
    public Map<Integer, Long> getMonthlyApplicationTrends(Integer year) {
        log.info("{}년도 월별 신청서 추이 조회", year);

        Map<Integer, Long> trends = new HashMap<>();

        for (int month = 1; month <= 12; month++) {
            LocalDate startDate = LocalDate.of(year, month, 1);
            LocalDate endDate = startDate.plusMonths(1).minusDays(1);

            long count = applicationRepository.countByCreatedAtBetween(
                    startDate.atStartOfDay(), endDate.atTime(23, 59, 59));
            trends.put(month, count);
        }

        log.info("{}년도 월별 신청서 추이: {}", year, trends);
        return trends;
    }

    /**
     * 월별 결제 추이 / Get monthly payment trends
     *
     * @param year 년도
     * @return 월별 결제 금액
     */
    public Map<Integer, BigDecimal> getMonthlyPaymentTrends(Integer year) {
        log.info("{}년도 월별 결제 추이 조회", year);

        Map<Integer, BigDecimal> trends = new HashMap<>();

        for (int month = 1; month <= 12; month++) {
            LocalDate startDate = LocalDate.of(year, month, 1);
            LocalDate endDate = startDate.plusMonths(1).minusDays(1);

            BigDecimal monthlyTotal = paymentRepository.findByStatusOrderByCreatedAtDesc(PaymentStatus.CONFIRMED)
                    .stream()
                    .filter(payment -> {
                        LocalDate depositDate = payment.getDepositDate();
                        return depositDate != null &&
                               !depositDate.isBefore(startDate) &&
                               !depositDate.isAfter(endDate);
                    })
                    .map(payment -> payment.getAmount())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            trends.put(month, monthlyTotal);
        }

        log.info("{}년도 월별 결제 추이: {}", year, trends);
        return trends;
    }
}
