package kr.mclub.apiserver.membership.domain;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import kr.mclub.apiserver.shared.domain.BaseTimeEntity;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * OCR 추출 결과 엔티티
 * OCR result entity
 */
@Entity
@Table(name = "ocr_results")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OcrResult extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "document_id", nullable = false)
    private Long documentId;

    // OCR 메타데이터
    @Enumerated(EnumType.STRING)
    @Column(name = "ocr_provider", nullable = false, length = 30)
    private OcrProvider ocrProvider;

    @Column(name = "ocr_version", length = 20)
    private String ocrVersion;

    @Column(name = "processing_time_ms")
    private Integer processingTimeMs;

    @Column(name = "confidence_score", precision = 5, scale = 4)
    private BigDecimal confidenceScore;  // 0.0000 ~ 1.0000

    @Column(name = "is_success", nullable = false)
    private boolean isSuccess = true;  // OCR 처리 성공 여부

    // 추출된 데이터 (JSON)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "extracted_data", nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> extractedData;  // 서류별 추출 결과

    @Column(name = "raw_text", columnDefinition = "TEXT")
    private String rawText;  // 원본 추출 텍스트

    // 대조 결과
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "match_result", columnDefinition = "jsonb")
    private Map<String, Object> matchResult;  // 신청 정보와 대조 결과

    @Column(name = "is_matched")
    private Boolean isMatched;  // 전체 대조 성공 여부

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "mismatch_fields", columnDefinition = "text[]")
    private List<String> mismatchFields;  // 불일치 필드 목록

    @Builder
    public OcrResult(Long documentId, OcrProvider ocrProvider, String ocrVersion,
                     Integer processingTimeMs, BigDecimal confidenceScore, Boolean isSuccess,
                     Map<String, Object> extractedData, String rawText) {
        this.documentId = documentId;
        this.ocrProvider = ocrProvider;
        this.ocrVersion = ocrVersion;
        this.processingTimeMs = processingTimeMs;
        this.confidenceScore = confidenceScore;
        this.isSuccess = isSuccess != null ? isSuccess : true;
        this.extractedData = extractedData;
        this.rawText = rawText;
    }

    /**
     * 대조 결과 설정
     * Set match result
     */
    public void setMatchResult(Map<String, Object> matchResult, Boolean isMatched, List<String> mismatchFields) {
        this.matchResult = matchResult;
        this.isMatched = isMatched;
        this.mismatchFields = mismatchFields;
    }

    /**
     * 대조 성공 여부 업데이트
     * Update match status
     */
    public void updateMatchStatus(Boolean isMatched) {
        this.isMatched = isMatched;
    }

    /**
     * 불일치 필드 추가
     * Add mismatch field
     */
    public void addMismatchField(String fieldName) {
        if (this.mismatchFields == null) {
            this.mismatchFields = new java.util.ArrayList<>();
        }
        this.mismatchFields.add(fieldName);
        this.isMatched = false;
    }
}
