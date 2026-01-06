package kr.mclub.apiserver.membership.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import kr.mclub.apiserver.membership.domain.MembershipPeriod;
import kr.mclub.apiserver.membership.domain.MembershipPeriodStatus;

/**
 * 멤버십 기간 Repository / Membership Period Repository
 *
 * @since 1.0
 */
public interface MembershipPeriodRepository extends JpaRepository<MembershipPeriod, Long> {

    /**
     * 사용자 ID로 모든 멤버십 기간 조회 / Find all periods by user ID
     */
    List<MembershipPeriod> findByUserIdOrderByStartYearDesc(Long userId);

    /**
     * 사용자의 특정 연도 멤버십 기간 조회 / Find period by user and year
     */
    Optional<MembershipPeriod> findByUserIdAndStartYear(Long userId, Integer startYear);

    /**
     * 사용자의 현재 활성 멤버십 기간 조회 / Find user's active period
     */
    Optional<MembershipPeriod> findByUserIdAndStatus(Long userId, MembershipPeriodStatus status);

    /**
     * 특정 연도의 모든 활성 멤버십 조회 / Find all active periods by year
     */
    List<MembershipPeriod> findByStartYearAndStatus(Integer startYear, MembershipPeriodStatus status);

    // TODO: 다음 메서드들은 Service 레이어 구현 시 비즈니스 로직에 맞게 재구현 필요
    // - findValidMembershipOnDate: 특정 날짜의 유효한 멤버십 조회 (year 기반으로 재구현)
    // - findMembershipsNearRenewal: 갱신 기한 임박 멤버십 조회 (year 기반으로 재구현)
    // - findExpiredMemberships: 만료된 멤버십 조회 (year 기반으로 재구현)

    /**
     * 특정 연도 활성 멤버십 개수 조회 / Count active memberships by year
     */
    long countByStartYearAndStatus(Integer startYear, MembershipPeriodStatus status);

    /**
     * 사용자의 멤버십 존재 여부 확인 / Check if user has membership for year
     */
    boolean existsByUserIdAndStartYear(Long userId, Integer startYear);
}
