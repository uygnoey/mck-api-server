package kr.mclub.apiserver.membership.domain;

import jakarta.persistence.*;
import kr.mclub.apiserver.shared.domain.BaseTimeEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 이사 파트 / Director Part
 *
 * <p>이사진이 담당하는 파트(부서) 정보를 관리합니다.</p>
 *
 * <h3>이사 파트 예시</h3>
 * <ul>
 *   <li>총무 - 회비 관리, 회원 관리</li>
 *   <li>행사 - 정기/비정기 모임 기획 및 운영</li>
 *   <li>홍보 - SNS 운영, 대외 협력</li>
 *   <li>기술 - 웹사이트 운영, 시스템 관리</li>
 * </ul>
 *
 * @since 1.0
 */
@Entity
@Table(name = "director_parts", uniqueConstraints = {
        @UniqueConstraint(name = "uk_director_part_name", columnNames = {"part_name"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DirectorPart extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 파트명
     * 예: 총무, 행사, 홍보, 기술
     */
    @Column(name = "part_name", nullable = false, unique = true, length = 50)
    private String partName;

    /**
     * 파트 설명
     * 담당 업무 설명
     */
    @Column(name = "description", length = 500)
    private String description;

    /**
     * 정렬 순서
     * 화면 표시 시 사용
     */
    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    /**
     * 활성 여부
     * false면 폐지된 파트
     */
    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    /**
     * 생성한 관리자 ID (User 테이블 참조)
     * 회장만 파트 생성/수정/삭제 가능
     */
    @Column(name = "created_by_admin_id", nullable = false)
    private Long createdByAdminId;

    // === 생성자 ===

    /**
     * 이사 파트 생성
     *
     * @param partName 파트명
     * @param description 파트 설명
     * @param displayOrder 정렬 순서
     * @param createdByAdminId 생성한 관리자 ID
     */
    public DirectorPart(
            String partName,
            String description,
            Integer displayOrder,
            Long createdByAdminId
    ) {
        this.partName = partName;
        this.description = description;
        this.displayOrder = displayOrder;
        this.isActive = true;
        this.createdByAdminId = createdByAdminId;
    }

    // === 비즈니스 메서드 ===

    /**
     * 파트 정보 수정
     *
     * @param partName 파트명
     * @param description 파트 설명
     * @param displayOrder 정렬 순서
     */
    public void update(String partName, String description, Integer displayOrder) {
        this.partName = partName;
        this.description = description;
        this.displayOrder = displayOrder;
    }

    /**
     * 파트 비활성화 (폐지)
     */
    public void deactivate() {
        this.isActive = false;
    }

    /**
     * 파트 활성화 (재개설)
     */
    public void activate() {
        this.isActive = true;
    }
}
