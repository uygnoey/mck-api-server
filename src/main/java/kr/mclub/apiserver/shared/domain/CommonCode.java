package kr.mclub.apiserver.shared.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 공통 코드 엔티티 / Common Code Entity
 *
 * <p>시스템 전반에서 사용하는 공통 코드를 DB 테이블로 관리합니다.</p>
 * <p>Java Enum 대신 DB로 관리하여 코드 변경 시 Java 코드 수정 없이 동적으로 관리 가능합니다.</p>
 *
 * <h3>코드 그룹 예시</h3>
 * <ul>
 *   <li>VEHICLE_OWNERSHIP_TYPE - 차량 소유 형태 (개인, 법인, 리스 등)</li>
 *   <li>DOCUMENT_TYPE - 서류 타입 (차량등록증, 신분증 등)</li>
 *   <li>VERIFICATION_STATUS - 심사 상태 (대기, 승인, 반려 등)</li>
 *   <li>PAYMENT_TYPE - 결제 타입 (가입비, 연회비)</li>
 *   <li>PAYMENT_STATUS - 결제 상태 (대기, 확인, 취소)</li>
 *   <li>VEHICLE_STATUS - 차량 상태 (활성, 매각, 유예기간)</li>
 * </ul>
 *
 * @since 1.0
 */
@Entity
@Table(name = "common_codes", uniqueConstraints = {
        @UniqueConstraint(name = "uk_common_code", columnNames = {"code_group", "code"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommonCode extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 코드 그룹
     * 예: VEHICLE_OWNERSHIP_TYPE, DOCUMENT_TYPE, VERIFICATION_STATUS
     */
    @Column(name = "code_group", nullable = false, length = 50)
    private String codeGroup;

    /**
     * 코드 값
     * 예: PERSONAL, CORPORATE, VEHICLE_REGISTRATION
     */
    @Column(nullable = false, length = 50)
    private String code;

    /**
     * 한글 표시명
     * 예: 개인, 법인, 차량등록증
     */
    @Column(nullable = false, length = 100)
    private String name;

    /**
     * 정렬 순서
     * 화면에 표시할 때 사용
     */
    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    /**
     * 활성화 여부
     * false면 사용하지 않는 코드
     */
    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    /**
     * 코드 설명
     * 추가 정보나 사용 용도 설명
     */
    @Column(length = 500)
    private String description;

    /**
     * 추가 속성 1 (선택)
     * 코드별 추가 데이터 저장
     * 예: DocumentType의 경우 requiredForAll (필수 여부)
     */
    @Column(name = "attribute1", length = 100)
    private String attribute1;

    /**
     * 추가 속성 2 (선택)
     * 예: PaymentType의 경우 defaultAmount (기본 금액)
     */
    @Column(name = "attribute2", length = 100)
    private String attribute2;

    /**
     * 추가 속성 3 (선택)
     */
    @Column(name = "attribute3", length = 100)
    private String attribute3;

    // === 생성자 ===

    /**
     * 공통 코드 생성
     *
     * @param codeGroup 코드 그룹
     * @param code 코드 값
     * @param name 한글 표시명
     * @param displayOrder 정렬 순서
     * @param description 설명
     */
    public CommonCode(String codeGroup, String code, String name, Integer displayOrder, String description) {
        this.codeGroup = codeGroup;
        this.code = code;
        this.name = name;
        this.displayOrder = displayOrder;
        this.isActive = true;
        this.description = description;
    }

    // === 비즈니스 메서드 ===

    /**
     * 코드 활성화
     */
    public void activate() {
        this.isActive = true;
    }

    /**
     * 코드 비활성화
     */
    public void deactivate() {
        this.isActive = false;
    }

    /**
     * 코드 정보 수정
     *
     * @param name 한글 표시명
     * @param displayOrder 정렬 순서
     * @param description 설명
     */
    public void update(String name, Integer displayOrder, String description) {
        this.name = name;
        this.displayOrder = displayOrder;
        this.description = description;
    }

    /**
     * 추가 속성 설정
     *
     * @param attribute1 추가 속성 1
     * @param attribute2 추가 속성 2
     * @param attribute3 추가 속성 3
     */
    public void setAttributes(String attribute1, String attribute2, String attribute3) {
        this.attribute1 = attribute1;
        this.attribute2 = attribute2;
        this.attribute3 = attribute3;
    }
}
