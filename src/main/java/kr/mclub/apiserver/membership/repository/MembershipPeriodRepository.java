package kr.mclub.apiserver.membership.repository;

import kr.mclub.apiserver.membership.domain.MembershipPeriod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 멤버십 기간 Repository / Membership Period Repository
 *
 * @since 1.0
 */
public interface MembershipPeriodRepository extends JpaRepository<MembershipPeriod, Long> {

    /**
     * 사용자 ID로 모든 멤버십 기간 조회 / Find all periods by user ID
     */
    List<MembershipPeriod> findByUserIdOrderByYearDesc(Long userId);

    /**
     * 사용자의 특정 연도 멤버십 기간 조회 / Find period by user and year
     */
    Optional<MembershipPeriod> findByUserIdAndYear(Long userId, Integer year);

    /**
     * 사용자의 현재 활성 멤버십 기간 조회 / Find user's active period
     */
    Optional<MembershipPeriod> findByUserIdAndIsActiveTrue(Long userId);

    /**
     * 특정 연도의 모든 활성 멤버십 조회 / Find all active periods by year
     */
    List<MembershipPeriod> findByYearAndIsActiveTrue(Integer year);

    /**
     * 특정 날짜에 유효한 사용자의 멤버십 조회 / Find valid membership on date
     */
    @Query("SELECT mp FROM MembershipPeriod mp " +
           "WHERE mp.userId = :userId " +
           "AND mp.isActive = true " +
           "AND :currentDate BETWEEN mp.startDate AND mp.renewalDeadline")
    Optional<MembershipPeriod> findValidMembershipOnDate(
            @Param("userId") Long userId,
            @Param("currentDate") LocalDate currentDate
    );

    /**
     * 갱신 기한이 임박한 멤버십 목록 조회 / Find memberships near renewal deadline
     *
     * @param targetDate 기준 날짜 (예: 오늘 + 30일)
     * @return 갱신 기한이 targetDate 이전인 활성 멤버십 목록
     */
    @Query("SELECT mp FROM MembershipPeriod mp " +
           "WHERE mp.isActive = true " +
           "AND mp.renewalDeadline <= :targetDate " +
           "ORDER BY mp.renewalDeadline ASC")
    List<MembershipPeriod> findMembershipsNearRenewal(@Param("targetDate") LocalDate targetDate);

    /**
     * 만료된 멤버십 목록 조회 / Find expired memberships
     */
    @Query("SELECT mp FROM MembershipPeriod mp " +
           "WHERE mp.isActive = true " +
           "AND mp.renewalDeadline < :currentDate")
    List<MembershipPeriod> findExpiredMemberships(@Param("currentDate") LocalDate currentDate);

    /**
     * 특정 연도 활성 멤버십 개수 조회 / Count active memberships by year
     */
    long countByYearAndIsActiveTrue(Integer year);

    /**
     * 사용자의 멤버십 존재 여부 확인 / Check if user has membership for year
     */
    boolean existsByUserIdAndYear(Long userId, Integer year);
}
