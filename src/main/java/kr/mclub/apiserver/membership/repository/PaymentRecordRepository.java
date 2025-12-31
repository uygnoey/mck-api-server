package kr.mclub.apiserver.membership.repository;

import kr.mclub.apiserver.membership.domain.PaymentRecord;
import kr.mclub.apiserver.shared.domain.CommonCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

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
     * 사용자의 특정 연도 결제 기록 조회 / Find payment records by user and year
     */
    List<PaymentRecord> findByUserIdAndTargetYear(Long userId, Integer targetYear);

    /**
     * 사용자의 특정 연도 확인된 결제 조회 / Find confirmed payment by user and year
     */
    @Query("SELECT pr FROM PaymentRecord pr " +
           "WHERE pr.userId = :userId " +
           "AND pr.targetYear = :targetYear " +
           "AND pr.status.codeGroup = 'PAYMENT_STATUS' " +
           "AND pr.status.code = 'CONFIRMED'")
    Optional<PaymentRecord> findConfirmedPaymentByUserAndYear(
            @Param("userId") Long userId,
            @Param("targetYear") Integer targetYear
    );

    /**
     * 상태별 결제 기록 조회 / Find payment records by status
     */
    List<PaymentRecord> findByStatusOrderByDepositDateDesc(CommonCode status);

    /**
     * 수동 확인 대기 중인 결제 목록 조회 (PENDING 상태)
     * Find pending payments for manual confirmation
     */
    @Query("SELECT pr FROM PaymentRecord pr " +
           "WHERE pr.status.codeGroup = 'PAYMENT_STATUS' " +
           "AND pr.status.code = 'PENDING' " +
           "ORDER BY pr.depositDate ASC")
    List<PaymentRecord> findPendingPayments();

    /**
     * 특정 기간 내 결제 기록 조회 / Find payments within date range
     */
    @Query("SELECT pr FROM PaymentRecord pr " +
           "WHERE pr.depositDate BETWEEN :startDate AND :endDate " +
           "ORDER BY pr.depositDate DESC")
    List<PaymentRecord> findPaymentsBetweenDates(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    /**
     * 결제 타입별 통계 조회 / Count payments by type
     */
    long countByPaymentType(CommonCode paymentType);

    /**
     * 특정 연도 결제 완료 건수 조회 / Count confirmed payments by year
     */
    @Query("SELECT COUNT(pr) FROM PaymentRecord pr " +
           "WHERE pr.targetYear = :targetYear " +
           "AND pr.status.codeGroup = 'PAYMENT_STATUS' " +
           "AND pr.status.code = 'CONFIRMED'")
    long countConfirmedPaymentsByYear(@Param("targetYear") Integer targetYear);
}
