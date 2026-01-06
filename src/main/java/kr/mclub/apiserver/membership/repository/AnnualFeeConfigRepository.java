package kr.mclub.apiserver.membership.repository;

import kr.mclub.apiserver.membership.domain.AnnualFeeConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 연회비 설정 Repository / Annual Fee Configuration Repository
 *
 * @since 1.0
 */
public interface AnnualFeeConfigRepository extends JpaRepository<AnnualFeeConfig, Long> {

    /**
     * 대상 연도로 설정 조회 / Find config by target year
     */
    Optional<AnnualFeeConfig> findByTargetYear(Integer targetYear);

    /**
     * 모든 설정을 연도 내림차순으로 조회 / Find all configs ordered by year desc
     */
    List<AnnualFeeConfig> findAllByOrderByTargetYearDesc();

    /**
     * 현재 이월 가능 기간인 설정 조회 / Find configs in carry-over period
     *
     * @param currentDate 현재 날짜
     * @return 이월 기간에 해당하는 설정 목록
     */
    @Query("SELECT afc FROM AnnualFeeConfig afc " +
           "WHERE :currentDate <= afc.carryOverDeadline " +
           "ORDER BY afc.targetYear DESC")
    List<AnnualFeeConfig> findConfigsInCarryOverPeriod(@Param("currentDate") LocalDate currentDate);

    /**
     * 현재 갱신 가능 기간인 설정 조회 / Find configs in renewal period
     *
     * @param currentDate 현재 날짜
     * @return 갱신 기간에 해당하는 설정 목록
     */
    @Query("SELECT afc FROM AnnualFeeConfig afc " +
           "WHERE :currentDate BETWEEN afc.renewalStartDate AND afc.renewalDeadline " +
           "ORDER BY afc.targetYear DESC")
    List<AnnualFeeConfig> findConfigsInRenewalPeriod(@Param("currentDate") LocalDate currentDate);

    /**
     * 최신 설정 조회 (가장 큰 targetYear) / Find latest config
     */
    @Query("SELECT afc FROM AnnualFeeConfig afc " +
           "ORDER BY afc.targetYear DESC LIMIT 1")
    Optional<AnnualFeeConfig> findLatestConfig();

    /**
     * 특정 연도 이후의 모든 설정 조회 / Find configs after year
     */
    @Query("SELECT afc FROM AnnualFeeConfig afc " +
           "WHERE afc.targetYear >= :year " +
           "ORDER BY afc.targetYear ASC")
    List<AnnualFeeConfig> findConfigsAfterYear(@Param("year") Integer year);

    /**
     * 대상 연도 설정 존재 여부 확인 / Check if config exists for year
     */
    boolean existsByTargetYear(Integer targetYear);
}
