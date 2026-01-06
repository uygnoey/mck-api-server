package kr.mclub.apiserver.membership.service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import kr.mclub.apiserver.membership.domain.ApplicationDocument;
import kr.mclub.apiserver.membership.domain.DocumentType;
import kr.mclub.apiserver.membership.domain.OcrResult;
import kr.mclub.apiserver.shared.exception.BusinessException;
import kr.mclub.apiserver.shared.exception.ErrorCode;

/**
 * Paddle OCR Service Implementation
 *
 * <p>Paddle OCR을 사용하여 서류 이미지에서 텍스트를 추출합니다.</p>
 * <p>Uses Paddle OCR to extract text from document images.</p>
 *
 * <p>현재는 Mock 구현이며, 실제 Paddle OCR API 연동 시 구현 필요</p>
 * <p>Currently a mock implementation; requires actual Paddle OCR API integration</p>
 *
 * @since 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaddleOcrService implements OcrService {

    @Value("${ocr.paddle.enabled:false}")
    private boolean ocrEnabled;

    @Value("${ocr.paddle.api-url:http://localhost:8000}")
    private String ocrApiUrl;

    @Value("${ocr.paddle.confidence-threshold:0.7}")
    private double confidenceThreshold;

    /**
     * 서류 OCR 처리 / Process document OCR
     *
     * @param document 서류
     * @return OCR 결과
     * @throws BusinessException OCR 서비스를 사용할 수 없는 경우
     */
    @Override
    public OcrResult processDocument(ApplicationDocument document) {
        log.info("OCR 처리 시작: documentId={}, documentType={}", document.getId(), document.getDocumentType());

        if (!ocrEnabled) {
            throw new BusinessException(ErrorCode.OCR_SERVICE_NOT_AVAILABLE,
                    "OCR 서비스가 활성화되지 않았습니다");
        }

        if (!isSupported(document)) {
            throw new BusinessException(ErrorCode.OCR_NOT_SUPPORTED,
                    "지원하지 않는 서류 유형입니다: " + document.getDocumentType());
        }

        try {
            // TODO: 실제 Paddle OCR API 호출
            // 현재는 Mock 데이터 반환
            Map<String, Object> extractedData = performOcr(document);

            // OCR 결과 생성
            OcrResult result = OcrResult.builder()
                    .documentId(document.getId())
                    .ocrProvider(kr.mclub.apiserver.membership.domain.OcrProvider.PADDLE_OCR)
                    .extractedData(extractedData)
                    .confidenceScore(calculateConfidenceScore(extractedData))
                    .build();

            log.info("OCR 처리 완료: documentId={}, confidence={}", document.getId(), result.getConfidenceScore());
            return result;

        } catch (Exception e) {
            log.error("OCR 처리 실패: documentId={}", document.getId(), e);
            throw new BusinessException(ErrorCode.OCR_SERVICE_NOT_AVAILABLE,
                    "OCR 처리 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * OCR 결과와 신청서 정보 매칭 검증 / Verify OCR result matches application
     *
     * @param ocrResult OCR 결과
     * @param document 서류
     * @return 매칭 여부
     */
    @Override
    public boolean matchWithApplication(OcrResult ocrResult, ApplicationDocument document) {
        log.info("OCR 결과 매칭 검증: documentId={}", document.getId());

        if (ocrResult.getExtractedData() == null || ocrResult.getExtractedData().isEmpty()) {
            log.warn("OCR 결과가 비어있습니다: documentId={}", document.getId());
            return false;
        }

        // 신뢰도 점수 확인
        if (ocrResult.getConfidenceScore().compareTo(BigDecimal.valueOf(confidenceThreshold)) < 0) {
            log.warn("OCR 신뢰도가 낮습니다: documentId={}, score={}, threshold={}",
                    document.getId(), ocrResult.getConfidenceScore(), confidenceThreshold);
            return false;
        }

        // 서류 유형별 매칭 검증
        boolean matched = switch (document.getDocumentType()) {
            case VEHICLE_REGISTRATION -> matchVehicleRegistration(ocrResult, document);
            case ID_CARD -> matchIdCard(ocrResult, document);
            default -> {
                log.warn("매칭 검증이 지원되지 않는 서류 유형: {}", document.getDocumentType());
                yield false;
            }
        };

        log.info("OCR 매칭 검증 결과: documentId={}, matched={}", document.getId(), matched);
        return matched;
    }

    /**
     * 서류 유형 지원 여부 확인 / Check if document type is supported
     *
     * @param document 서류
     * @return 지원 여부
     */
    @Override
    public boolean isSupported(ApplicationDocument document) {
        if (!ocrEnabled) {
            return false;
        }

        DocumentType type = document.getDocumentType();
        return type == DocumentType.VEHICLE_REGISTRATION ||
               type == DocumentType.ID_CARD;
    }

    /**
     * Paddle OCR API 호출 (Mock 구현)
     * Call Paddle OCR API (Mock implementation)
     *
     * @param document 서류
     * @return 추출된 데이터
     */
    private Map<String, Object> performOcr(ApplicationDocument document) {
        log.debug("Paddle OCR API 호출 (Mock): url={}, documentId={}", ocrApiUrl, document.getId());

        // TODO: 실제 Paddle OCR API 호출 로직 구현
        // RestTemplate 또는 WebClient를 사용하여 OCR API 호출
        // POST {ocrApiUrl}/ocr
        // Body: { "image_url": document.getFileUrl(), "document_type": document.getDocumentType() }

        // Mock 데이터 반환
        Map<String, Object> extractedData = new HashMap<>();

        switch (document.getDocumentType()) {
            case VEHICLE_REGISTRATION -> {
                extractedData.put("carNumber", "서울12가3456");
                extractedData.put("vinNumber", "KMHXX00XXXX000000");
                extractedData.put("carModel", "BMW M3");
                extractedData.put("ownerName", "홍길동");
            }
            case ID_CARD -> {
                extractedData.put("name", "홍길동");
                extractedData.put("residentNumber", "900101-1******");
                extractedData.put("issueDate", "2020-01-01");
            }
            default -> log.warn("지원하지 않는 서류 유형: {}", document.getDocumentType());
        }

        return extractedData;
    }

    /**
     * 신뢰도 점수 계산 / Calculate confidence score
     *
     * @param extractedData 추출된 데이터
     * @return 신뢰도 점수 (0.0 ~ 1.0)
     */
    private BigDecimal calculateConfidenceScore(Map<String, Object> extractedData) {
        if (extractedData == null || extractedData.isEmpty()) {
            return BigDecimal.ZERO;
        }

        // TODO: 실제 Paddle OCR API에서 반환된 신뢰도 점수 사용
        // Mock: 추출된 필드 수에 비례한 점수
        int expectedFields = 4;  // 예상 필드 수
        int actualFields = extractedData.size();
        double score = Math.min(1.0, (double) actualFields / expectedFields);

        return BigDecimal.valueOf(Math.round(score * 100.0) / 100.0);  // 소수점 2자리
    }

    /**
     * 차량등록증 매칭 검증 / Verify vehicle registration match
     *
     * @param ocrResult OCR 결과
     * @param document 서류
     * @return 매칭 여부
     */
    private boolean matchVehicleRegistration(OcrResult ocrResult, ApplicationDocument document) {
        Map<String, Object> extracted = ocrResult.getExtractedData();

        // 필수 필드 존재 여부 확인
        boolean hasRequiredFields = extracted.containsKey("carNumber") &&
                                    extracted.containsKey("vinNumber") &&
                                    extracted.containsKey("carModel");

        if (!hasRequiredFields) {
            log.warn("차량등록증 필수 필드 누락: documentId={}", document.getId());
            return false;
        }

        // TODO: 실제 신청서의 차량 정보와 비교
        // MembershipApplication application = document.getApplication();
        // return extracted.get("carNumber").equals(application.getCarNumber()) &&
        //        extracted.get("vinNumber").equals(application.getVinNumber());

        return true;  // Mock: 항상 성공
    }

    /**
     * 신분증 매칭 검증 / Verify ID card match
     *
     * @param ocrResult OCR 결과
     * @param document 서류
     * @return 매칭 여부
     */
    private boolean matchIdCard(OcrResult ocrResult, ApplicationDocument document) {
        Map<String, Object> extracted = ocrResult.getExtractedData();

        // 필수 필드 존재 여부 확인
        boolean hasRequiredFields = extracted.containsKey("name") &&
                                    extracted.containsKey("residentNumber");

        if (!hasRequiredFields) {
            log.warn("신분증 필수 필드 누락: documentId={}", document.getId());
            return false;
        }

        // TODO: 실제 신청서의 신청자 정보와 비교
        // MembershipApplication application = document.getApplication();
        // return extracted.get("name").equals(application.getApplicantName());

        return true;  // Mock: 항상 성공
    }
}
