package kr.mclub.apiserver.membership.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import kr.mclub.apiserver.membership.domain.AnnualFeeConfig;
import kr.mclub.apiserver.membership.repository.AnnualFeeConfigRepository;
import kr.mclub.apiserver.shared.exception.BusinessException;
import kr.mclub.apiserver.shared.exception.ErrorCode;

/**
 * 연회비 설정 Service / Annual Fee Configuration Service
 *
 * <p>연회비 금액, 이월/갱신 기간 설정을 관리합니다.</p>
 * <p>Manages annual fee amounts and carry-over/renewal period configuration.</p>
 *
 * @since 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnnualFeeService {

    private final AnnualFeeConfigRepository annualFeeConfigRepository;

    /**
     * 특정 년도 연회비 설정 조회 / Get annual fee config by year
     *
     * @param year 대상 년도
     * @return 연회비 설정 (없으면 기본값 반환)
     */
    public AnnualFeeConfig getConfigByYear(Integer year) {
        return annualFeeConfigRepository.findByTargetYear(year)
                .orElseGet(() -> createDefaultConfig(year));
    }

    /**
     * 현재 년도 연회비 설정 조회 / Get current year config
     *
     * @return 현재 년도 연회비 설정
     */
    public AnnualFeeConfig getCurrentYearConfig() {
        return getConfigByYear(LocalDate.now().getYear());
    }

    /**
     * 모든 연회비 설정 조회 / Get all configs
     *
     * @return 연회비 설정 목록
     */
    public List<AnnualFeeConfig> getAllConfigs() {
        return annualFeeConfigRepository.findAllByOrderByTargetYearDesc();
    }

    /**
     * 연회비 설정 생성 / Create annual fee config
     *
     * @param targetYear 대상 년도
     * @param carryOverDeadline 이월 마감일
     * @param renewalStartDate 갱신 시작일
     * @param renewalDeadline 갱신 마감일
     * @param enrollmentFeeAmount 입회비
     * @param annualFeeAmount 연회비
     * @param configuredBy 설정한 임원 ID
     * @param notes 비고
     * @return 생성된 설정
     * @throws BusinessException 이미 설정이 존재하는 경우
     */
    @Transactional
    public AnnualFeeConfig createConfig(Integer targetYear, LocalDate carryOverDeadline,
                                        LocalDate renewalStartDate, LocalDate renewalDeadline,
                                        BigDecimal enrollmentFeeAmount, BigDecimal annualFeeAmount,
                                        Long configuredBy, String notes) {
        log.info("연회비 설정 생성: targetYear={}, enrollmentFee={}, annualFee={}",
                targetYear, enrollmentFeeAmount, annualFeeAmount);

        // 중복 확인
        annualFeeConfigRepository.findByTargetYear(targetYear)
                .ifPresent(existing -> {
                    throw new BusinessException(ErrorCode.ANNUAL_FEE_CONFIG_ALREADY_EXISTS,
                            String.format("%d년도 설정이 이미 존재합니다", targetYear));
                });

        AnnualFeeConfig config = AnnualFeeConfig.builder()
                .targetYear(targetYear)
                .carryOverDeadline(carryOverDeadline)
                .renewalStartDate(renewalStartDate)
                .renewalDeadline(renewalDeadline)
                .enrollmentFeeAmount(enrollmentFeeAmount)
                .annualFeeAmount(annualFeeAmount)
                .configuredBy(configuredBy)
                .notes(notes)
                .build();

        AnnualFeeConfig savedConfig = annualFeeConfigRepository.save(config);
        log.info("연회비 설정 생성 완료: configId={}", savedConfig.getId());

        return savedConfig;
    }

    /**
     * 연회비 설정 업데이트 / Update annual fee config
     *
     * @param targetYear 대상 년도
     * @param carryOverDeadline 이월 마감일
     * @param renewalStartDate 갱신 시작일
     * @param renewalDeadline 갱신 마감일
     * @param enrollmentFeeAmount 입회비
     * @param annualFeeAmount 연회비
     * @param notes 비고
     * @return 업데이트된 설정
     * @throws BusinessException 설정을 찾을 수 없는 경우
     */
    @Transactional
    public AnnualFeeConfig updateConfig(Integer targetYear, LocalDate carryOverDeadline,
                                        LocalDate renewalStartDate, LocalDate renewalDeadline,
                                        BigDecimal enrollmentFeeAmount, BigDecimal annualFeeAmount,
                                        String notes) {
        log.info("연회비 설정 업데이트: targetYear={}", targetYear);

        AnnualFeeConfig config = annualFeeConfigRepository.findByTargetYear(targetYear)
                .orElseThrow(() -> new BusinessException(ErrorCode.ANNUAL_FEE_CONFIG_NOT_FOUND,
                        String.format("%d년도 설정을 찾을 수 없습니다", targetYear)));

        config.update(carryOverDeadline, renewalStartDate, renewalDeadline,
                enrollmentFeeAmount, annualFeeAmount, notes);

        log.info("연회비 설정 업데이트 완료: configId={}", config.getId());
        return config;
    }

    /**
     * 기본 설정 생성 (실제 DB에 저장하지 않음)
     * Create default config (not saved to DB)
     *
     * @param year 년도
     * @return 기본 설정
     */
    private AnnualFeeConfig createDefaultConfig(Integer year) {
        return AnnualFeeConfig.builder()
                .targetYear(year)
                .carryOverDeadline(LocalDate.of(year, 1, 15))
                .renewalStartDate(LocalDate.of(year, 1, 1))
                .renewalDeadline(LocalDate.of(year, 1, 31))
                .enrollmentFeeAmount(new BigDecimal("200000"))
                .annualFeeAmount(new BigDecimal("200000"))
                .configuredBy(0L)
                .notes("기본 설정")
                .build();
    }
}
