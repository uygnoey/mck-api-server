package kr.mclub.apiserver.membership.api.dto;

import java.time.LocalDateTime;

import kr.mclub.apiserver.membership.domain.ApplicationDocument;
import kr.mclub.apiserver.membership.domain.DocumentType;
import kr.mclub.apiserver.membership.domain.VerificationStatus;

/**
 * 서류 응답 DTO
 * Document response DTO
 */
public record DocumentResponse(
        Long id,
        Long applicationId,
        DocumentType documentType,
        String fileUrl,
        String originalFileName,
        Long fileSize,
        String contentType,
        VerificationStatus verificationStatus,
        LocalDateTime verifiedAt,
        Long verifiedBy,
        String rejectionReason,
        Long ocrResultId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    /**
     * 엔티티로부터 응답 DTO 생성
     * Create response DTO from entity
     */
    public static DocumentResponse from(ApplicationDocument document) {
        return new DocumentResponse(
                document.getId(),
                document.getApplication().getId(),
                document.getDocumentType(),
                document.getFileUrl(),
                document.getOriginalFileName(),
                document.getFileSize(),
                document.getContentType(),
                document.getVerificationStatus(),
                document.getVerifiedAt(),
                document.getVerifiedBy(),
                document.getRejectionReason(),
                document.getOcrResultId(),
                document.getCreatedAt(),
                document.getUpdatedAt()
        );
    }
}
