package kr.mclub.apiserver.membership.service;

import kr.mclub.apiserver.membership.domain.ApplicationDocument;
import kr.mclub.apiserver.membership.domain.OcrResult;

/**
 * OCR 처리 서비스 인터페이스 / OCR Processing Service Interface
 *
 * <p>서류 이미지에서 텍스트를 추출하고 신청 정보와 대조합니다.</p>
 * <p>Extracts text from document images and matches with application data.</p>
 *
 * @since 1.0
 */
public interface OcrService {

    /**
     * 서류 OCR 처리 / Process document OCR
     *
     * @param document 처리할 서류
     * @return OCR 결과
     */
    OcrResult processDocument(ApplicationDocument document);

    /**
     * OCR 결과와 신청 정보 대조 / Match OCR result with application data
     *
     * @param ocrResult OCR 결과
     * @param document 서류
     * @return 대조 성공 여부
     */
    boolean matchWithApplication(OcrResult ocrResult, ApplicationDocument document);

    /**
     * OCR 지원 여부 확인 / Check if OCR is supported for document type
     *
     * @param document 서류
     * @return 지원 여부
     */
    boolean isSupported(ApplicationDocument document);
}
