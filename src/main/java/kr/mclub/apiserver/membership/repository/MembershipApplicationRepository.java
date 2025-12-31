package kr.mclub.apiserver.membership.repository;

import kr.mclub.apiserver.membership.domain.MembershipApplication;
import kr.mclub.apiserver.shared.domain.CommonCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

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
     * 사용자의 진행 중인 신청서 조회 (PENDING, DOCUMENT_VERIFICATION 상태)
     * Find user's pending application
     */
    @Query("SELECT ma FROM MembershipApplication ma " +
           "WHERE ma.userId = :userId " +
           "AND ma.status.codeGroup = 'APPLICATION_STATUS' " +
           "AND ma.status.code IN ('PENDING', 'DOCUMENT_VERIFICATION')")
    Optional<MembershipApplication> findPendingApplicationByUserId(@Param("userId") Long userId);

    /**
     * 상태별 신청서 목록 조회 / Find applications by status
     */
    List<MembershipApplication> findByStatus(CommonCode status);

    /**
     * 상태별 신청서 목록 조회 (페이징) / Find applications by status with pagination
     */
    @Query("SELECT ma FROM MembershipApplication ma WHERE ma.status = :status ORDER BY ma.createdAt DESC")
    List<MembershipApplication> findByStatusOrderByCreatedAtDesc(@Param("status") CommonCode status);

    /**
     * 차대번호 중복 확인 / Check VIN number duplication
     */
    boolean existsByVinNumber(String vinNumber);

    /**
     * 특정 상태의 신청서 개수 조회 / Count applications by status
     */
    long countByStatus(CommonCode status);

    /**
     * 사용자의 승인된 신청서 존재 여부 확인 / Check if user has approved application
     */
    @Query("SELECT COUNT(ma) > 0 FROM MembershipApplication ma " +
           "WHERE ma.userId = :userId " +
           "AND ma.status.codeGroup = 'APPLICATION_STATUS' " +
           "AND ma.status.code = 'APPROVED'")
    boolean hasApprovedApplication(@Param("userId") Long userId);
}
