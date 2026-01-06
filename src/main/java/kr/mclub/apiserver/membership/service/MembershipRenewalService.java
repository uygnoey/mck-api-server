package kr.mclub.apiserver.membership.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import kr.mclub.apiserver.membership.domain.MembershipPeriod;
import kr.mclub.apiserver.membership.domain.MembershipPeriodStatus;
import kr.mclub.apiserver.membership.domain.PaymentRecord;
import kr.mclub.apiserver.membership.domain.PaymentStatus;
import kr.mclub.apiserver.membership.domain.PaymentType;
import kr.mclub.apiserver.membership.repository.MembershipPeriodRepository;
import kr.mclub.apiserver.membership.repository.PaymentRecordRepository;
import kr.mclub.apiserver.shared.exception.BusinessException;
import kr.mclub.apiserver.shared.exception.ErrorCode;

/**
 * 멤버십 갱신 Service / Membership Renewal Service
 *
 * <p>정회원 멤버십 기간 생성 및 갱신을 관리합니다.</p>
 * <p>Manages regular membership period creation and renewal.</p>
 *
 * @since 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MembershipRenewalService {

    private final MembershipPeriodRepository periodRepository;
    private final PaymentRecordRepository paymentRepository;

    /**
     * 초기 멤버십 기간 생성 (가입비 납부 확인 후)
     * Create initial membership period after enrollment fee payment
     *
     * @param userId 사용자 ID
     * @param paymentId 가입비 결제 ID
     * @return 생성된 멤버십 기간
     * @throws BusinessException 결제를 찾을 수 없거나 이미 기간이 존재하는 경우
     */
    @Transactional
    public MembershipPeriod createInitialPeriod(Long userId, Long paymentId) {
        log.info("초기 멤버십 기간 생성 시작: userId={}, paymentId={}", userId, paymentId);

        // 결제 확인
        PaymentRecord payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND));

        if (payment.getStatus() != PaymentStatus.CONFIRMED) {
            throw new BusinessException(ErrorCode.INVALID_PAYMENT_STATUS,
                    "확인된 결제만 멤버십 기간을 생성할 수 있습니다");
        }

        if (payment.getPaymentType() != PaymentType.ENROLLMENT_FEE) {
            throw new BusinessException(ErrorCode.INVALID_PAYMENT_STATUS,
                    "가입비 결제만 초기 멤버십 기간을 생성할 수 있습니다");
        }

        Integer targetYear = payment.getTargetYear();
        if (targetYear == null) {
            targetYear = LocalDate.now().getYear();
        }

        // 이미 해당 년도 멤버십이 있는지 확인
        if (periodRepository.existsByUserIdAndStartYear(userId, targetYear)) {
            throw new BusinessException(ErrorCode.MEMBERSHIP_ALREADY_RENEWED,
                    String.format("%d년도 멤버십이 이미 존재합니다", targetYear));
        }

        // 멤버십 기간 생성
        MembershipPeriod period = MembershipPeriod.builder()
                .userId(userId)
                .startYear(targetYear)
                .endYear(targetYear)
                .build();

        MembershipPeriod savedPeriod = periodRepository.save(period);
        log.info("초기 멤버십 기간 생성 완료: periodId={}, year={}", savedPeriod.getId(), targetYear);

        return savedPeriod;
    }

    /**
     * 멤버십 갱신 (연회비 납부 확인 후)
     * Renew membership after annual fee payment
     *
     * @param userId 사용자 ID
     * @param paymentId 연회비 결제 ID
     * @return 갱신된 멤버십 기간
     * @throws BusinessException 결제를 찾을 수 없거나 이미 갱신된 경우
     */
    @Transactional
    public MembershipPeriod renewMembership(Long userId, Long paymentId) {
        log.info("멤버십 갱신 시작: userId={}, paymentId={}", userId, paymentId);

        // 결제 확인
        PaymentRecord payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND));

        if (payment.getStatus() != PaymentStatus.CONFIRMED) {
            throw new BusinessException(ErrorCode.INVALID_PAYMENT_STATUS,
                    "확인된 결제만 멤버십을 갱신할 수 있습니다");
        }

        if (payment.getPaymentType() != PaymentType.ANNUAL_FEE) {
            throw new BusinessException(ErrorCode.INVALID_PAYMENT_STATUS,
                    "연회비 결제만 멤버십을 갱신할 수 있습니다");
        }

        Integer targetYear = payment.getTargetYear();
        if (targetYear == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT,
                    "연회비 결제에는 대상 년도가 필요합니다");
        }

        // 이미 해당 년도 멤버십이 있는지 확인
        if (periodRepository.existsByUserIdAndStartYear(userId, targetYear)) {
            throw new BusinessException(ErrorCode.MEMBERSHIP_ALREADY_RENEWED,
                    String.format("%d년도 멤버십이 이미 존재합니다", targetYear));
        }

        // 멤버십 기간 생성
        MembershipPeriod period = MembershipPeriod.builder()
                .userId(userId)
                .startYear(targetYear)
                .endYear(targetYear)
                .build();

        // 갱신 정보 설정
        period.renew(paymentId);

        MembershipPeriod savedPeriod = periodRepository.save(period);
        log.info("멤버십 갱신 완료: periodId={}, year={}", savedPeriod.getId(), targetYear);

        return savedPeriod;
    }

    /**
     * 사용자의 활성 멤버십 기간 조회
     * Get user's active membership period
     *
     * @param userId 사용자 ID
     * @return 활성 멤버십 기간 (없으면 null)
     */
    public MembershipPeriod getActivePeriod(Long userId) {
        return periodRepository.findByUserIdAndStatus(userId, MembershipPeriodStatus.ACTIVE)
                .orElse(null);
    }

    /**
     * 사용자의 특정 년도 멤버십 기간 조회
     * Get user's membership period by year
     *
     * @param userId 사용자 ID
     * @param year 년도
     * @return 멤버십 기간
     * @throws BusinessException 멤버십 기간을 찾을 수 없는 경우
     */
    public MembershipPeriod getPeriodByYear(Long userId, Integer year) {
        return periodRepository.findByUserIdAndStartYear(userId, year)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBERSHIP_PERIOD_NOT_FOUND,
                        String.format("%d년도 멤버십을 찾을 수 없습니다", year)));
    }

    /**
     * 사용자의 모든 멤버십 기간 조회
     * Get all user's membership periods
     *
     * @param userId 사용자 ID
     * @return 멤버십 기간 목록
     */
    public List<MembershipPeriod> getAllPeriods(Long userId) {
        return periodRepository.findByUserIdOrderByStartYearDesc(userId);
    }

    /**
     * 멤버십 갱신 가능 여부 확인
     * Check if membership renewal is available
     *
     * @param userId 사용자 ID
     * @param targetYear 갱신할 년도
     * @return 갱신 가능 여부
     */
    public boolean canRenew(Long userId, Integer targetYear) {
        // 이미 해당 년도 멤버십이 있으면 갱신 불가
        if (periodRepository.existsByUserIdAndStartYear(userId, targetYear)) {
            return false;
        }

        // 이전 년도 멤버십이 있는지 확인
        Integer previousYear = targetYear - 1;
        return periodRepository.existsByUserIdAndStartYear(userId, previousYear);
    }

    /**
     * 멤버십 만료 처리
     * Expire membership
     *
     * @param periodId 멤버십 기간 ID
     * @throws BusinessException 멤버십 기간을 찾을 수 없는 경우
     */
    @Transactional
    public void expirePeriod(Long periodId) {
        log.info("멤버십 만료 처리: periodId={}", periodId);

        MembershipPeriod period = periodRepository.findById(periodId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBERSHIP_PERIOD_NOT_FOUND));

        period.expire();

        log.info("멤버십 만료 처리 완료: periodId={}", periodId);
    }

    /**
     * 멤버십 취소 처리
     * Cancel membership
     *
     * @param periodId 멤버십 기간 ID
     * @throws BusinessException 멤버십 기간을 찾을 수 없는 경우
     */
    @Transactional
    public void cancelPeriod(Long periodId) {
        log.info("멤버십 취소 처리: periodId={}", periodId);

        MembershipPeriod period = periodRepository.findById(periodId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBERSHIP_PERIOD_NOT_FOUND));

        period.cancel();

        log.info("멤버십 취소 처리 완료: periodId={}", periodId);
    }

    /**
     * 특정 년도의 활성 멤버십 개수 조회
     * Get count of active memberships for year
     *
     * @param year 년도
     * @return 활성 멤버십 개수
     */
    public long getActiveMembershipCount(Integer year) {
        return periodRepository.countByStartYearAndStatus(year, MembershipPeriodStatus.ACTIVE);
    }

    /**
     * 멤버십 만료 알림 발송 기록
     * Mark expiration notification as sent
     *
     * @param periodId 멤버십 기간 ID
     * @throws BusinessException 멤버십 기간을 찾을 수 없는 경우
     */
    @Transactional
    public void markExpirationNotified(Long periodId) {
        log.info("멤버십 만료 알림 발송 기록: periodId={}", periodId);

        MembershipPeriod period = periodRepository.findById(periodId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBERSHIP_PERIOD_NOT_FOUND));

        period.markExpirationNotified();

        log.info("멤버십 만료 알림 발송 기록 완료: periodId={}", periodId);
    }
}
