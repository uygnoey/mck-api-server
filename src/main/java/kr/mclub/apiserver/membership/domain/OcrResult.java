package kr.mclub.apiserver.membership.domain;

import jakarta.persistence.*;
import kr.mclub.apiserver.shared.domain.BaseTimeEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * OCR 검증 결과 / OCR Result
 *
 * <p>제출된 서류에서 OCR로 추출한 정보 및 검증 결과를 저장합니다.</p>
 *
 * <h3>OCR 처리 대상</h3>
 * <ul>
 *   <li>차량등록증: 차량 번호, 차대번호, 소유자명 추출</li>
 *   <li>신분증: 이름, 생년월일 추출 및 검증</li>
 *   <li>사업자등록증: 사업자번호, 회사명 추출</li>
 * </ul>
 *
 * @since 1.0
 */
@Entity
@Table(name = "ocr_results")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OcrResult extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 서류 ID (ApplicationDocument 테이블 참조)
     */
    @Column(name = "document_id", nullable = false)
    private Long documentId;

    /**
     * OCR 처리 성공 여부
     */
    @Column(name = "is_success", nullable = false)
    private Boolean isSuccess;

    /**
     * OCR 신뢰도 점수 (0.0 ~ 1.0)
     * 1.0에 가까울수록 높은 신뢰도
     */
    @Column(name = "confidence_score", precision = 3, scale = 2)
    private BigDecimal confidenceScore;

    /**
     * 추출된 텍스트 (JSON 형식)
     * 예: {"carNumber": "12가3456", "vinNumber": "ABC123", "ownerName": "홍길동"}
     */
    @Column(name = "extracted_text", columnDefinition = "TEXT")
    private String extractedText;

    /**
     * 검증 결과
     * true: 추출된 정보가 신청서 정보와 일치
     * false: 불일치 또는 검증 불가
     */
    @Column(name = "is_verified", nullable = false)
    private Boolean isVerified;

    /**
     * 검증 메시지
     * 검증 실패 사유 또는 추가 정보
     */
    @Column(name = "verification_message", length = 500)
    private String verificationMessage;

    /**
     * OCR 처리 일시
     */
    @Column(name = "processed_at", nullable = false)
    private LocalDateTime processedAt;

    /**
     * 사용한 OCR 엔진
     * 예: PaddleOCR, Tesseract, Naver Clova OCR
     */
    @Column(name = "ocr_engine", length = 50)
    private String ocrEngine;

    /**
     * 오류 메시지
     * OCR 처리 실패 시 오류 내용
     */
    @Column(name = "error_message", length = 1000)
    private String errorMessage;

    // === 생성자 ===

    /**
     * OCR 결과 생성 (성공)
     *
     * @param documentId 서류 ID
     * @param confidenceScore 신뢰도 점수
     * @param extractedText 추출된 텍스트 (JSON)
     * @param isVerified 검증 결과
     * @param verificationMessage 검증 메시지
     * @param ocrEngine 사용한 OCR 엔진
     */
    public OcrResult(
            Long documentId,
            BigDecimal confidenceScore,
            String extractedText,
            Boolean isVerified,
            String verificationMessage,
            String ocrEngine
    ) {
        this.documentId = documentId;
        this.isSuccess = true;
        this.confidenceScore = confidenceScore;
        this.extractedText = extractedText;
        this.isVerified = isVerified;
        this.verificationMessage = verificationMessage;
        this.processedAt = LocalDateTime.now();
        this.ocrEngine = ocrEngine;
    }

    /**
     * OCR 결과 생성 (실패)
     *
     * @param documentId 서류 ID
     * @param errorMessage 오류 메시지
     * @param ocrEngine 사용한 OCR 엔진
     */
    public OcrResult(
            Long documentId,
            String errorMessage,
            String ocrEngine
    ) {
        this.documentId = documentId;
        this.isSuccess = false;
        this.isVerified = false;
        this.errorMessage = errorMessage;
        this.processedAt = LocalDateTime.now();
        this.ocrEngine = ocrEngine;
    }

    // === 비즈니스 메서드 ===

    /**
     * 검증 결과 업데이트
     *
     * @param isVerified 검증 결과
     * @param message 검증 메시지
     */
    public void updateVerification(Boolean isVerified, String message) {
        this.isVerified = isVerified;
        this.verificationMessage = message;
    }
}
