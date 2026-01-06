package kr.mclub.apiserver.membership.api.dto;

import kr.mclub.apiserver.membership.domain.DocumentType;

/**
 * 서류 업로드 요청 DTO
 * Document upload request DTO
 */
public record DocumentUploadRequest(
        DocumentType documentType,  // 서류 유형
        String fileUrl,  // 파일 URL (S3/Spaces)
        String originalFileName,  // 원본 파일명
        Long fileSize,  // 파일 크기
        String contentType  // 컨텐츠 타입
) {
}
