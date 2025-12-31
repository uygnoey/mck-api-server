package kr.mclub.apiserver.membership.domain;

import jakarta.persistence.*;
import kr.mclub.apiserver.shared.domain.BaseTimeEntity;
import kr.mclub.apiserver.shared.domain.CommonCode;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 정회원 신청 서류 / Application Document
 *
 * <p>정회원 가입 신청 시 제출하는 서류 파일 정보를 관리합니다.</p>
 *
 * <h3>서류 종류 (DocumentType)</h3>
 * <ul>
 *   <li>VEHICLE_REGISTRATION - 차량등록증 (필수)</li>
 *   <li>ID_CARD - 신분증 (필수)</li>
 *   <li>BUSINESS_LICENSE - 사업자등록증 (법인 소유 시)</li>
 *   <li>EMPLOYMENT_CERTIFICATE - 재직증명서 (법인 소유 시)</li>
 *   <li>LEASE_CONTRACT - 리스 계약서 (리스 시)</li>
 *   <li>RENTAL_CONTRACT - 렌트 계약서 (렌트 시)</li>
 * </ul>
 *
 * @since 1.0
 */
@Entity
@Table(name = "application_documents")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ApplicationDocument extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 신청서 ID (MembershipApplication 테이블 참조)
     */
    @Column(name = "application_id", nullable = false)
    private Long applicationId;

    /**
     * 서류 타입 (CommonCode: DOCUMENT_TYPE)
     * - VEHICLE_REGISTRATION: 차량등록증
     * - ID_CARD: 신분증
     * - BUSINESS_LICENSE: 사업자등록증
     * - EMPLOYMENT_CERTIFICATE: 재직증명서
     * - LEASE_CONTRACT: 리스 계약서
     * - RENTAL_CONTRACT: 렌트 계약서
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_type_code_id", nullable = false)
    private CommonCode documentType;

    /**
     * 파일 저장 경로 (DigitalOcean Spaces URL)
     * 예: https://mclub-storage.sgp1.digitaloceanspaces.com/documents/2025/01/abc123.jpg
     */
    @Column(name = "file_url", nullable = false, length = 500)
    private String fileUrl;

    /**
     * 원본 파일명
     * 예: 차량등록증_홍길동.jpg
     */
    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    /**
     * 파일 크기 (bytes)
     */
    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    /**
     * 파일 MIME 타입
     * 예: image/jpeg, application/pdf
     */
    @Column(name = "content_type", nullable = false, length = 100)
    private String contentType;

    /**
     * OCR 검증 완료 여부
     * true: OCR 처리 완료, false: OCR 처리 대기/실패
     */
    @Column(name = "is_verified", nullable = false)
    private Boolean isVerified;

    /**
     * 업로드 일시
     */
    @Column(name = "uploaded_at", nullable = false)
    private LocalDateTime uploadedAt;

    // === 생성자 ===

    /**
     * 신청 서류 생성
     *
     * @param applicationId 신청서 ID
     * @param documentType 서류 타입
     * @param fileUrl 파일 저장 경로
     * @param fileName 원본 파일명
     * @param fileSize 파일 크기
     * @param contentType MIME 타입
     */
    public ApplicationDocument(
            Long applicationId,
            CommonCode documentType,
            String fileUrl,
            String fileName,
            Long fileSize,
            String contentType
    ) {
        this.applicationId = applicationId;
        this.documentType = documentType;
        this.fileUrl = fileUrl;
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.contentType = contentType;
        this.isVerified = false;
        this.uploadedAt = LocalDateTime.now();
    }

    // === 비즈니스 메서드 ===

    /**
     * OCR 검증 완료 처리
     */
    public void markAsVerified() {
        this.isVerified = true;
    }

    /**
     * OCR 검증 실패 처리
     */
    public void markAsUnverified() {
        this.isVerified = false;
    }
}
