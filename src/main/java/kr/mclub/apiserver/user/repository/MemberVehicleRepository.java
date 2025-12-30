package kr.mclub.apiserver.user.repository;

import kr.mclub.apiserver.user.domain.MemberVehicle;
import kr.mclub.apiserver.user.domain.VehicleStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 회원 차량 Repository
 * Member vehicle repository
 */
public interface MemberVehicleRepository extends JpaRepository<MemberVehicle, Long> {

    /**
     * 사용자 ID로 조회
     * Find by user ID
     */
    List<MemberVehicle> findByUserId(Long userId);

    /**
     * 사용자 ID로 활성 차량 조회
     * Find active vehicles by user ID
     */
    List<MemberVehicle> findByUserIdAndStatus(Long userId, VehicleStatus status);

    /**
     * 차대번호로 조회
     * Find by VIN number
     */
    Optional<MemberVehicle> findByVinNumber(String vinNumber);

    /**
     * 차대번호 존재 여부
     * Check if VIN number exists
     */
    boolean existsByVinNumber(String vinNumber);

    /**
     * 사용자의 대표 차량 조회
     * Find primary vehicle by user ID
     */
    Optional<MemberVehicle> findByUserIdAndIsPrimaryTrue(Long userId);

    /**
     * 유예 기간 만료된 차량 조회
     * Find vehicles with expired grace period
     */
    @Query("SELECT v FROM MemberVehicle v WHERE v.status = 'GRACE_PERIOD' AND v.gracePeriodEndAt < :date")
    List<MemberVehicle> findExpiredGracePeriodVehicles(@Param("date") LocalDate date);

    /**
     * 사용자의 활성 차량 수
     * Count active vehicles for user
     */
    long countByUserIdAndStatus(Long userId, VehicleStatus status);

    /**
     * 사용자에게 활성 차량이 있는지 확인
     * Check if user has any active vehicle
     */
    @Query("SELECT CASE WHEN COUNT(v) > 0 THEN true ELSE false END FROM MemberVehicle v WHERE v.userId = :userId AND v.status = 'ACTIVE'")
    boolean hasActiveVehicle(@Param("userId") Long userId);
}
