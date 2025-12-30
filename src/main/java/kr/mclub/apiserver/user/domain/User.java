package kr.mclub.apiserver.user.domain;

import jakarta.persistence.*;
import kr.mclub.apiserver.shared.domain.BaseTimeEntity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 사용자 엔티티
 * User entity
 */
@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_number", unique = true)
    private Integer memberNumber;  // 정회원 번호 (영구 소유, NULL이면 준회원)

    @Column(name = "real_name", nullable = false, length = 50)
    private String realName;  // 실명

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(name = "profile_image_url", length = 500)
    private String profileImageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "grade_id", nullable = false)
    private UserGrade grade;  // 등급

    @Enumerated(EnumType.STRING)
    @Column(name = "associate_status", length = 20)
    private AssociateStatus associateStatus;  // 준회원 상태

    @Column(name = "director_part_id")
    private Long directorPartId;  // 이사인 경우 담당 파트 ID

    @Column(name = "partner_company_name", length = 100)
    private String partnerCompanyName;  // 파트너사 업체명

    @Enumerated(EnumType.STRING)
    @Column(name = "exemption_type", nullable = false, length = 20)
    private ExemptionType exemptionType = ExemptionType.NONE;  // 면제 유형

    @Column(name = "exemption_reason", length = 200)
    private String exemptionReason;  // 면제 사유

    @Column(name = "exemption_year")
    private Integer exemptionYear;  // 1회성 면제 적용 년도

    @Column(name = "is_withdrawn", nullable = false)
    private boolean isWithdrawn = false;

    @Column(name = "withdrawn_at")
    private LocalDateTime withdrawnAt;

    @Column(name = "withdrawal_reason", length = 500)
    private String withdrawalReason;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OAuthAccount> oAuthAccounts = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PasskeyCredential> passkeyCredentials = new ArrayList<>();

    @Builder
    public User(String realName, String email, String phoneNumber, String profileImageUrl,
                UserGrade grade, AssociateStatus associateStatus) {
        this.realName = realName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.profileImageUrl = profileImageUrl;
        this.grade = grade;
        this.associateStatus = associateStatus;
        this.exemptionType = ExemptionType.NONE;
    }

    /**
     * 화면에 표시할 이름 반환
     * Returns display name: "610 홍길동" or "610 홍길동 (회장)"
     */
    public String getDisplayName() {
        StringBuilder sb = new StringBuilder();

        // 정회원 번호가 있으면 앞에 표시
        if (memberNumber != null) {
            sb.append(memberNumber).append(" ");
        }

        sb.append(realName);

        // 등급 접미사가 있으면 추가
        if (grade != null && grade.getDisplaySuffix() != null) {
            sb.append(" ").append(grade.getDisplaySuffix());
        }

        return sb.toString();
    }

    /**
     * 프로필 업데이트
     * Update profile information
     */
    public void updateProfile(String phoneNumber, String profileImageUrl) {
        this.phoneNumber = phoneNumber;
        this.profileImageUrl = profileImageUrl;
    }

    /**
     * 등급 변경
     * Change user grade
     */
    public void changeGrade(UserGrade newGrade) {
        this.grade = newGrade;

        // 등급 변경 시 준회원 상태 정리
        if (!newGrade.isMember()) {
            this.associateStatus = null;
        }
    }

    /**
     * 정회원 번호 부여
     * Assign member number (when becoming regular member)
     */
    public void assignMemberNumber(Integer memberNumber) {
        this.memberNumber = memberNumber;
        this.associateStatus = null;  // 준회원 상태 정리
    }

    /**
     * 준회원 상태 변경
     * Change associate status
     */
    public void changeAssociateStatus(AssociateStatus status) {
        this.associateStatus = status;
    }

    /**
     * 이사 파트 지정
     * Assign director part
     */
    public void assignDirectorPart(Long partId) {
        this.directorPartId = partId;
    }

    /**
     * 파트너사 정보 설정
     * Set partner company information
     */
    public void setPartnerInfo(String companyName) {
        this.partnerCompanyName = companyName;
    }

    /**
     * 연회비 면제 설정
     * Grant annual fee exemption
     */
    public void grantExemption(ExemptionType type, String reason, Integer year) {
        this.exemptionType = type;
        this.exemptionReason = reason;
        this.exemptionYear = year;
    }

    /**
     * 연회비 면제 해제
     * Revoke annual fee exemption
     */
    public void revokeExemption() {
        this.exemptionType = ExemptionType.NONE;
        this.exemptionReason = null;
        this.exemptionYear = null;
    }

    /**
     * 마지막 로그인 시간 업데이트
     * Update last login time
     */
    public void updateLastLoginAt() {
        this.lastLoginAt = LocalDateTime.now();
    }

    /**
     * 회원 탈퇴
     * Withdraw membership
     */
    public void withdraw(String reason) {
        this.isWithdrawn = true;
        this.withdrawnAt = LocalDateTime.now();
        this.withdrawalReason = reason;
        this.isActive = false;
    }

    /**
     * OAuth 계정 추가
     * Add OAuth account
     */
    public void addOAuthAccount(OAuthAccount account) {
        oAuthAccounts.add(account);
        account.setUser(this);
    }

    /**
     * Passkey 추가
     * Add Passkey credential
     */
    public void addPasskeyCredential(PasskeyCredential credential) {
        passkeyCredentials.add(credential);
        credential.setUser(this);
    }

    /**
     * 특정 연도에 면제 대상인지 확인
     * Check if user is exempt for a specific year
     */
    public boolean isExemptForYear(int year) {
        if (exemptionType == ExemptionType.PERMANENT) {
            return true;
        }
        if (exemptionType == ExemptionType.ONE_TIME && exemptionYear != null) {
            return exemptionYear.equals(year);
        }
        return false;
    }
}
