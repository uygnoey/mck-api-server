package kr.mclub.apiserver.membership.domain;

import java.util.Map;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import kr.mclub.apiserver.shared.domain.BaseTimeEntity;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * 이사 파트 엔티티
 * Director part entity
 */
@Entity
@Table(name = "director_parts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DirectorPart extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 파트 정보
    @Column(nullable = false, unique = true, length = 50)
    private String name;  // 파트명 (예: 행사, 홍보, 총무)

    @Column(length = 200)
    private String description;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder = 0;  // 표시 순서

    // 권한 설정
    @Column(name = "can_manage_members", nullable = false)
    private boolean canManageMembers = false;  // 회원 관리 권한

    @Column(name = "can_manage_posts", nullable = false)
    private boolean canManagePosts = true;  // 게시글 관리 권한

    @Column(name = "can_manage_events", nullable = false)
    private boolean canManageEvents = false;  // 이벤트 관리 권한

    @Column(name = "can_assign_sub_permissions", nullable = false)
    private boolean canAssignSubPermissions = false;  // 세부 권한 지정 가능

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "custom_permissions", columnDefinition = "jsonb")
    private Map<String, Object> customPermissions;  // 추가 커스텀 권한 (JSON)

    // 관리
    @Column(name = "created_by", nullable = false)
    private Long createdBy;  // 생성자 (회장 ID)

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Builder
    public DirectorPart(String name, String description, Integer displayOrder, Long createdBy) {
        this.name = name;
        this.description = description;
        this.displayOrder = displayOrder != null ? displayOrder : 0;
        this.createdBy = createdBy;
        this.canManageMembers = false;
        this.canManagePosts = true;
        this.canManageEvents = false;
        this.canAssignSubPermissions = false;
        this.isActive = true;
    }

    /**
     * 파트 정보 업데이트
     * Update part information
     */
    public void update(String name, String description, Integer displayOrder) {
        this.name = name;
        this.description = description;
        if (displayOrder != null) {
            this.displayOrder = displayOrder;
        }
    }

    /**
     * 권한 설정
     * Set permissions
     */
    public void setPermissions(boolean canManageMembers, boolean canManagePosts,
                                boolean canManageEvents, boolean canAssignSubPermissions) {
        this.canManageMembers = canManageMembers;
        this.canManagePosts = canManagePosts;
        this.canManageEvents = canManageEvents;
        this.canAssignSubPermissions = canAssignSubPermissions;
    }

    /**
     * 커스텀 권한 설정
     * Set custom permissions
     */
    public void setCustomPermissions(Map<String, Object> permissions) {
        this.customPermissions = permissions;
    }

    /**
     * 파트 비활성화
     * Deactivate part
     */
    public void deactivate() {
        this.isActive = false;
    }

    /**
     * 파트 활성화
     * Activate part
     */
    public void activate() {
        this.isActive = true;
    }
}
