package kr.mclub.apiserver.user.repository;

import kr.mclub.apiserver.shared.domain.CommonCode;
import kr.mclub.apiserver.user.domain.MemberVehicle;
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
     * 사용자 ID로 조회 (최신순)
     * Find by user ID ordered by registration date desc
     */
    List<MemberVehicle> findByUserIdOrderByIsPrimaryDescRegisteredAtDesc(Long userId);

    /**
     * 사용자 ID와 상태로 조회
     * Find by user ID and status
     */
    List<MemberVehicle> findByUserIdAndStatus(Long userId, CommonCode status);

    /**
     * 사용자의 활성 차량 조회
     * Find active vehicles by user ID
     */
    @Query("SELECT mv FROM MemberVehicle mv " +
           "WHERE mv.userId = :userId " +
           "AND mv.status.codeGroup = 'VEHICLE_STATUS' " +
           "AND mv.status.code = 'ACTIVE' " +
           "ORDER BY mv.isPrimary DESC, mv.registeredAt DESC")
    List<MemberVehicle> findActiveVehiclesByUserId(@Param("userId") Long userId);

    /**
     * 차대번호로 조회
     * Find by VIN number
     */
    Optional<MemberVehicle> findByVinNumber(String vinNumber);

    /**
     * 차량번호로 조회
     * Find by license plate
     */
    Optional<MemberVehicle> findByLicensePlate(String licensePlate);

    /**
     * 차대번호 존재 여부
     * Check if VIN number exists
     */
    boolean existsByVinNumber(String vinNumber);

    /**
     * 차량번호 존재 여부
     * Check if license plate exists
     */
    boolean existsByLicensePlate(String licensePlate);

    /**
     * 사용자의 대표 차량 조회
     * Find primary vehicle by user ID
     */
    Optional<MemberVehicle> findByUserIdAndIsPrimaryTrue(Long userId);

    /**
     * 유예 기간 만료된 차량 조회
     * Find vehicles with expired grace period
     */
    @Query("SELECT mv FROM MemberVehicle mv " +
           "WHERE mv.status.codeGroup = 'VEHICLE_STATUS' " +
           "AND mv.status.code = 'GRACE_PERIOD' " +
           "AND mv.gracePeriodEndAt < :date")
    List<MemberVehicle> findExpiredGracePeriodVehicles(@Param("date") LocalDate date);

    /**
     * 사용자의 활성 차량 존재 여부
     * Check if user has active vehicle
     */
    @Query("SELECT COUNT(mv) > 0 FROM MemberVehicle mv " +
           "WHERE mv.userId = :userId " +
           "AND mv.status.codeGroup = 'VEHICLE_STATUS' " +
           "AND mv.status.code = 'ACTIVE'")
    boolean hasActiveVehicle(@Param("userId") Long userId);

    /**
     * 차량 상태별 조회
     * Find vehicles by status
     */
    List<MemberVehicle> findByStatusOrderByRegisteredAtDesc(CommonCode status);

    /**
     * 모델별 활성 차량 개수 조회
     * Count active vehicles by model
     */
    @Query("SELECT mv.modelName, COUNT(mv) FROM MemberVehicle mv " +
           "WHERE mv.status.codeGroup = 'VEHICLE_STATUS' " +
           "AND mv.status.code = 'ACTIVE' " +
           "GROUP BY mv.modelName " +
           "ORDER BY COUNT(mv) DESC")
    List<Object[]> countActiveVehiclesByModel();
}
