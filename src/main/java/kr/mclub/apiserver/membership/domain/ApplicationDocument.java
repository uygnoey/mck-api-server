package kr.mclub.apiserver.membership.domain;

import java.time.LocalDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import kr.mclub.apiserver.shared.domain.BaseTimeEntity;

/**
 * 정회원 신청 서류 엔티티
 * Application document entity
 */
@Entity
@Table(name = "application_documents")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ApplicationDocument extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", nullable = false)
    private MembershipApplication application;

    // 서류 정보
    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", nullable = false, length = 30)
    private DocumentType documentType;

    @Column(name = "file_url", nullable = false, length = 500)
    private String fileUrl;  // S3/Spaces 저장 URL

    @Column(name = "original_file_name", nullable = false, length = 255)
    private String originalFileName;

    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @Column(name = "content_type", nullable = false, length = 100)
    private String contentType;

    // 검증 상태
    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status", nullable = false, length = 20)
    private VerificationStatus verificationStatus = VerificationStatus.PENDING;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @Column(name = "verified_by")
    private Long verifiedBy;  // 검증한 관리자 ID

    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;

    // OCR 연결
    @Column(name = "ocr_result_id")
    private Long ocrResultId;  // OCR 결과 ID

    @Builder
    public ApplicationDocument(MembershipApplication application, DocumentType documentType,
                                String fileUrl, String originalFileName,
                                Long fileSize, String contentType) {
        this.application = application;
        this.documentType = documentType;
        this.fileUrl = fileUrl;
        this.originalFileName = originalFileName;
        this.fileSize = fileSize;
        this.contentType = contentType;
        this.verificationStatus = VerificationStatus.PENDING;
    }

    /**
     * Application 설정 (양방향 관계)
     * Set application for bidirectional relationship
     */
    protected void setApplication(MembershipApplication application) {
        this.application = application;
    }

    /**
     * 서류 검증 완료
     * Mark document as verified
     */
    public void verify(Long verifierId) {
        this.verificationStatus = VerificationStatus.VERIFIED;
        this.verifiedAt = LocalDateTime.now();
        this.verifiedBy = verifierId;
    }

    /**
     * 서류 검증 반려
     * Reject document verification
     */
    public void reject(String reason, Long verifierId) {
        this.verificationStatus = VerificationStatus.REJECTED;
        this.rejectionReason = reason;
        this.verifiedAt = LocalDateTime.now();
        this.verifiedBy = verifierId;
    }

    /**
     * OCR 결과 연결
     * Link OCR result
     */
    public void linkOcrResult(Long ocrResultId) {
        this.ocrResultId = ocrResultId;
    }

    /**
     * 검증 상태 변경
     * Change verification status
     */
    public void changeVerificationStatus(VerificationStatus newStatus) {
        this.verificationStatus = newStatus;
    }
}
