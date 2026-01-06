package kr.mclub.apiserver.membership.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import kr.mclub.apiserver.membership.domain.PaymentRecord;
import kr.mclub.apiserver.membership.domain.PaymentStatus;
import kr.mclub.apiserver.membership.domain.PaymentType;

/**
 * 결제 기록 Repository / Payment Record Repository
 *
 * @since 1.0
 */
public interface PaymentRecordRepository extends JpaRepository<PaymentRecord, Long> {

    /**
     * 사용자 ID로 모든 결제 기록 조회 / Find all payment records by user ID
     */
    List<PaymentRecord> findByUserIdOrderByDepositDateDesc(Long userId);

    /**
     * 사용자 ID로 모든 결제 기록 조회 (생성일 기준) / Find all payment records by user ID ordered by created at
     */
    List<PaymentRecord> findByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * 사용자의 특정 연도 결제 기록 조회 / Find payment records by user and year
     */
    List<PaymentRecord> findByUserIdAndTargetYear(Long userId, Integer targetYear);

    /**
     * 사용자의 특정 연도 확인된 결제 조회 / Find confirmed payment by user and year
     */
    @Query("SELECT pr FROM PaymentRecord pr " +
           "WHERE pr.userId = :userId " +
           "AND pr.targetYear = :targetYear " +
           "AND pr.status = 'CONFIRMED'")
    Optional<PaymentRecord> findConfirmedPaymentByUserAndYear(
            @Param("userId") Long userId,
            @Param("targetYear") Integer targetYear
    );

    /**
     * 상태별 결제 기록 조회 / Find payment records by status
     */
    List<PaymentRecord> findByStatusOrderByDepositDateDesc(PaymentStatus status);

    /**
     * 상태별 결제 기록 조회 (생성일 기준) / Find payment records by status ordered by created at
     */
    List<PaymentRecord> findByStatusOrderByCreatedAtDesc(PaymentStatus status);

    /**
     * 신청서, 결제 유형, 상태로 결제 조회 / Find payment by application, type and status
     */
    Optional<PaymentRecord> findByApplicationIdAndPaymentTypeAndStatus(
            Long applicationId, PaymentType paymentType, PaymentStatus status);

    /**
     * 사용자, 결제 유형, 년도, 상태로 결제 조회 / Find payment by user, type, year and status
     */
    Optional<PaymentRecord> findByUserIdAndPaymentTypeAndTargetYearAndStatus(
            Long userId, PaymentType paymentType, Integer targetYear, PaymentStatus status);

    /**
     * 수동 확인 대기 중인 결제 목록 조회 (PENDING 상태)
     * Find pending payments for manual confirmation
     */
    @Query("SELECT pr FROM PaymentRecord pr " +
           "WHERE pr.status = 'PENDING' " +
           "AND pr.autoConfirmed = false " +
           "ORDER BY pr.depositDate ASC")
    List<PaymentRecord> findPendingManualConfirmations();

    /**
     * 특정 연도의 결제 통계 조회 / Find payment statistics for a year
     */
    @Query("SELECT pr.paymentType, COUNT(pr), SUM(pr.amount) " +
           "FROM PaymentRecord pr " +
           "WHERE pr.targetYear = :targetYear " +
           "AND pr.status = 'CONFIRMED' " +
           "GROUP BY pr.paymentType")
    List<Object[]> findPaymentStatisticsByYear(@Param("targetYear") Integer targetYear);

    /**
     * 결제 타입별 개수 조회 / Count by payment type
     */
    long countByPaymentType(PaymentType paymentType);

    /**
     * 신청서 ID로 결제 기록 조회 / Find payment records by application ID
     */
    List<PaymentRecord> findByApplicationId(Long applicationId);

    /**
     * 상태별 결제 개수 조회 / Count payments by status
     */
    long countByStatus(PaymentStatus status);
}
