package kr.mclub.apiserver.membership.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import kr.mclub.apiserver.membership.domain.ApplicationDocument;
import kr.mclub.apiserver.membership.domain.DocumentType;
import kr.mclub.apiserver.membership.domain.VerificationStatus;

/**
 * 신청 서류 Repository / Application Document Repository
 *
 * @since 1.0
 */
public interface ApplicationDocumentRepository extends JpaRepository<ApplicationDocument, Long> {

    /**
     * 신청서 ID로 모든 서류 조회 / Find all documents by application ID
     */
    @Query("SELECT ad FROM ApplicationDocument ad WHERE ad.application.id = :applicationId")
    List<ApplicationDocument> findByApplicationId(@Param("applicationId") Long applicationId);

    /**
     * 신청서 ID와 서류 타입으로 조회 / Find document by application ID and type
     */
    @Query("SELECT ad FROM ApplicationDocument ad WHERE ad.application.id = :applicationId AND ad.documentType = :documentType")
    Optional<ApplicationDocument> findByApplicationIdAndDocumentType(
            @Param("applicationId") Long applicationId,
            @Param("documentType") DocumentType documentType
    );

    /**
     * 신청서의 필수 서류 누락 확인 / Check if required documents are present
     *
     * @param applicationId 신청서 ID
     * @param requiredTypeCount 필수 서류 개수
     * @return 필수 서류가 모두 있으면 true
     */
    @Query("SELECT COUNT(DISTINCT ad.documentType) >= :requiredTypeCount " +
           "FROM ApplicationDocument ad " +
           "WHERE ad.application.id = :applicationId")
    boolean hasAllRequiredDocuments(
            @Param("applicationId") Long applicationId,
            @Param("requiredTypeCount") long requiredTypeCount
    );

    /**
     * 신청서의 검증 완료된 서류 개수 조회 / Count verified documents
     */
    @Query("SELECT COUNT(ad) FROM ApplicationDocument ad " +
           "WHERE ad.application.id = :applicationId " +
           "AND ad.verificationStatus = 'VERIFIED'")
    long countVerifiedDocuments(@Param("applicationId") Long applicationId);

    /**
     * 검증 대기 중인 서류 목록 조회 / Find unverified documents
     */
    @Query("SELECT ad FROM ApplicationDocument ad " +
           "WHERE ad.verificationStatus = 'PENDING' " +
           "ORDER BY ad.createdAt ASC")
    List<ApplicationDocument> findUnverifiedDocuments();

    /**
     * 검증 상태별 서류 목록 조회 / Find documents by verification status
     */
    List<ApplicationDocument> findByVerificationStatus(VerificationStatus status);
}
