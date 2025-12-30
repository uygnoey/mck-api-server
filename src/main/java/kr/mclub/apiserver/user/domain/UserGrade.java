package kr.mclub.apiserver.user.domain;

import jakarta.persistence.*;
import kr.mclub.apiserver.shared.domain.BaseTimeEntity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 사용자 등급 (동적 관리)
 * User grade entity - dynamically managed via database
 */
@Entity
@Table(name = "user_grades")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserGrade extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String code;  // 등급 코드 (예: DEVELOPER, ADVISOR, REGULAR)

    @Column(nullable = false, length = 50)
    private String name;  // 등급명 (예: 개발자, 고문, 정회원)

    @Column(name = "role_name", nullable = false, unique = true, length = 50)
    private String roleName;  // Spring Security Role (예: ROLE_DEVELOPER)

    @Column(name = "permission_level", nullable = false)
    private Integer permissionLevel;  // 권한 레벨 (높을수록 상위 등급)

    @Column(name = "is_executive", nullable = false)
    private boolean isExecutive = false;  // 임원 여부

    @Column(name = "is_staff", nullable = false)
    private boolean isStaff = false;  // 운영진 여부

    @Column(name = "is_member", nullable = false)
    private boolean isMember = false;  // 정/준회원 여부

    @Column(name = "requires_annual_fee", nullable = false)
    private boolean requiresAnnualFee = true;  // 연회비 필요 여부

    @Column(name = "is_system_grade", nullable = false)
    private boolean isSystemGrade = false;  // 시스템 등급 (삭제 불가)

    @Column(name = "display_suffix", length = 20)
    private String displaySuffix;  // 표시 접미사 (예: "(고문)", "(회장)")

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;  // 표시 순서

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Column(name = "created_by")
    private Long createdBy;  // 생성자 ID (NULL이면 시스템)

    @Builder
    public UserGrade(String code, String name, String roleName, Integer permissionLevel,
                     boolean isExecutive, boolean isStaff, boolean isMember,
                     boolean requiresAnnualFee, boolean isSystemGrade,
                     String displaySuffix, Integer displayOrder, Long createdBy) {
        this.code = code;
        this.name = name;
        this.roleName = roleName;
        this.permissionLevel = permissionLevel;
        this.isExecutive = isExecutive;
        this.isStaff = isStaff;
        this.isMember = isMember;
        this.requiresAnnualFee = requiresAnnualFee;
        this.isSystemGrade = isSystemGrade;
        this.displaySuffix = displaySuffix;
        this.displayOrder = displayOrder;
        this.createdBy = createdBy;
    }

    /**
     * 등급 비교: 이 등급이 대상 등급보다 높거나 같은지
     * Check if this grade is higher or equal to another grade
     */
    public boolean isHigherOrEqualTo(UserGrade other) {
        return this.permissionLevel >= other.getPermissionLevel();
    }

    /**
     * 등급 비교: 이 등급이 대상 등급보다 높은지
     * Check if this grade is strictly higher than another grade
     */
    public boolean isHigherThan(UserGrade other) {
        return this.permissionLevel > other.getPermissionLevel();
    }

    /**
     * 등급 정보 업데이트
     * Update grade information
     */
    public void update(String name, Integer permissionLevel, boolean isExecutive,
                       boolean isStaff, String displaySuffix, Integer displayOrder) {
        this.name = name;
        this.permissionLevel = permissionLevel;
        this.isExecutive = isExecutive;
        this.isStaff = isStaff;
        this.displaySuffix = displaySuffix;
        this.displayOrder = displayOrder;
    }

    /**
     * 등급 비활성화
     * Deactivate grade
     */
    public void deactivate() {
        this.isActive = false;
    }

    /**
     * 등급 활성화
     * Activate grade
     */
    public void activate() {
        this.isActive = true;
    }
}
