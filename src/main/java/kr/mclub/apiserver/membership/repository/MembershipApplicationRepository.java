package kr.mclub.apiserver.membership.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import kr.mclub.apiserver.membership.domain.ApplicationStatus;
import kr.mclub.apiserver.membership.domain.MembershipApplication;

/**
 * 정회원 신청 Repository / Membership Application Repository
 *
 * @since 1.0
 */
public interface MembershipApplicationRepository extends JpaRepository<MembershipApplication, Long> {

    /**
     * 사용자 ID로 신청서 조회 / Find application by user ID
     */
    Optional<MembershipApplication> findByUserId(Long userId);

    /**
     * 사용자의 진행 중인 신청서 조회
     * Find user's pending application
     */
    @Query("SELECT ma FROM MembershipApplication ma " +
           "WHERE ma.userId = :userId " +
           "AND ma.status IN ('DOCUMENT_PENDING', 'DOCUMENT_SUBMITTED', 'UNDER_REVIEW', 'PAYMENT_PENDING')")
    Optional<MembershipApplication> findPendingApplicationByUserId(@Param("userId") Long userId);

    /**
     * 상태별 신청서 목록 조회 / Find applications by status
     */
    List<MembershipApplication> findByStatus(ApplicationStatus status);

    /**
     * 상태별 신청서 목록 조회 (정렬) / Find applications by status ordered
     */
    List<MembershipApplication> findByStatusOrderByCreatedAtDesc(ApplicationStatus status);

    /**
     * 차대번호 중복 확인 / Check VIN number duplication
     */
    boolean existsByVinNumber(String vinNumber);

    /**
     * 신청 번호로 조회 / Find by application number
     */
    Optional<MembershipApplication> findByApplicationNumber(String applicationNumber);

    /**
     * 상태별 신청서 개수 조회 / Count applications by status
     */
    long countByStatus(ApplicationStatus status);

    /**
     * 서류 심사 중인 신청서 목록 조회 / Find applications under review
     */
    @Query("SELECT ma FROM MembershipApplication ma " +
           "WHERE ma.status IN ('DOCUMENT_SUBMITTED', 'UNDER_REVIEW') " +
           "ORDER BY ma.createdAt ASC")
    List<MembershipApplication> findApplicationsUnderReview();

    /**
     * 사용자 ID와 상태로 신청서 조회 / Find application by user ID and status
     */
    Optional<MembershipApplication> findByUserIdAndStatus(Long userId, ApplicationStatus status);

    /**
     * 날짜 접두사로 최신 신청 번호 조회 / Find latest application number by date prefix
     *
     * @param datePrefix 날짜 접두사 (예: "APP-20251230")
     * @return 최신 신청 번호 (Optional)
     */
    @Query("SELECT ma.applicationNumber FROM MembershipApplication ma " +
           "WHERE ma.applicationNumber LIKE CONCAT(:datePrefix, '%') " +
           "ORDER BY ma.applicationNumber DESC LIMIT 1")
    Optional<String> findLatestApplicationNumberByDatePrefix(@Param("datePrefix") String datePrefix);

    /**
     * 기간별 신청서 개수 조회 / Count applications by date range
     */
    long countByCreatedAtBetween(java.time.LocalDateTime startDate, java.time.LocalDateTime endDate);

    /**
     * 상태 및 기간별 신청서 개수 조회 / Count applications by status and date range
     */
    long countByStatusAndCreatedAtBetween(ApplicationStatus status, java.time.LocalDateTime startDate, java.time.LocalDateTime endDate);
}
