package kr.mclub.apiserver.membership.api.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import kr.mclub.apiserver.membership.domain.OcrProvider;
import kr.mclub.apiserver.membership.domain.OcrResult;

/**
 * OCR 결과 응답 DTO
 * OCR result response DTO
 */
public record OcrResultResponse(
        Long id,
        Long documentId,
        OcrProvider ocrProvider,
        String ocrVersion,
        Integer processingTimeMs,
        BigDecimal confidenceScore,
        boolean isSuccess,
        Map<String, Object> extractedData,
        String rawText,
        Map<String, Object> matchResult,
        Boolean isMatched,
        List<String> mismatchFields,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    /**
     * 엔티티로부터 응답 DTO 생성
     * Create response DTO from entity
     */
    public static OcrResultResponse from(OcrResult ocrResult) {
        return new OcrResultResponse(
                ocrResult.getId(),
                ocrResult.getDocumentId(),
                ocrResult.getOcrProvider(),
                ocrResult.getOcrVersion(),
                ocrResult.getProcessingTimeMs(),
                ocrResult.getConfidenceScore(),
                ocrResult.isSuccess(),
                ocrResult.getExtractedData(),
                ocrResult.getRawText(),
                ocrResult.getMatchResult(),
                ocrResult.getIsMatched(),
                ocrResult.getMismatchFields(),
                ocrResult.getCreatedAt(),
                ocrResult.getUpdatedAt()
        );
    }
}
