package kr.mclub.apiserver.membership.repository;

import kr.mclub.apiserver.membership.domain.OcrResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * OCR 검증 결과 Repository / OCR Result Repository
 *
 * @since 1.0
 */
public interface OcrResultRepository extends JpaRepository<OcrResult, Long> {

    /**
     * 서류 ID로 OCR 결과 조회 / Find OCR result by document ID
     */
    Optional<OcrResult> findByDocumentId(Long documentId);

    /**
     * 신청서의 모든 OCR 결과 조회 / Find all OCR results by application ID
     */
    @Query("SELECT ocr FROM OcrResult ocr " +
           "JOIN ApplicationDocument ad ON ocr.documentId = ad.id " +
           "WHERE ad.applicationId = :applicationId")
    List<OcrResult> findByApplicationId(@Param("applicationId") Long applicationId);

    /**
     * 검증 성공한 OCR 결과 목록 조회 / Find successful OCR results
     */
    List<OcrResult> findByIsSuccessTrueOrderByCreatedAtDesc();

    /**
     * 검증 실패한 OCR 결과 목록 조회 / Find failed OCR results
     */
    List<OcrResult> findByIsSuccessFalseOrderByCreatedAtDesc();

    /**
     * 수동 검증이 필요한 OCR 결과 목록 조회 / Find OCR results requiring manual review
     */
    @Query("SELECT ocr FROM OcrResult ocr " +
           "WHERE ocr.isSuccess = false " +
           "OR ocr.confidenceScore < :minConfidence " +
           "ORDER BY ocr.createdAt ASC")
    List<OcrResult> findResultsRequiringManualReview(@Param("minConfidence") Double minConfidence);

    /**
     * 특정 신뢰도 이상의 성공 결과 조회 / Find successful results above confidence threshold
     */
    @Query("SELECT ocr FROM OcrResult ocr " +
           "WHERE ocr.isSuccess = true " +
           "AND ocr.confidenceScore >= :minConfidence " +
           "ORDER BY ocr.confidenceScore DESC")
    List<OcrResult> findHighConfidenceResults(@Param("minConfidence") Double minConfidence);

    /**
     * 평균 신뢰도 점수 조회 / Calculate average confidence score
     */
    @Query("SELECT AVG(ocr.confidenceScore) FROM OcrResult ocr WHERE ocr.isSuccess = true")
    Double calculateAverageConfidenceScore();

    /**
     * OCR 성공률 조회 / Calculate OCR success rate
     */
    @Query("SELECT (COUNT(CASE WHEN ocr.isSuccess = true THEN 1 END) * 100.0 / COUNT(*)) " +
           "FROM OcrResult ocr")
    Double calculateSuccessRate();
}
