package kr.mclub.apiserver.membership.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import kr.mclub.apiserver.shared.domain.BaseTimeEntity;
import kr.mclub.apiserver.user.domain.VehicleOwnershipType;

/**
 * 정회원 신청서 엔티티
 * Membership application entity
 */
@Entity
@Table(name = "membership_applications")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MembershipApplication extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    // 신청 정보
    @Column(name = "application_number", nullable = false, unique = true, length = 20)
    private String applicationNumber;  // 예: APP-2025-0001

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ApplicationStatus status = ApplicationStatus.DOCUMENT_PENDING;

    // 차량 소유 유형
    @Enumerated(EnumType.STRING)
    @Column(name = "vehicle_ownership_type", nullable = false, length = 30)
    private VehicleOwnershipType vehicleOwnershipType;

    // 신청자 정보 (신청 당시 스냅샷)
    @Column(name = "applicant_name", nullable = false, length = 50)
    private String applicantName;

    @Column(name = "applicant_phone", nullable = false, length = 20)
    private String applicantPhone;

    @Column(name = "applicant_email", length = 255)
    private String applicantEmail;

    // 차량 정보 (최초 등록 차량)
    @Column(name = "car_number", nullable = false, length = 20)
    private String carNumber;

    @Column(name = "vin_number", nullable = false, length = 50)
    private String vinNumber;

    @Column(name = "car_model", nullable = false, length = 100)
    private String carModel;

    // 처리 정보
    @Column(name = "reviewed_by")
    private Long reviewedBy;  // 검토한 관리자 ID

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;

    // 결제 정보
    @Column(name = "payment_amount", precision = 10, scale = 2)
    private BigDecimal paymentAmount;  // 결제 금액 (입회비 + 연회비)

    @Column(name = "target_year")
    private Integer targetYear;  // 연회비 대상 년도

    // 완료 정보
    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "assigned_member_number")
    private Integer assignedMemberNumber;  // 부여된 정회원 번호

    // 제출 서류 (One-to-Many)
    @OneToMany(mappedBy = "application", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ApplicationDocument> documents = new ArrayList<>();

    @Builder
    public MembershipApplication(Long userId, String applicationNumber,
                                  VehicleOwnershipType vehicleOwnershipType,
                                  String applicantName, String applicantPhone, String applicantEmail,
                                  String carNumber, String vinNumber, String carModel) {
        this.userId = userId;
        this.applicationNumber = applicationNumber;
        this.vehicleOwnershipType = vehicleOwnershipType;
        this.applicantName = applicantName;
        this.applicantPhone = applicantPhone;
        this.applicantEmail = applicantEmail;
        this.carNumber = carNumber;
        this.vinNumber = vinNumber;
        this.carModel = carModel;
        this.status = ApplicationStatus.DOCUMENT_PENDING;
    }

    /**
     * 신청 상태 변경
     * Change application status
     */
    public void changeStatus(ApplicationStatus newStatus) {
        this.status = newStatus;
    }

    /**
     * 서류 제출 완료로 변경
     * Mark documents as submitted
     */
    public void markDocumentsSubmitted() {
        this.status = ApplicationStatus.DOCUMENT_SUBMITTED;
    }

    /**
     * 서류 검토 시작
     * Start document review
     */
    public void startReview(Long reviewerId) {
        this.status = ApplicationStatus.UNDER_REVIEW;
        this.reviewedBy = reviewerId;
        this.reviewedAt = LocalDateTime.now();
    }

    /**
     * 서류 승인
     * Approve documents
     */
    public void approveDocuments() {
        this.status = ApplicationStatus.DOCUMENT_APPROVED;
    }

    /**
     * 서류 반려
     * Reject documents
     */
    public void rejectDocuments(String reason) {
        this.status = ApplicationStatus.DOCUMENT_REJECTED;
        this.rejectionReason = reason;
    }

    /**
     * 신청서 승인 (서류 승인 후 결제 대기로 전환)
     * Approve application (approve documents and move to payment pending)
     */
    public void approve(Long adminId) {
        this.reviewedBy = adminId;
        this.reviewedAt = LocalDateTime.now();
        this.status = ApplicationStatus.DOCUMENT_APPROVED;
    }

    /**
     * 신청서 반려
     * Reject application
     */
    public void reject(String reason, Long adminId) {
        this.reviewedBy = adminId;
        this.reviewedAt = LocalDateTime.now();
        this.rejectionReason = reason;
        this.status = ApplicationStatus.DOCUMENT_REJECTED;
    }

    /**
     * 결제 대기 상태로 변경
     * Change status to payment pending
     */
    public void markPaymentPending(BigDecimal amount, Integer year) {
        this.status = ApplicationStatus.PAYMENT_PENDING;
        this.paymentAmount = amount;
        this.targetYear = year;
    }

    /**
     * 결제 확인 완료
     * Confirm payment
     */
    public void confirmPayment() {
        this.status = ApplicationStatus.PAYMENT_CONFIRMED;
    }

    /**
     * 신청 완료 및 정회원 전환
     * Complete application and convert to regular member
     */
    public void complete(Integer memberNumber) {
        this.status = ApplicationStatus.COMPLETED;
        this.approvedAt = LocalDateTime.now();
        this.assignedMemberNumber = memberNumber;
    }

    /**
     * 신청 취소
     * Cancel application
     */
    public void cancel(String reason) {
        this.status = ApplicationStatus.CANCELLED;
        this.rejectionReason = reason;
    }

    /**
     * 서류 추가
     * Add document
     */
    public void addDocument(ApplicationDocument document) {
        documents.add(document);
        document.setApplication(this);
    }

    /**
     * 서류 제출 완료 여부 확인
     * Check if all required documents are submitted
     */
    public boolean areAllDocumentsSubmitted() {
        if (documents.isEmpty()) {
            return false;
        }

        // 차량 소유 유형별 필수 서류 개수 확인
        int requiredDocCount = getRequiredDocumentCount();
        return documents.size() >= requiredDocCount;
    }

    /**
     * 필수 서류 개수 반환
     * Get required document count based on ownership type
     */
    private int getRequiredDocumentCount() {
        return switch (vehicleOwnershipType) {
            case PERSONAL -> 2;  // 차량등록증 + 신분증
            case CORPORATE -> 3;  // 차량등록증 + 신분증 + 사업자등록증
            case LEASE, RENTAL -> 3;  // 차량등록증 + 신분증 + 계약서
            case CORPORATE_LEASE, CORPORATE_RENTAL -> 4;  // 차량등록증 + 신분증 + 사업자등록증 + 계약서
        };
    }
}
