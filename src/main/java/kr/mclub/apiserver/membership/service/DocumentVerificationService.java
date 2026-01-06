package kr.mclub.apiserver.membership.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import kr.mclub.apiserver.membership.api.dto.DocumentResponse;
import kr.mclub.apiserver.membership.api.dto.DocumentUploadRequest;
import kr.mclub.apiserver.membership.api.dto.OcrResultResponse;
import kr.mclub.apiserver.membership.domain.ApplicationDocument;
import kr.mclub.apiserver.membership.domain.MembershipApplication;
import kr.mclub.apiserver.membership.domain.OcrResult;
import kr.mclub.apiserver.membership.domain.VerificationStatus;
import kr.mclub.apiserver.membership.repository.ApplicationDocumentRepository;
import kr.mclub.apiserver.membership.repository.MembershipApplicationRepository;
import kr.mclub.apiserver.membership.repository.OcrResultRepository;
import kr.mclub.apiserver.shared.exception.BusinessException;
import kr.mclub.apiserver.shared.exception.ErrorCode;

/**
 * 서류 검증 Service / Document Verification Service
 *
 * <p>정회원 신청 서류의 업로드, OCR 처리, 검증을 관리합니다.</p>
 * <p>Manages document upload, OCR processing, and verification for membership applications.</p>
 *
 * @since 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DocumentVerificationService {

    private final ApplicationDocumentRepository documentRepository;
    private final MembershipApplicationRepository applicationRepository;
    private final OcrResultRepository ocrResultRepository;
    private final Optional<OcrService> ocrService;  // Optional: OCR 서비스가 없을 수도 있음

    /**
     * 서류 업로드 등록 / Register document upload
     *
     * @param applicationId 신청서 ID
     * @param request 서류 업로드 요청
     * @return 등록된 서류 응답 DTO
     * @throws BusinessException 신청서를 찾을 수 없거나 이미 같은 유형의 서류가 등록된 경우
     */
    @Transactional
    public DocumentResponse uploadDocument(Long applicationId, DocumentUploadRequest request) {
        log.info("서류 업로드 등록 시작: applicationId={}, documentType={}", applicationId, request.documentType());

        // 신청서 조회
        MembershipApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.APPLICATION_NOT_FOUND));

        // 중복 서류 확인
        documentRepository.findByApplicationIdAndDocumentType(applicationId, request.documentType())
                .ifPresent(existing -> {
                    throw new BusinessException(ErrorCode.DOCUMENT_ALREADY_EXISTS,
                            "이미 등록된 서류 유형입니다: " + request.documentType());
                });

        // 서류 생성
        ApplicationDocument document = ApplicationDocument.builder()
                .application(application)
                .documentType(request.documentType())
                .fileUrl(request.fileUrl())
                .originalFileName(request.originalFileName())
                .fileSize(request.fileSize())
                .contentType(request.contentType())
                .build();

        ApplicationDocument savedDocument = documentRepository.save(document);

        // OCR 처리 시도 (OCR 서비스가 있고, 지원하는 서류 타입인 경우)
        ocrService.ifPresent(service -> {
            if (service.isSupported(savedDocument)) {
                try {
                    OcrResult ocrResult = service.processDocument(savedDocument);
                    OcrResult savedOcrResult = ocrResultRepository.save(ocrResult);
                    savedDocument.linkOcrResult(savedOcrResult.getId());
                    log.info("OCR 처리 완료: documentId={}, ocrResultId={}", savedDocument.getId(), savedOcrResult.getId());
                } catch (Exception e) {
                    log.error("OCR 처리 실패: documentId={}", savedDocument.getId(), e);
                    // OCR 실패해도 서류 등록은 성공으로 처리
                }
            }
        });

        log.info("서류 업로드 등록 완료: documentId={}", savedDocument.getId());
        return DocumentResponse.from(savedDocument);
    }

    /**
     * 신청서의 모든 서류 조회 / Get all documents for application
     *
     * @param applicationId 신청서 ID
     * @return 서류 목록
     */
    public List<DocumentResponse> getDocumentsByApplicationId(Long applicationId) {
        return documentRepository.findByApplicationId(applicationId).stream()
                .map(DocumentResponse::from)
                .toList();
    }

    /**
     * 서류 ID로 조회 / Get document by ID
     *
     * @param documentId 서류 ID
     * @return 서류 응답 DTO
     * @throws BusinessException 서류를 찾을 수 없는 경우
     */
    public DocumentResponse getDocumentById(Long documentId) {
        ApplicationDocument document = documentRepository.findById(documentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DOCUMENT_NOT_FOUND));

        return DocumentResponse.from(document);
    }

    /**
     * 서류 검증 승인 / Approve document verification
     *
     * @param documentId 서류 ID
     * @param verifierId 검증자 ID (관리자)
     * @return 검증된 서류 응답 DTO
     * @throws BusinessException 서류를 찾을 수 없거나 이미 검증된 경우
     */
    @Transactional
    public DocumentResponse approveDocument(Long documentId, Long verifierId) {
        log.info("서류 검증 승인 시작: documentId={}, verifierId={}", documentId, verifierId);

        ApplicationDocument document = documentRepository.findById(documentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DOCUMENT_NOT_FOUND));

        // 이미 검증된 서류인지 확인
        if (document.getVerificationStatus() == VerificationStatus.VERIFIED) {
            throw new BusinessException(ErrorCode.DOCUMENT_ALREADY_VERIFIED,
                    "이미 검증된 서류입니다");
        }

        // 서류 검증 승인
        document.verify(verifierId);

        // 모든 서류가 검증되었는지 확인하고 신청서 상태 업데이트
        checkAndUpdateApplicationStatus(document.getApplication());

        log.info("서류 검증 승인 완료: documentId={}", documentId);
        return DocumentResponse.from(document);
    }

    /**
     * 서류 검증 반려 / Reject document verification
     *
     * @param documentId 서류 ID
     * @param reason 반려 사유
     * @param verifierId 검증자 ID (관리자)
     * @return 반려된 서류 응답 DTO
     * @throws BusinessException 서류를 찾을 수 없는 경우
     */
    @Transactional
    public DocumentResponse rejectDocument(Long documentId, String reason, Long verifierId) {
        log.info("서류 검증 반려 시작: documentId={}, verifierId={}, reason={}", documentId, verifierId, reason);

        ApplicationDocument document = documentRepository.findById(documentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DOCUMENT_NOT_FOUND));

        // 서류 반려
        document.reject(reason, verifierId);

        // 신청서 상태를 서류 반려로 업데이트
        document.getApplication().rejectDocuments(reason);

        log.info("서류 검증 반려 완료: documentId={}", documentId);
        return DocumentResponse.from(document);
    }

    /**
     * 서류의 OCR 결과 조회 / Get OCR result for document
     *
     * @param documentId 서류 ID
     * @return OCR 결과 응답 DTO
     * @throws BusinessException 서류 또는 OCR 결과를 찾을 수 없는 경우
     */
    public OcrResultResponse getOcrResult(Long documentId) {
        ApplicationDocument document = documentRepository.findById(documentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DOCUMENT_NOT_FOUND));

        if (document.getOcrResultId() == null) {
            throw new BusinessException(ErrorCode.OCR_RESULT_NOT_FOUND,
                    "OCR 결과가 없습니다");
        }

        OcrResult ocrResult = ocrResultRepository.findById(document.getOcrResultId())
                .orElseThrow(() -> new BusinessException(ErrorCode.OCR_RESULT_NOT_FOUND));

        return OcrResultResponse.from(ocrResult);
    }

    /**
     * 신청서의 모든 서류가 검증되었는지 확인하고 상태 업데이트
     * Check if all documents are verified and update application status
     *
     * @param application 신청서
     */
    private void checkAndUpdateApplicationStatus(MembershipApplication application) {
        List<ApplicationDocument> documents = documentRepository.findByApplicationId(application.getId());

        // 필수 서류가 모두 있는지 확인
        if (!application.areAllDocumentsSubmitted()) {
            log.debug("필수 서류가 아직 제출되지 않음: applicationId={}", application.getId());
            return;
        }

        // 모든 서류가 검증되었는지 확인
        boolean allVerified = documents.stream()
                .allMatch(doc -> doc.getVerificationStatus() == VerificationStatus.VERIFIED);

        if (allVerified) {
            // 모든 서류가 검증되었으면 신청서 상태를 서류 승인으로 변경
            application.approveDocuments();
            log.info("모든 서류 검증 완료, 신청서 상태 업데이트: applicationId={}", application.getId());
        }
    }

    /**
     * OCR 재처리 요청 / Request OCR reprocessing
     *
     * @param documentId 서류 ID
     * @return OCR 결과 응답 DTO
     * @throws BusinessException 서류를 찾을 수 없거나 OCR 서비스가 없는 경우
     */
    @Transactional
    public OcrResultResponse reprocessOcr(Long documentId) {
        log.info("OCR 재처리 시작: documentId={}", documentId);

        ApplicationDocument document = documentRepository.findById(documentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DOCUMENT_NOT_FOUND));

        OcrService service = ocrService.orElseThrow(() ->
                new BusinessException(ErrorCode.OCR_SERVICE_NOT_AVAILABLE,
                        "OCR 서비스를 사용할 수 없습니다"));

        if (!service.isSupported(document)) {
            throw new BusinessException(ErrorCode.OCR_NOT_SUPPORTED,
                    "지원하지 않는 서류 유형입니다: " + document.getDocumentType());
        }

        // OCR 처리
        OcrResult ocrResult = service.processDocument(document);
        OcrResult savedOcrResult = ocrResultRepository.save(ocrResult);

        // 서류에 OCR 결과 연결
        document.linkOcrResult(savedOcrResult.getId());

        log.info("OCR 재처리 완료: documentId={}, ocrResultId={}", documentId, savedOcrResult.getId());
        return OcrResultResponse.from(savedOcrResult);
    }
}
