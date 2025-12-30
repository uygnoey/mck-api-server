# BMW M Club Korea API Server - Detailed Design
# BMW M Club Korea API 서버 - 상세 설계서

## 1. Module Package Structure / 모듈 패키지 구조

### 1.1 Complete Package Tree / 전체 패키지 트리

```
kr.mclub.apiserver/
├── MckApiServerApplication.java          # Main Application Entry
├── shared/                               # Shared Kernel (공유 컴포넌트)
│   ├── domain/
│   │   ├── BaseEntity.java              # 공통 엔티티 (id, createdAt, updatedAt)
│   │   ├── BaseTimeEntity.java          # 시간 관련 엔티티
│   │   └── DomainEvent.java             # 도메인 이벤트 인터페이스
│   ├── exception/
│   │   ├── BusinessException.java       # 비즈니스 예외 기본 클래스
│   │   ├── ErrorCode.java               # 에러 코드 정의
│   │   └── GlobalExceptionHandler.java  # 전역 예외 처리
│   ├── security/
│   │   ├── SecurityConfig.java          # 보안 설정
│   │   ├── JwtTokenProvider.java        # JWT 토큰 관리
│   │   └── CurrentUser.java             # 현재 사용자 어노테이션
│   └── util/
│       ├── ApiResponse.java             # 표준 API 응답
│       └── PageResponse.java            # 페이지네이션 응답
│
├── user/                                 # User Module
│   ├── package-info.java                # 모듈 메타데이터
│   ├── UserModule.java                  # 모듈 설정
│   ├── domain/
│   │   ├── User.java                    # 사용자 엔티티
│   │   ├── UserGrade.java               # 🆕 등급 엔티티 (동적 관리, DB 테이블)
│   │   ├── AssociateStatus.java         # 준회원 상태 Enum
│   │   ├── ExemptionType.java           # 면제 유형 Enum
│   │   ├── OAuthProvider.java           # OAuth 제공자 Enum
│   │   ├── OAuthAccount.java            # OAuth 연결 계정
│   │   └── PasskeyCredential.java       # Passkey 자격증명
│   ├── repository/
│   │   ├── UserRepository.java
│   │   ├── UserGradeRepository.java     # 🆕 등급 Repository
│   │   ├── OAuthAccountRepository.java
│   │   └── PasskeyCredentialRepository.java
│   ├── service/
│   │   ├── UserService.java
│   │   ├── OAuth2UserService.java
│   │   └── PasskeyService.java          # Passkey 인증 서비스
│   ├── api/
│   │   ├── AuthController.java          # OAuth2, Passkey 인증
│   │   ├── UserController.java
│   │   ├── ProfileController.java       # 프로필 관리
│   │   └── dto/
│   │       ├── UserProfileRequest.java
│   │       ├── UserProfileResponse.java
│   │       ├── PasskeyRegistrationRequest.java
│   │       └── RealNameChangeRequest.java
│   ├── event/
│   │   ├── UserRegisteredEvent.java
│   │   ├── UserGradeChangedEvent.java
│   │   └── UserEventPublisher.java
│   └── internal/                        # 내부 전용 (다른 모듈 접근 불가)
│       └── OAuth2TokenService.java
│
├── membership/                           # 🆕 Membership Module (정회원 가입)
│   ├── package-info.java
│   ├── MembershipModule.java
│   ├── domain/
│   │   ├── MembershipApplication.java   # 정회원 신청서
│   │   ├── VehicleOwnershipType.java    # 차량 소유 유형 Enum
│   │   ├── ApplicationDocument.java     # 제출 서류
│   │   ├── DocumentType.java            # 서류 유형 Enum
│   │   ├── OcrResult.java               # OCR 추출 결과
│   │   ├── VerificationStatus.java      # 검증 상태 Enum
│   │   ├── PaymentRecord.java           # 입금 기록
│   │   ├── PaymentType.java             # 결제 유형 Enum
│   │   ├── PaymentStatus.java           # 결제 상태 Enum
│   │   ├── MembershipPeriod.java        # 회원권 기간
│   │   ├── DirectorPart.java            # 이사 파트 (동적)
│   │   ├── MemberVehicle.java           # 회원 차량 (다중)
│   │   ├── VehicleStatus.java           # 차량 상태 Enum
│   │   └── AnnualFeeConfig.java         # 연회비 기간 설정
│   ├── repository/
│   │   ├── MembershipApplicationRepository.java
│   │   ├── ApplicationDocumentRepository.java
│   │   ├── PaymentRecordRepository.java
│   │   ├── MembershipPeriodRepository.java
│   │   ├── DirectorPartRepository.java
│   │   ├── MemberVehicleRepository.java
│   │   └── AnnualFeeConfigRepository.java
│   ├── service/
│   │   ├── MembershipApplicationService.java
│   │   ├── DocumentVerificationService.java
│   │   ├── OcrService.java              # OCR 인터페이스
│   │   ├── PaymentService.java
│   │   ├── OpenBankingService.java      # 오픈뱅킹 연동
│   │   ├── MembershipRenewalService.java
│   │   ├── DirectorPartService.java
│   │   ├── VehicleManagementService.java
│   │   └── AnnualFeeService.java        # 연회비 기간/이월 관리
│   ├── api/
│   │   ├── MembershipController.java
│   │   ├── PaymentController.java
│   │   ├── VehicleController.java
│   │   └── dto/...
│   ├── event/
│   │   ├── MembershipAppliedEvent.java
│   │   ├── MembershipApprovedEvent.java
│   │   ├── MembershipRejectedEvent.java
│   │   ├── MembershipExpiredEvent.java
│   │   ├── PaymentConfirmedEvent.java
│   │   ├── VehicleAddedEvent.java
│   │   ├── VehicleSoldEvent.java
│   │   └── VehicleGracePeriodExpiredEvent.java
│   └── scheduler/
│       ├── MembershipExpirationScheduler.java
│       └── VehicleGracePeriodScheduler.java
│
├── landing/                             # Landing Module
│   ├── package-info.java
│   ├── LandingModule.java
│   ├── domain/
│   │   ├── LandingContent.java
│   │   ├── ClubHistory.java
│   │   ├── Executive.java
│   │   ├── Event.java
│   │   └── InstagramPost.java
│   ├── repository/
│   │   ├── LandingContentRepository.java
│   │   ├── ClubHistoryRepository.java
│   │   ├── ExecutiveRepository.java
│   │   ├── EventRepository.java
│   │   └── InstagramPostRepository.java
│   ├── service/
│   │   ├── LandingService.java
│   │   ├── HistoryService.java
│   │   ├── ExecutiveService.java
│   │   ├── EventService.java
│   │   └── InstagramSyncService.java
│   ├── api/
│   │   ├── LandingController.java
│   │   ├── HistoryController.java
│   │   ├── ExecutiveController.java
│   │   ├── EventController.java
│   │   └── dto/
│   │       └── ... (각 도메인별 DTO)
│   ├── webhook/
│   │   └── InstagramWebhookController.java
│   └── event/
│       ├── EventCreatedEvent.java
│       └── InstagramPostSyncedEvent.java
│
├── community/                           # Community Module
│   ├── package-info.java
│   ├── CommunityModule.java
│   ├── domain/
│   │   ├── Board.java
│   │   ├── Post.java
│   │   ├── Comment.java
│   │   ├── Attachment.java
│   │   ├── PostLike.java
│   │   ├── Bookmark.java
│   │   ├── PermissionGroup.java         # 🆕 권한 그룹
│   │   ├── BoardPermission.java         # 🆕 권한 Enum
│   │   ├── BoardPermissionMapping.java  # 🆕 게시판-권한그룹 매핑
│   │   └── UserPermissionGroup.java     # 🆕 사용자-권한그룹 매핑
│   ├── repository/
│   │   ├── BoardRepository.java
│   │   ├── PostRepository.java
│   │   ├── CommentRepository.java
│   │   ├── AttachmentRepository.java
│   │   ├── PostLikeRepository.java
│   │   ├── BookmarkRepository.java
│   │   ├── PermissionGroupRepository.java      # 🆕
│   │   ├── BoardPermissionMappingRepository.java # 🆕
│   │   └── UserPermissionGroupRepository.java  # 🆕
│   ├── service/
│   │   ├── BoardService.java
│   │   ├── PostService.java
│   │   ├── CommentService.java
│   │   ├── FileUploadService.java
│   │   └── BoardPermissionChecker.java  # 🆕 권한 체크 서비스
│   ├── api/
│   │   ├── BoardController.java
│   │   ├── PostController.java
│   │   ├── CommentController.java
│   │   └── dto/
│   │       └── ...
│   └── event/
│       ├── PostCreatedEvent.java
│       ├── CommentAddedEvent.java
│       └── PostLikedEvent.java
│
├── admin/                               # Admin Module
│   ├── package-info.java
│   ├── AdminModule.java
│   ├── domain/
│   │   ├── AdminAction.java
│   │   ├── AuditLog.java                # 🆕 감사 로그
│   │   └── DashboardMetric.java
│   ├── repository/
│   │   ├── AdminActionRepository.java
│   │   └── AuditLogRepository.java      # 🆕
│   ├── service/
│   │   ├── MemberManagementService.java
│   │   ├── MembershipAdminService.java  # 🆕 정회원 승인/반려
│   │   ├── BoardManagementService.java
│   │   ├── PermissionService.java
│   │   ├── DirectorPartService.java     # 🆕 이사 파트 관리
│   │   ├── ExemptionService.java        # 🆕 면제 부여/해제
│   │   ├── AnnualFeeAdminService.java   # 🆕 연회비 설정 관리
│   │   └── DashboardService.java
│   ├── api/
│   │   ├── MemberManagementController.java
│   │   ├── MembershipAdminController.java  # 🆕
│   │   ├── BoardManagementController.java
│   │   ├── PermissionController.java
│   │   ├── DirectorPartController.java     # 🆕
│   │   ├── AnnualFeeConfigController.java  # 🆕
│   │   ├── DashboardController.java
│   │   └── dto/
│   │       └── ...
│   └── event/
│       └── AdminActionLoggedEvent.java
│
├── chat/                                # Chat Module
│   ├── package-info.java
│   ├── ChatModule.java
│   ├── domain/
│   │   ├── ChatRoom.java
│   │   ├── ChatMessage.java
│   │   ├── ChatParticipant.java
│   │   └── ChatRoomType.java
│   ├── repository/
│   │   ├── ChatRoomRepository.java
│   │   ├── ChatMessageRepository.java
│   │   └── ChatParticipantRepository.java
│   ├── service/
│   │   ├── ChatRoomService.java
│   │   └── ChatMessageService.java
│   ├── grpc/
│   │   ├── ChatGrpcService.java
│   │   └── proto/
│   │       └── chat.proto
│   ├── api/
│   │   ├── ChatRestController.java
│   │   └── dto/
│   │       └── ...
│   └── event/
│       ├── ChatRoomCreatedEvent.java
│       └── MessageSentEvent.java
│
├── navercafe/                           # NaverCafe Module
│   ├── package-info.java
│   ├── NaverCafeModule.java
│   ├── domain/
│   │   ├── CafePost.java
│   │   └── CafeSyncLog.java
│   ├── repository/
│   │   ├── CafePostRepository.java
│   │   └── CafeSyncLogRepository.java
│   ├── service/
│   │   ├── CafeFetchService.java
│   │   └── CafePostingService.java
│   ├── webhook/
│   │   └── NaverCafeWebhookController.java
│   ├── api/
│   │   ├── NaverCafeController.java
│   │   └── dto/
│   │       └── ...
│   └── event/
│       ├── CafePostSyncedEvent.java
│       └── CrossPostingCompletedEvent.java
│
└── notification/                        # 🆕 Notification Module (알림)
    ├── package-info.java
    ├── NotificationModule.java
    ├── domain/
    │   ├── NotificationPreference.java  # 사용자별 알림 설정
    │   ├── NotificationLog.java         # 알림 발송 기록
    │   └── NotificationType.java        # 알림 유형 Enum
    ├── repository/
    │   ├── NotificationPreferenceRepository.java
    │   └── NotificationLogRepository.java
    ├── service/
    │   ├── NotificationService.java     # 알림 발송 조율
    │   ├── NotificationChannel.java     # 채널 인터페이스
    │   ├── EmailNotificationChannel.java
    │   ├── PushNotificationChannel.java # FCM/APNs
    │   └── SmsNotificationChannel.java
    ├── api/
    │   ├── NotificationController.java
    │   └── dto/...
    └── event/
        └── NotificationEventListener.java  # 모든 모듈 이벤트 구독
```

---

## 2. Domain Entities / 도메인 엔티티

### 2.1 User Module Entities / 사용자 모듈 엔티티

```java
// User.java
// 사용자 엔티티 - 8단계 등급 체계, 정회원 번호, 탈퇴 관리 포함
@Entity
@Table(name = "users")
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // === 정회원 관련 필드 (불변) ===
    @Column(unique = true)
    private Integer memberNumber;               // 정회원 번호 (영구 소유, NULL = 신규 준회원)

    @Column(nullable = false, length = 50)
    private String realName;                    // 실명 (모든 회원 필수)

    // === 연락처 ===
    @Column(nullable = false, unique = true)
    private String email;

    @Column(length = 20)
    private String phoneNumber;                 // 전화번호 (SMS 인증 필요)

    @Column(length = 500)
    private String profileImageUrl;

    // === 등급 관련 ===
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "grade_id", nullable = false)
    private UserGrade grade;                        // 🆕 등급 엔티티 참조 (DB 테이블)

    @Enumerated(EnumType.STRING)
    @Column
    private AssociateStatus associateStatus;        // 준회원인 경우 상태

    @Column
    private Long directorPartId;                    // 이사인 경우 담당 파트 ID

    @Column(length = 100)
    private String partnerCompanyName;              // 파트너사인 경우 업체명

    // === 연회비 면제 관련 ===
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExemptionType exemptionType = ExemptionType.NONE;

    @Column(length = 200)
    private String exemptionReason;                 // 면제 사유

    @Column
    private Integer exemptionYear;                  // 1회성 면제 적용 년도

    // === 탈퇴 관련 ===
    @Column(nullable = false)
    private boolean isWithdrawn = false;

    @Column
    private LocalDateTime withdrawnAt;

    @Column(length = 500)
    private String withdrawalReason;

    // === 로그인 관련 ===
    @Column
    private LocalDateTime lastLoginAt;

    @Column(nullable = false)
    private boolean isActive = true;

    // === 연관 관계 ===
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<OAuthAccount> oauthAccounts = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<PasskeyCredential> passkeyCredentials = new ArrayList<>();

    // === 활동명 생성 ===
    public String getDisplayName() {
        if (isWithdrawn) {
            return memberNumber != null
                ? memberNumber + " (탈퇴)"
                : "(탈퇴한 회원)";
        }

        StringBuilder sb = new StringBuilder();

        // 정회원 번호가 있으면 표시
        if (memberNumber != null) {
            sb.append(memberNumber).append(" ");
        }
        sb.append(realName);

        // 등급별 접미사
        switch (grade) {
            case ADVISOR -> sb.append(" (고문)");
            case PRESIDENT -> sb.append(" (회장)");
            case VICE_PRESIDENT -> sb.append(" (부회장)");
            case DIRECTOR -> sb.append(" (").append(getDirectorPartName()).append("이사)");
            case ASSOCIATE -> sb.append(" (준회원)");
            case PARTNER -> {
                return partnerCompanyName + " (파트너)";
            }
            // DEVELOPER, REGULAR: 접미사 없음
        }
        return sb.toString();
    }

    // 탈퇴 처리
    public void withdraw(String reason) {
        this.isWithdrawn = true;
        this.withdrawnAt = LocalDateTime.now();
        this.withdrawalReason = reason;
        this.email = "withdrawn_" + this.id + "@deleted.local";
        this.profileImageUrl = null;
        this.oauthAccounts.clear();
        this.passkeyCredentials.clear();
    }
}

// UserGrade.java - 🆕 등급 엔티티 (동적 관리, DB 테이블)
// 임원진이 등급을 추가/제거할 수 있도록 Enum이 아닌 DB 테이블로 관리
@Entity
@Table(name = "user_grades")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserGrade extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 등급 정보
    @Column(nullable = false, unique = true, length = 30)
    private String code;                    // 등급 코드 (예: DEVELOPER, ADVISOR, REGULAR)

    @Column(nullable = false, length = 50)
    private String name;                    // 등급명 (예: 개발자, 고문, 정회원)

    @Column(nullable = false, unique = true, length = 50)
    private String roleName;                // Spring Security Role (예: ROLE_DEVELOPER)

    // 권한 레벨 (높을수록 상위 등급)
    @Column(nullable = false)
    private Integer permissionLevel;        // 권한 레벨 (예: 10, 9, 8, ...)

    // 특성 플래그
    @Column(nullable = false)
    private boolean executive = false;      // 임원 여부 (PRESIDENT, VICE_PRESIDENT, DIRECTOR)

    @Column(nullable = false)
    private boolean staff = false;          // 운영진 여부 (임원 + 고문)

    @Column(nullable = false)
    private boolean member = false;         // 정/준회원 여부

    @Column(nullable = false)
    private boolean requiresAnnualFee = true;  // 연회비 필요 여부

    @Column(nullable = false)
    private boolean systemGrade = false;    // 시스템 등급 (삭제 불가: DEVELOPER, ASSOCIATE)

    // 표시 설정
    @Column(length = 20)
    private String displaySuffix;           // 표시 접미사 (예: "(고문)", "(회장)")

    @Column(nullable = false)
    private Integer displayOrder;           // 표시 순서

    // 관리
    @Column
    private Long createdBy;                 // 생성자 ID (NULL이면 시스템)

    @Column(nullable = false)
    private boolean active = true;

    // 비즈니스 메서드
    public boolean isHigherOrEqualTo(UserGrade other) {
        return this.permissionLevel >= other.getPermissionLevel();
    }

    public boolean canManage(UserGrade other) {
        return this.permissionLevel > other.getPermissionLevel();
    }

    // 시스템 등급 코드 상수 (참조용)
    public static final String CODE_DEVELOPER = "DEVELOPER";
    public static final String CODE_ADVISOR = "ADVISOR";
    public static final String CODE_PRESIDENT = "PRESIDENT";
    public static final String CODE_VICE_PRESIDENT = "VICE_PRESIDENT";
    public static final String CODE_DIRECTOR = "DIRECTOR";
    public static final String CODE_REGULAR = "REGULAR";
    public static final String CODE_ASSOCIATE = "ASSOCIATE";
    public static final String CODE_PARTNER = "PARTNER";

    @Builder
    public UserGrade(String code, String name, String roleName, Integer permissionLevel,
                     boolean executive, boolean staff, boolean member, boolean requiresAnnualFee,
                     boolean systemGrade, String displaySuffix, Integer displayOrder, Long createdBy) {
        this.code = code;
        this.name = name;
        this.roleName = roleName;
        this.permissionLevel = permissionLevel;
        this.executive = executive;
        this.staff = staff;
        this.member = member;
        this.requiresAnnualFee = requiresAnnualFee;
        this.systemGrade = systemGrade;
        this.displaySuffix = displaySuffix;
        this.displayOrder = displayOrder;
        this.createdBy = createdBy;
    }
}

// AssociateStatus.java - 준회원 상태 구분
public enum AssociateStatus {
    PENDING("신규 - OAuth 가입만 완료"),
    REVIEWING("심사중 - 정회원 신청서 제출됨"),
    EXPIRED("만료 - 정회원→연회비 미납 강등"),
    REJECTED("반려 - 정회원 신청 반려됨");

    private final String description;
}

// ExemptionType.java - 연회비 면제 유형
public enum ExemptionType {
    NONE("면제 아님 - 일반 정회원"),
    PERMANENT("영구 면제 - 고문, 명예정회원"),
    ONE_TIME("1회성 면제 - 해당 년도만");

    private final String description;
}

// OAuthProvider.java
public enum OAuthProvider {
    GOOGLE, APPLE, NAVER
}

// OAuthAccount.java - 다중 OAuth 연결
@Entity
@Table(name = "oauth_accounts")
public class OAuthAccount extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OAuthProvider provider;

    @Column(nullable = false)
    private String providerId;                  // OAuth 제공자의 사용자 ID

    @Column
    private String providerEmail;               // OAuth 계정 이메일
}

// PasskeyCredential.java - Passkey 자격증명 (WebAuthn)
@Entity
@Table(name = "passkey_credentials")
public class PasskeyCredential extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, unique = true)
    private String credentialId;                // Base64 인코딩된 자격증명 ID

    @Column(nullable = false, columnDefinition = "TEXT")
    private String publicKey;                   // PEM 형식 공개키

    @Column(nullable = false)
    private Long signCounter;                   // 서명 카운터 (리플레이 공격 방지)

    @Column(length = 100)
    private String deviceName;                  // 기기 이름 (iPhone, MacBook 등)

    @Column(length = 100)
    private String aaguid;                      // 인증기 모델 식별자

    @Column
    private LocalDateTime lastUsedAt;           // 마지막 사용 시간
}

// ═══════════════════════════════════════════════════════════════════════════════
// 🆕 UserGrade Repository & Service (등급 동적 관리)
// ═══════════════════════════════════════════════════════════════════════════════

// UserGradeRepository.java
public interface UserGradeRepository extends JpaRepository<UserGrade, Long> {

    Optional<UserGrade> findByCode(String code);

    Optional<UserGrade> findByRoleName(String roleName);

    List<UserGrade> findByIsActiveOrderByDisplayOrderAsc(boolean isActive);

    List<UserGrade> findAllByOrderByPermissionLevelDesc();

    boolean existsByCode(String code);

    // 특정 권한 레벨 이상의 등급 조회
    List<UserGrade> findByPermissionLevelGreaterThanEqual(Integer level);

    // 삭제 가능한 등급 조회 (시스템 등급 제외)
    @Query("SELECT g FROM UserGrade g WHERE g.isSystemGrade = false AND g.isActive = true")
    List<UserGrade> findDeletableGrades();
}

// UserGradeService.java
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserGradeService {

    private final UserGradeRepository userGradeRepository;
    private final UserRepository userRepository;

    /**
     * 등급 코드로 조회 / Find grade by code
     */
    public UserGrade findByCode(String code) {
        return userGradeRepository.findByCode(code)
            .orElseThrow(() -> new GradeNotFoundException("등급을 찾을 수 없습니다: " + code));
    }

    /**
     * 활성 등급 목록 조회 / Get active grades ordered by display order
     */
    public List<UserGrade> getActiveGrades() {
        return userGradeRepository.findByIsActiveOrderByDisplayOrderAsc(true);
    }

    /**
     * 새 등급 생성 (임원만 가능) / Create new grade (executives only)
     */
    @Transactional
    public UserGrade createGrade(UserGradeCreateRequest request, Long creatorId) {
        // 코드 중복 체크
        if (userGradeRepository.existsByCode(request.code())) {
            throw new DuplicateGradeCodeException(request.code());
        }

        UserGrade grade = UserGrade.builder()
            .code(request.code())
            .name(request.name())
            .roleName("ROLE_" + request.code())
            .permissionLevel(request.permissionLevel())
            .executive(request.isExecutive())
            .staff(request.isStaff())
            .member(request.isMember())
            .requiresAnnualFee(request.requiresAnnualFee())
            .systemGrade(false)  // 동적 생성 등급은 시스템 등급 아님
            .displaySuffix(request.displaySuffix())
            .displayOrder(request.displayOrder())
            .createdBy(creatorId)
            .build();

        return userGradeRepository.save(grade);
    }

    /**
     * 등급 삭제 (임원만 가능, 시스템 등급 제외) / Delete grade (executives only, except system grades)
     */
    @Transactional
    public void deleteGrade(Long gradeId) {
        UserGrade grade = userGradeRepository.findById(gradeId)
            .orElseThrow(() -> new GradeNotFoundException(gradeId));

        // 시스템 등급은 삭제 불가 / System grades cannot be deleted
        if (grade.isSystemGrade()) {
            throw new SystemGradeDeleteException(grade.getCode());
        }

        // 해당 등급 사용 중인 회원 있는지 확인 / Check if any members have this grade
        if (userRepository.existsByGradeId(gradeId)) {
            throw new GradeInUseException(grade.getName());
        }

        // Soft delete
        grade.deactivate();
    }

    /**
     * 기본 등급 조회 (신규 가입 시) / Get default grade for new registrations
     */
    public UserGrade getDefaultGrade() {
        return findByCode(UserGrade.CODE_ASSOCIATE);
    }
}
```

### 2.2 Landing Module Entities / 랜딩 모듈 엔티티

```java
// ClubHistory.java
@Entity
@Table(name = "club_histories")
public class ClubHistory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer year;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column
    private String imageUrl;

    @Column(nullable = false)
    private Integer displayOrder;
}

// Executive.java
@Entity
@Table(name = "executives")
public class Executive extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer termYear;                    // 몇 기 (1기, 2기, ...)

    @Column(nullable = false, length = 50)
    private String position;                     // 회장, 부회장, 총무 등

    @Column(nullable = false, length = 100)
    private String name;

    @Column
    private String profileImageUrl;

    @Column(columnDefinition = "TEXT")
    private String introduction;

    @Column(nullable = false)
    private Integer displayOrder;

    @Column(nullable = false)
    private boolean isCurrent = false;           // 현재 임원 여부
}

// Event.java
@Entity
@Table(name = "events")
public class Event extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private LocalDateTime eventStartAt;

    @Column(nullable = false)
    private LocalDateTime eventEndAt;

    @Column(length = 500)
    private String location;

    @Column
    private String locationMapUrl;               // 지도 URL

    @Column
    private Integer maxParticipants;

    @Column
    private Integer currentParticipants = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventStatus status = EventStatus.UPCOMING;

    @Column
    private String coverImageUrl;
}

// InstagramPost.java
@Entity
@Table(name = "instagram_posts")
public class InstagramPost extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String instagramPostId;              // Instagram 고유 ID

    @Column(columnDefinition = "TEXT")
    private String caption;

    @Column(nullable = false)
    private String mediaUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MediaType mediaType;                 // IMAGE, VIDEO, CAROUSEL

    @Column
    private String permalink;

    @Column(nullable = false)
    private LocalDateTime postedAt;

    @Column
    private Integer likeCount;

    @Column
    private Integer commentCount;
}
```

### 2.3 Membership Module Entities / 정회원 가입 모듈 엔티티 (🆕)

```java
// MembershipApplication.java - 정회원 신청서
@Entity
@Table(name = "membership_applications")
public class MembershipApplication extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false, length = 50)
    private String applicantName;               // 신청자 이름

    @Column(nullable = false, length = 20)
    private String phoneNumber;                 // 연락처

    @Column(nullable = false, length = 20)
    private String carNumber;                   // 차량번호

    @Column(nullable = false, length = 50)
    private String vinNumber;                   // 차대번호

    @Column(length = 50)
    private String carModel;                    // 차종 (M3, M4, M5 등)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VehicleOwnershipType ownershipType; // 차량 소유 유형

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VerificationStatus status = VerificationStatus.PENDING;

    @Column(columnDefinition = "TEXT")
    private String rejectionReason;             // 반려 사유

    @Column
    private Long reviewedBy;                    // 심사한 관리자 ID

    @Column
    private LocalDateTime reviewedAt;

    @Column
    private LocalDateTime approvedAt;

    @OneToMany(mappedBy = "application", cascade = CascadeType.ALL)
    private List<ApplicationDocument> documents = new ArrayList<>();
}

// VehicleOwnershipType.java - 차량 소유 유형
public enum VehicleOwnershipType {
    PERSONAL("개인 - 본인 명의"),
    CORPORATE("법인 - 법인 명의"),
    LEASE("리스 - 개인 리스"),
    RENTAL("렌트 - 개인 렌트"),
    CORPORATE_LEASE("법인리스 - 법인 리스"),
    CORPORATE_RENTAL("법인렌트 - 법인 렌트");

    private final String description;

    // 필수 서류 반환
    public Set<DocumentType> getRequiredDocuments() {
        return switch (this) {
            case PERSONAL -> Set.of(DocumentType.VEHICLE_REGISTRATION, DocumentType.ID_CARD);
            case CORPORATE -> Set.of(DocumentType.VEHICLE_REGISTRATION,
                                     DocumentType.BUSINESS_LICENSE,
                                     DocumentType.EMPLOYMENT_CERTIFICATE);
            case LEASE -> Set.of(DocumentType.VEHICLE_REGISTRATION,
                                 DocumentType.LEASE_CONTRACT);
            case RENTAL -> Set.of(DocumentType.VEHICLE_REGISTRATION,
                                  DocumentType.RENTAL_CONTRACT);
            case CORPORATE_LEASE -> Set.of(DocumentType.VEHICLE_REGISTRATION,
                                           DocumentType.BUSINESS_LICENSE,
                                           DocumentType.EMPLOYMENT_CERTIFICATE,
                                           DocumentType.LEASE_CONTRACT);
            case CORPORATE_RENTAL -> Set.of(DocumentType.VEHICLE_REGISTRATION,
                                            DocumentType.BUSINESS_LICENSE,
                                            DocumentType.EMPLOYMENT_CERTIFICATE,
                                            DocumentType.RENTAL_CONTRACT);
        };
    }
}

// ApplicationDocument.java - 제출 서류
@Entity
@Table(name = "application_documents")
public class ApplicationDocument extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", nullable = false)
    private MembershipApplication application;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DocumentType documentType;

    @Column(nullable = false, length = 500)
    private String fileUrl;                     // S3 URL

    @Column(nullable = false)
    private String originalFileName;

    @Column(nullable = false)
    private Long fileSize;

    @OneToOne(mappedBy = "document", cascade = CascadeType.ALL)
    private OcrResult ocrResult;
}

// DocumentType.java - 서류 유형
public enum DocumentType {
    VEHICLE_REGISTRATION("차량등록증"),
    ID_CARD("신분증"),
    BUSINESS_LICENSE("사업자등록증"),
    EMPLOYMENT_CERTIFICATE("재직증명서"),
    LEASE_CONTRACT("리스계약서"),
    RENTAL_CONTRACT("렌트계약서");

    private final String description;
}

// OcrResult.java - OCR 추출 결과
@Entity
@Table(name = "ocr_results")
public class OcrResult extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false)
    private ApplicationDocument document;

    @Column(columnDefinition = "TEXT")
    private String extractedText;               // 전체 추출 텍스트

    @Column(columnDefinition = "JSON")
    private String extractedFields;             // 필드별 추출 결과 (JSON)

    @Column
    private Double confidence;                  // 신뢰도 (0~1)

    @Column(nullable = false)
    private boolean isVerified = false;         // 대조 검증 완료 여부

    @Column(columnDefinition = "TEXT")
    private String verificationNotes;           // 검증 메모

    @Column
    private LocalDateTime processedAt;
}

// VerificationStatus.java - 검증 상태
public enum VerificationStatus {
    PENDING("대기 - 서류 업로드 필요"),
    DOCUMENTS_UPLOADED("서류 제출 완료 - OCR 처리 중"),
    OCR_COMPLETED("OCR 완료 - 검토 대기"),
    UNDER_REVIEW("검토 중 - 관리자 확인"),
    APPROVED("승인 완료 - 입금 대기"),
    PAYMENT_PENDING("입금 대기 중"),
    COMPLETED("완료 - 정회원 승급"),
    REJECTED("반려됨");

    private final String description;
}

// PaymentRecord.java - 입금 기록
@Entity
@Table(name = "payment_records")
public class PaymentRecord extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentType paymentType;            // ENROLLMENT_FEE, ANNUAL_FEE

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;                  // 금액 (200,000원)

    @Column(nullable = false, length = 50)
    private String depositorName;               // 입금자명

    @Column
    private LocalDate depositDate;              // 입금일

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status = PaymentStatus.PENDING;

    @Column
    private Long confirmedBy;                   // 확인한 관리자 ID

    @Column
    private LocalDateTime confirmedAt;

    @Column(nullable = false)
    private boolean autoConfirmed = false;      // 오픈뱅킹 자동 확인 여부

    @Column(length = 100)
    private String bankTransactionId;           // 은행 거래 ID (오픈뱅킹 연동 시)

    @Column
    private Integer targetYear;                 // 연회비 대상 년도

    @Column(columnDefinition = "TEXT")
    private String notes;                       // 비고
}

// PaymentType.java
public enum PaymentType {
    ENROLLMENT_FEE("입회비", BigDecimal.valueOf(200000)),
    ANNUAL_FEE("연회비", BigDecimal.valueOf(200000));

    private final String description;
    private final BigDecimal defaultAmount;
}

// PaymentStatus.java
public enum PaymentStatus {
    PENDING("입금 대기"),
    CONFIRMED("입금 확인"),
    CANCELLED("취소됨");

    private final String description;
}

// MembershipPeriod.java - 회원권 기간
@Entity
@Table(name = "membership_periods")
public class MembershipPeriod extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Integer year;                       // 회원권 년도

    @Column(nullable = false)
    private LocalDate startDate;                // 시작일

    @Column(nullable = false)
    private LocalDate endDate;                  // 종료일

    @Column(nullable = false)
    private boolean isActive = true;

    @Column
    private Long paymentRecordId;               // 연결된 입금 기록
}

// DirectorPart.java - 이사 파트 (동적 생성 가능)
@Entity
@Table(name = "director_parts")
public class DirectorPart extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;                        // 파트명 (행사, 홍보, 총무 등)

    @Column(length = 200)
    private String description;

    @Column(columnDefinition = "JSON")
    private String permissions;                 // 부여된 권한 목록 (JSON)

    @Column(nullable = false)
    private Long createdById;                   // 생성자 (회장 ID)

    @Column(nullable = false)
    private boolean canManageMembers = false;   // 회원 관리 권한

    @Column(nullable = false)
    private boolean canManagePosts = true;      // 게시글 관리 권한

    @Column(nullable = false)
    private boolean canManageEvents = false;    // 이벤트 관리 권한

    @Column(nullable = false)
    private boolean isActive = true;

    @Column(nullable = false)
    private Integer displayOrder = 0;
}

// MemberVehicle.java - 회원 차량 (다중 차량 등록)
@Entity
@Table(name = "member_vehicles")
public class MemberVehicle extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false, length = 20)
    private String carNumber;                   // 차량번호

    @Column(nullable = false, unique = true, length = 50)
    private String vinNumber;                   // 차대번호 (중복 불가)

    @Column(length = 50)
    private String carModel;                    // 차종 (M3, M4, M5 등)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VehicleOwnershipType ownershipType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VehicleStatus status = VehicleStatus.ACTIVE;

    @Column(nullable = false)
    private LocalDate registeredAt;             // 등록일

    @Column
    private LocalDate soldAt;                   // 매각일

    @Column
    private LocalDate gracePeriodEndAt;         // 유예 종료일

    @Column(nullable = false)
    private boolean isPrimary = false;          // 대표 차량 여부
}

// VehicleStatus.java
public enum VehicleStatus {
    ACTIVE("현재 소유 중"),
    SOLD("매각 완료"),
    GRACE_PERIOD("유예 기간 - M차량 없음");

    private final String description;
}

// AnnualFeeConfig.java - 연회비 기간 설정 (매년 임원진이 설정)
@Entity
@Table(name = "annual_fee_configs")
public class AnnualFeeConfig extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Integer targetYear;                 // 대상 년도 (예: 2025)

    @Column(nullable = false)
    private LocalDate carryOverDeadline;        // 이월 마감일 (예: 2025-01-15)

    @Column(nullable = false)
    private LocalDate renewalStartDate;         // 갱신 시작일 (예: 2025-01-01)

    @Column(nullable = false)
    private LocalDate renewalDeadline;          // 갱신 마감일 (예: 2025-01-31)

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal annualFeeAmount;         // 연회비 금액 (기본 200,000원)

    @Column(nullable = false)
    private Long configuredBy;                  // 설정한 임원 ID

    @Column(nullable = false)
    private LocalDateTime configuredAt;

    @Column(length = 500)
    private String notes;                       // 비고 (예: "설 연휴로 마감일 연장")
}
```

### 2.4 Community Module Entities / 커뮤니티 모듈 엔티티

```java
// Board.java - 게시판 (권한 그룹 기반으로 변경)
// Board entity - Board (changed to group-based permissions)
@Entity
@Table(name = "boards")
public class Board extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String slug;                         // URL용 식별자 / URL identifier

    @Column(nullable = false, length = 100)
    private String name;                         // 게시판 이름 / Board name

    @Column(length = 500)
    private String description;                  // 게시판 설명 / Board description

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BoardType boardType;                 // GENERAL, NOTICE, GALLERY, QNA

    // 🆕 등급 기반 권한 제거 → 권한 그룹 기반으로 변경
    // Removed grade-based permissions → Changed to permission group-based
    // 권한은 BoardPermissionMapping 테이블을 통해 관리
    // Permissions are managed through BoardPermissionMapping table

    @Column(nullable = false)
    private Integer displayOrder;                // 표시 순서 / Display order

    @Column(nullable = false)
    private boolean isActive = true;             // 활성화 여부 / Active status

    @Column(nullable = false)
    private boolean allowComments = true;        // 댓글 허용 / Allow comments

    @Column(nullable = false)
    private boolean allowAttachments = true;     // 첨부파일 허용 / Allow attachments

    @Column(nullable = false)
    private Long createdById;                    // 생성자 ID / Creator ID
}

// Post.java
@Entity
@Table(name = "posts")
public class Post extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id", nullable = false)
    private Board board;

    @Column(nullable = false)
    private Long authorId;                       // User ID (느슨한 결합)

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(nullable = false)
    private Integer viewCount = 0;

    @Column(nullable = false)
    private Integer likeCount = 0;

    @Column(nullable = false)
    private Integer commentCount = 0;

    @Column(nullable = false)
    private boolean isPinned = false;

    @Column(nullable = false)
    private boolean isNotice = false;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Attachment> attachments = new ArrayList<>();
}

// Comment.java
@Entity
@Table(name = "comments")
public class Comment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Comment parent;                      // 대댓글용

    @Column(nullable = false)
    private Long authorId;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(nullable = false)
    private Integer likeCount = 0;

    @Column(nullable = false)
    private boolean isDeleted = false;

    @OneToMany(mappedBy = "parent")
    private List<Comment> replies = new ArrayList<>();
}

// Attachment.java
@Entity
@Table(name = "attachments")
public class Attachment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @Column(nullable = false)
    private String originalFileName;

    @Column(nullable = false)
    private String storedFileName;               // S3 Key

    @Column(nullable = false)
    private String fileUrl;

    @Column(nullable = false)
    private Long fileSize;

    @Column(nullable = false, length = 100)
    private String contentType;

    @Column(nullable = false)
    private Integer displayOrder;
}

// PostLike.java
@Entity
@Table(name = "post_likes", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"post_id", "user_id"})
})
public class PostLike extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "post_id", nullable = false)
    private Long postId;

    @Column(name = "user_id", nullable = false)
    private Long userId;
}

// Bookmark.java - 북마크
// Bookmark entity
@Entity
@Table(name = "bookmarks", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"post_id", "user_id"})
})
public class Bookmark extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "post_id", nullable = false)
    private Long postId;

    @Column(name = "user_id", nullable = false)
    private Long userId;
}

// ═══════════════════════════════════════════════════════════════════════════════
// 🆕 권한 그룹 시스템 엔티티 / Permission Group System Entities
// ═══════════════════════════════════════════════════════════════════════════════

// BoardPermission.java - 게시판 권한 Enum
// BoardPermission enum - Board permission types
public enum BoardPermission {
    READ("글읽기", "Read posts"),                     // 글읽기 (댓글 읽기 포함)
    WRITE("글쓰기", "Write posts"),                   // 글쓰기
    MOVE("게시글 이동", "Move posts"),                // 게시글 이동 (권한 있는 게시판 간)
    COMMENT("댓글쓰기", "Write comments"),            // 댓글쓰기
    DELETE("삭제", "Delete (soft)"),                  // 삭제 (Soft Delete)
    HARD_DELETE("완전삭제", "Hard delete"),           // 완전 삭제 (관리자/운영진만)
    SHARE("게시글 공유", "Share posts");              // 게시글 공유 (외부 공유 링크 생성)

    private final String descriptionKo;
    private final String descriptionEn;

    BoardPermission(String descriptionKo, String descriptionEn) {
        this.descriptionKo = descriptionKo;
        this.descriptionEn = descriptionEn;
    }

    public String getDescriptionKo() { return descriptionKo; }
    public String getDescriptionEn() { return descriptionEn; }
}

// PermissionGroup.java - 권한 그룹 (임원진이 동적으로 생성/삭제 가능)
// PermissionGroup entity - Permission group (dynamically manageable by executives)
@Entity
@Table(name = "permission_groups")
public class PermissionGroup extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String name;                             // 그룹명 (예: "정회원 기본", "운영진", "준회원 제한")
                                                     // Group name (e.g., "Regular Default", "Executives", "Associate Limited")

    @Column(length = 200)
    private String description;                      // 그룹 설명 / Group description

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "permission_group_permissions",
        joinColumns = @JoinColumn(name = "group_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "permission")
    private Set<BoardPermission> defaultPermissions = new HashSet<>();  // 기본 권한 목록
                                                                        // Default permissions for this group

    @Column(nullable = false)
    private boolean isSystemGroup = false;           // 시스템 기본 그룹 여부 (삭제 불가)
                                                     // System default group (cannot be deleted)

    @Column(nullable = false)
    private boolean isActive = true;                 // 활성화 여부 / Active status

    @Column(nullable = false)
    private Integer displayOrder = 0;                // 표시 순서 / Display order

    @Column(nullable = false)
    private Long createdById;                        // 생성자 ID (임원 ID) / Creator ID (executive)

    @Column
    private LocalDateTime lastModifiedAt;            // 최종 수정일시 / Last modified datetime

    @Column
    private Long lastModifiedById;                   // 최종 수정자 ID / Last modified by ID
}

// BoardPermissionMapping.java - 게시판-권한그룹 매핑 (게시판별 권한 설정)
// BoardPermissionMapping entity - Board-PermissionGroup mapping (per-board permission settings)
@Entity
@Table(name = "board_permission_mappings", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"board_id", "permission_group_id"})
})
public class BoardPermissionMapping extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "board_id", nullable = false)
    private Long boardId;                            // 게시판 ID / Board ID

    @Column(name = "permission_group_id", nullable = false)
    private Long permissionGroupId;                  // 권한 그룹 ID / Permission group ID

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "board_permission_mapping_permissions",
        joinColumns = @JoinColumn(name = "mapping_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "permission")
    private Set<BoardPermission> permissions = new HashSet<>();  // 이 게시판에서의 권한
                                                                 // Permissions for this board

    @Column(nullable = false)
    private Long assignedById;                       // 권한 부여자 ID / Assigned by ID

    @Column
    private LocalDateTime assignedAt;                // 권한 부여 일시 / Assigned datetime

    // 특정 게시판에서 특정 그룹이 가진 권한을 확인
    // Check if this mapping grants a specific permission
    public boolean hasPermission(BoardPermission permission) {
        return permissions.contains(permission);
    }
}

// UserPermissionGroup.java - 사용자-권한그룹 매핑 (사용자별 추가 권한 부여)
// UserPermissionGroup entity - User-PermissionGroup mapping (additional permissions per user)
@Entity
@Table(name = "user_permission_groups", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "permission_group_id"})
})
public class UserPermissionGroup extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;                             // 사용자 ID / User ID

    @Column(name = "permission_group_id", nullable = false)
    private Long permissionGroupId;                  // 권한 그룹 ID / Permission group ID

    @Column(nullable = false)
    private Long assignedById;                       // 권한 부여자 ID (임원 ID) / Assigned by ID (executive)

    @Column
    private LocalDateTime assignedAt;                // 권한 부여 일시 / Assigned datetime

    @Column(length = 200)
    private String assignReason;                     // 부여 사유 / Assignment reason

    @Column
    private LocalDate expiresAt;                     // 만료일 (임시 권한 부여 시) / Expiration date (for temporary grants)

    @Column(nullable = false)
    private boolean isActive = true;                 // 활성화 여부 / Active status
}

// ═══════════════════════════════════════════════════════════════════════════════
// 권한 체크 서비스 예시 / Permission Check Service Example
// ═══════════════════════════════════════════════════════════════════════════════

/**
 * BoardPermissionChecker - 게시판 권한 체크 서비스
 * BoardPermissionChecker - Board permission checking service
 *
 * @description
 * KR: 사용자가 특정 게시판에서 특정 권한을 가지고 있는지 확인
 * EN: Check if a user has a specific permission on a specific board
 */
@Service
@RequiredArgsConstructor
public class BoardPermissionChecker {

    private final UserRepository userRepository;
    private final UserPermissionGroupRepository userPermissionGroupRepository;
    private final BoardPermissionMappingRepository boardPermissionMappingRepository;
    private final PermissionGroupRepository permissionGroupRepository;

    /**
     * 권한 체크 / Permission check
     *
     * @param userId 사용자 ID / User ID
     * @param boardId 게시판 ID / Board ID
     * @param permission 확인할 권한 / Permission to check
     * @return 권한 보유 여부 / Whether user has permission
     */
    public boolean hasPermission(Long userId, Long boardId, BoardPermission permission) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException(userId));

        // 1. DEVELOPER는 모든 권한 / DEVELOPER has all permissions
        if (UserGrade.CODE_DEVELOPER.equals(user.getGrade().getCode())) {
            return true;
        }

        // 2. 탈퇴 회원은 모든 권한 없음 / Withdrawn members have no permissions
        if (user.isWithdrawn()) {
            return false;
        }

        // 3. 사용자의 권한 그룹 목록 조회 / Get user's permission groups
        List<Long> userGroupIds = getUserPermissionGroupIds(userId, user.getGrade());

        // 4. 해당 게시판에서 사용자 그룹들의 권한 확인 / Check permissions for user's groups on this board
        return boardPermissionMappingRepository
            .findByBoardIdAndPermissionGroupIdIn(boardId, userGroupIds)
            .stream()
            .anyMatch(mapping -> mapping.hasPermission(permission));
    }

    /**
     * 사용자의 모든 권한 그룹 ID 조회 / Get all permission group IDs for user
     *
     * @param userId 사용자 ID / User ID
     * @param grade 사용자 등급 / User grade
     * @return 권한 그룹 ID 목록 / List of permission group IDs
     */
    private List<Long> getUserPermissionGroupIds(Long userId, UserGrade grade) {
        // 등급별 기본 그룹 ID 조회 / Get default group ID for grade
        Long defaultGroupId = getDefaultGroupIdForGrade(grade);

        // 사용자에게 추가로 부여된 그룹 ID 조회 / Get additionally assigned group IDs
        List<Long> additionalGroupIds = userPermissionGroupRepository
            .findActiveByUserId(userId)
            .stream()
            .map(UserPermissionGroup::getPermissionGroupId)
            .toList();

        // 합치기 / Combine
        List<Long> allGroupIds = new ArrayList<>();
        if (defaultGroupId != null) {
            allGroupIds.add(defaultGroupId);
        }
        allGroupIds.addAll(additionalGroupIds);

        return allGroupIds;
    }

    /**
     * 등급별 기본 권한 그룹 ID 반환 / Return default permission group ID for grade
     *
     * @param grade 사용자 등급 / User grade
     * @return 기본 권한 그룹 ID / Default permission group ID
     */
    private Long getDefaultGroupIdForGrade(UserGrade grade) {
        // 🆕 UserGrade는 이제 entity이므로 코드 기반 비교 / UserGrade is now entity, use code-based comparison
        String gradeCode = grade.getCode();
        String groupName;

        if (UserGrade.CODE_DEVELOPER.equals(gradeCode)) {
            return null;  // 별도 처리 / Handled separately
        } else if (grade.isStaff()) {
            // 임원 + 고문 = 운영진 / Executive + Advisor = Staff
            groupName = "운영진";
        } else if (UserGrade.CODE_REGULAR.equals(gradeCode)) {
            groupName = "정회원 기본";
        } else if (UserGrade.CODE_ASSOCIATE.equals(gradeCode)) {
            groupName = "준회원 제한";
        } else if (UserGrade.CODE_PARTNER.equals(gradeCode)) {
            groupName = "파트너 전용";
        } else {
            // 동적으로 추가된 등급은 등급명으로 그룹 매칭 시도 / Dynamically added grades try matching by grade name
            groupName = grade.getName();
        }

        return permissionGroupRepository.findByName(groupName)
            .map(PermissionGroup::getId)
            .orElse(null);
    }
}
```

### 2.5 Admin Module Entities / 어드민 모듈 엔티티

```java
// AdminAction.java
@Entity
@Table(name = "admin_actions")
public class AdminAction extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long adminUserId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActionType actionType;               // USER_GRADE_CHANGE, BOARD_CREATE, POST_DELETE, etc.

    @Column(nullable = false, length = 100)
    private String targetEntity;                 // User, Board, Post, etc.

    @Column(nullable = false)
    private Long targetEntityId;

    @Column(columnDefinition = "TEXT")
    private String actionDetail;                 // JSON 형태의 상세 정보

    @Column(length = 50)
    private String ipAddress;
}

// DashboardMetric.java (통계용)
@Entity
@Table(name = "dashboard_metrics")
public class DashboardMetric extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate metricDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MetricType metricType;               // NEW_USERS, POSTS, COMMENTS, ACTIVE_USERS, etc.

    @Column(nullable = false)
    private Long metricValue;
}
```

### 2.5 Chat Module Entities / 채팅 모듈 엔티티

```java
// ChatRoom.java
@Entity
@Table(name = "chat_rooms")
public class ChatRoom extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100)
    private String name;                         // 그룹 채팅방 이름

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChatRoomType roomType;               // DIRECT, GROUP, EVENT

    @Column
    private Long eventId;                        // 이벤트 연결 시 (EVENT 타입)

    @Column
    private LocalDateTime lastMessageAt;

    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL)
    private List<ChatParticipant> participants = new ArrayList<>();
}

// ChatMessage.java
@Entity
@Table(name = "chat_messages")
public class ChatMessage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private ChatRoom chatRoom;

    @Column(nullable = false)
    private Long senderId;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MessageType messageType;             // TEXT, IMAGE, FILE, SYSTEM

    @Column
    private String attachmentUrl;

    @Column(nullable = false)
    private boolean isDeleted = false;
}

// ChatParticipant.java
@Entity
@Table(name = "chat_participants", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"room_id", "user_id"})
})
public class ChatParticipant extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private ChatRoom chatRoom;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column
    private LocalDateTime lastReadAt;

    @Column(nullable = false)
    private boolean isActive = true;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ParticipantRole role = ParticipantRole.MEMBER;  // OWNER, ADMIN, MEMBER
}
```

### 2.6 NaverCafe Module Entities / 네이버 카페 모듈 엔티티

```java
// CafePost.java
@Entity
@Table(name = "cafe_posts")
public class CafePost extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String cafeArticleId;                // 네이버 카페 글 ID

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(length = 100)
    private String authorNickname;

    @Column(nullable = false)
    private LocalDateTime postedAt;

    @Column
    private Integer viewCount;

    @Column
    private Integer commentCount;

    @Column
    private String originalUrl;

    @Column
    private Long linkedPostId;                   // Community Post와 연결 시

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SyncDirection syncDirection;         // FROM_CAFE, TO_CAFE, BIDIRECTIONAL
}

// CafeSyncLog.java
@Entity
@Table(name = "cafe_sync_logs")
public class CafeSyncLog extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SyncType syncType;                   // FETCH, POST, UPDATE

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SyncStatus status;                   // SUCCESS, FAILED, PARTIAL

    @Column
    private Integer processedCount;

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    @Column
    private LocalDateTime startedAt;

    @Column
    private LocalDateTime completedAt;
}
```

### 2.7 Notification Module Entities / 알림 모듈 엔티티 (🆕)

```java
// NotificationType.java - 알림 유형 Enum
// NotificationType enum - Notification types
public enum NotificationType {
    // 멤버십 관련 / Membership related
    MEMBERSHIP_APPLICATION_RECEIVED("정회원 신청 접수", "Membership application received"),
    MEMBERSHIP_DOCUMENT_APPROVED("서류 승인 완료", "Documents approved"),
    MEMBERSHIP_DOCUMENT_REJECTED("서류 반려", "Documents rejected"),
    MEMBERSHIP_PAYMENT_INSTRUCTION("입금 안내", "Payment instructions"),
    MEMBERSHIP_PAYMENT_CONFIRMED("입금 확인 완료", "Payment confirmed"),
    MEMBERSHIP_UPGRADE_COMPLETE("정회원 승급 완료", "Membership upgrade complete"),
    MEMBERSHIP_EXPIRED("멤버십 만료", "Membership expired"),

    // 연회비 관련 / Annual fee related
    ANNUAL_FEE_RENEWAL_30DAYS("연회비 만료 30일 전", "Annual fee due in 30 days"),
    ANNUAL_FEE_RENEWAL_7DAYS("연회비 만료 7일 전", "Annual fee due in 7 days"),
    ANNUAL_FEE_RENEWAL_COMPLETE("연회비 갱신 완료", "Annual fee renewal complete"),

    // 차량 관련 / Vehicle related
    VEHICLE_GRACE_PERIOD_WARNING("차량 유예 기간 경고", "Vehicle grace period warning"),
    VEHICLE_GRACE_PERIOD_EXPIRED("차량 유예 기간 만료", "Vehicle grace period expired"),

    // 커뮤니티 관련 / Community related
    POST_COMMENT_ADDED("게시글에 댓글", "Comment on your post"),
    POST_LIKED("게시글 좋아요", "Your post was liked"),
    POST_MENTIONED("게시글 멘션", "You were mentioned"),

    // 이벤트(행사) 관련 / Event related
    EVENT_CREATED("새 이벤트 등록", "New event created"),
    EVENT_REMINDER("이벤트 알림", "Event reminder"),
    EVENT_CANCELLED("이벤트 취소", "Event cancelled"),

    // 채팅 관련 / Chat related
    CHAT_MESSAGE_RECEIVED("새 채팅 메시지", "New chat message"),

    // 관리자 알림 / Admin notifications
    ADMIN_NEW_APPLICATION("신규 정회원 신청", "New membership application"),
    ADMIN_PAYMENT_PENDING("입금 확인 대기", "Payment confirmation pending");

    private final String descriptionKo;
    private final String descriptionEn;

    NotificationType(String descriptionKo, String descriptionEn) {
        this.descriptionKo = descriptionKo;
        this.descriptionEn = descriptionEn;
    }

    public String getDescriptionKo() { return descriptionKo; }
    public String getDescriptionEn() { return descriptionEn; }
}

// NotificationChannel.java - 알림 채널 Enum
// NotificationChannel enum - Notification delivery channels
public enum NotificationChannel {
    EMAIL("이메일", "Email", true),           // 기본 채널
    PUSH("앱 푸시", "Push notification", true), // 모바일 앱
    SMS("문자", "SMS", false);                // 비용 발생, 기본 비활성화

    private final String descriptionKo;
    private final String descriptionEn;
    private final boolean enabledByDefault;

    NotificationChannel(String descriptionKo, String descriptionEn, boolean enabledByDefault) {
        this.descriptionKo = descriptionKo;
        this.descriptionEn = descriptionEn;
        this.enabledByDefault = enabledByDefault;
    }

    public String getDescriptionKo() { return descriptionKo; }
    public String getDescriptionEn() { return descriptionEn; }
    public boolean isEnabledByDefault() { return enabledByDefault; }
}

// NotificationPreference.java - 사용자별 알림 설정
// NotificationPreference entity - User notification preferences
@Entity
@Table(name = "notification_preferences")
public class NotificationPreference extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long userId;                             // 사용자 ID / User ID

    @Column(nullable = false)
    private boolean emailEnabled = true;             // 이메일 알림 활성화 / Email notifications enabled

    @Column(nullable = false)
    private boolean pushEnabled = true;              // 푸시 알림 활성화 / Push notifications enabled

    @Column(nullable = false)
    private boolean smsEnabled = false;              // SMS 알림 활성화 (기본 비활성화) / SMS notifications (default disabled)

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "notification_muted_types",
        joinColumns = @JoinColumn(name = "preference_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type")
    private Set<NotificationType> mutedTypes = new HashSet<>();  // 음소거된 알림 유형 / Muted notification types

    @Column(nullable = false)
    private boolean quietHoursEnabled = false;       // 방해금지 시간 활성화 / Quiet hours enabled

    @Column
    private LocalTime quietHoursStart;               // 방해금지 시작 시간 / Quiet hours start

    @Column
    private LocalTime quietHoursEnd;                 // 방해금지 종료 시간 / Quiet hours end

    // 특정 알림 유형이 활성화되어 있는지 확인 / Check if notification type is enabled
    public boolean isTypeEnabled(NotificationType type) {
        return !mutedTypes.contains(type);
    }

    // 특정 채널이 활성화되어 있는지 확인 / Check if channel is enabled
    public boolean isChannelEnabled(NotificationChannel channel) {
        return switch (channel) {
            case EMAIL -> emailEnabled;
            case PUSH -> pushEnabled;
            case SMS -> smsEnabled;
        };
    }
}

// NotificationLog.java - 알림 발송 기록
// NotificationLog entity - Notification delivery log
@Entity
@Table(name = "notification_logs")
public class NotificationLog extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;                             // 수신자 ID / Recipient user ID

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType notificationType;       // 알림 유형 / Notification type

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationChannel channel;             // 발송 채널 / Delivery channel

    @Column(nullable = false, length = 200)
    private String title;                            // 알림 제목 / Notification title

    @Column(columnDefinition = "TEXT")
    private String body;                             // 알림 내용 / Notification body

    @Column(columnDefinition = "JSON")
    private String metadata;                         // 추가 데이터 (JSON) / Additional data (JSON)

    @Column(nullable = false)
    private boolean isSuccess;                       // 발송 성공 여부 / Delivery success

    @Column(columnDefinition = "TEXT")
    private String errorMessage;                     // 실패 시 에러 메시지 / Error message on failure

    @Column
    private LocalDateTime sentAt;                    // 발송 시각 / Sent timestamp

    @Column
    private LocalDateTime readAt;                    // 읽음 시각 / Read timestamp

    @Column(length = 100)
    private String externalId;                       // 외부 서비스 ID (FCM, SES 등) / External service ID
}

// PushToken.java - 푸시 알림 토큰 (모바일 디바이스)
// PushToken entity - Push notification token (mobile devices)
@Entity
@Table(name = "push_tokens", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "device_id"})
})
public class PushToken extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;                             // 사용자 ID / User ID

    @Column(name = "device_id", nullable = false, length = 100)
    private String deviceId;                         // 디바이스 고유 ID / Device unique ID

    @Column(nullable = false, columnDefinition = "TEXT")
    private String token;                            // FCM/APNs 토큰 / FCM/APNs token

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PushPlatform platform;                   // IOS, ANDROID

    @Column(length = 100)
    private String deviceName;                       // 디바이스 이름 / Device name

    @Column(nullable = false)
    private boolean isActive = true;                 // 활성화 여부 / Active status

    @Column
    private LocalDateTime lastUsedAt;                // 마지막 사용 시각 / Last used timestamp
}

// PushPlatform.java - 푸시 플랫폼 Enum
// PushPlatform enum - Push notification platforms
public enum PushPlatform {
    IOS("iOS", "Apple Push Notification Service"),
    ANDROID("Android", "Firebase Cloud Messaging");

    private final String displayName;
    private final String serviceName;

    PushPlatform(String displayName, String serviceName) {
        this.displayName = displayName;
        this.serviceName = serviceName;
    }

    public String getDisplayName() { return displayName; }
    public String getServiceName() { return serviceName; }
}

// ═══════════════════════════════════════════════════════════════════════════════
// 알림 서비스 인터페이스 / Notification Service Interface
// ═══════════════════════════════════════════════════════════════════════════════

/**
 * NotificationService - 알림 발송 서비스 인터페이스
 * NotificationService - Notification delivery service interface
 *
 * @description
 * KR: 다양한 채널을 통한 알림 발송을 추상화한 인터페이스
 * EN: Interface abstracting notification delivery through various channels
 */
public interface NotificationService {

    /**
     * 단일 알림 발송 / Send single notification
     */
    void send(Long userId, NotificationType type, String title, String body, Map<String, Object> data);

    /**
     * 특정 채널로 발송 / Send via specific channel
     */
    void sendViaChannel(Long userId, NotificationChannel channel, String title, String body);

    /**
     * 대량 알림 발송 (정회원 전체 등) / Bulk notification (all regular members, etc.)
     */
    void sendBulk(List<Long> userIds, NotificationType type, String title, String body);

    // === Membership 관련 알림 메서드 ===

    void sendApplicationReceivedNotification(Long userId);
    void sendPaymentInstructionNotification(Long userId);
    void sendMembershipCompleteNotification(Long userId, Integer memberNumber);
    void sendMembershipRejectedNotification(Long userId, String reason);
    void sendMembershipExpiredNotification(Long userId, Integer expiredYear);
    void sendRenewalCompleteNotification(Long userId, Integer targetYear);
    void sendVehicleGraceExpiredNotification(Long userId);
    void sendNewApplicationAlertToAdmins(Long applicationId, String applicantName);

    // === 연회비 갱신 알림 ===
    void sendAnnualFeeRenewalNotification(Long userId, Integer targetYear, LocalDate deadline, BigDecimal amount, Integer daysRemaining);

    // === 커뮤니티 알림 ===
    void sendCommentNotification(Long postId, Long commentAuthorId);
    void sendEventCreatedNotificationToMembers(Long eventId, String eventTitle);
}

/**
 * NotificationChannel 구현체 인터페이스 / Notification channel implementation interface
 */
public interface NotificationChannelSender {

    NotificationChannel getChannel();

    boolean send(String recipient, String title, String body, Map<String, Object> data);
}

// EmailNotificationSender.java
@Service
public class EmailNotificationSender implements NotificationChannelSender {

    @Override
    public NotificationChannel getChannel() {
        return NotificationChannel.EMAIL;
    }

    @Override
    public boolean send(String recipient, String title, String body, Map<String, Object> data) {
        // AWS SES 또는 기타 이메일 서비스 연동
        // AWS SES or other email service integration
        return true;
    }
}

// PushNotificationSender.java
@Service
public class PushNotificationSender implements NotificationChannelSender {

    private final PushTokenRepository pushTokenRepository;

    @Override
    public NotificationChannel getChannel() {
        return NotificationChannel.PUSH;
    }

    @Override
    public boolean send(String recipient, String title, String body, Map<String, Object> data) {
        // Firebase Cloud Messaging (FCM) 연동
        // Firebase Cloud Messaging (FCM) integration
        return true;
    }
}

// SmsNotificationSender.java
@Service
public class SmsNotificationSender implements NotificationChannelSender {

    @Override
    public NotificationChannel getChannel() {
        return NotificationChannel.SMS;
    }

    @Override
    public boolean send(String recipient, String title, String body, Map<String, Object> data) {
        // 알리고, 네이버 클라우드 SMS 등 연동
        // Aligo, Naver Cloud SMS integration
        return true;
    }
}
```

---

## 3. Domain Events / 도메인 이벤트

### 3.1 Event Definitions / 이벤트 정의

```java
// === User Module Events ===

// 사용자 등록 완료 이벤트
public record UserRegisteredEvent(
    Long userId,
    String username,
    String email,
    UserGrade grade,
    OAuthProvider provider,
    LocalDateTime registeredAt
) implements DomainEvent {}

// 사용자 등급 변경 이벤트
// 🆕 UserGrade가 entity이므로 직렬화를 위해 gradeId와 gradeCode 사용
public record UserGradeChangedEvent(
    Long userId,
    String username,
    Long previousGradeId,           // 이전 등급 ID / Previous grade ID
    String previousGradeCode,       // 이전 등급 코드 / Previous grade code
    Long newGradeId,                // 새 등급 ID / New grade ID
    String newGradeCode,            // 새 등급 코드 / New grade code
    Long changedByAdminId,
    LocalDateTime changedAt
) implements DomainEvent {}

// 사용자 프로필 업데이트 이벤트
public record UserProfileUpdatedEvent(
    Long userId,
    String username,
    Map<String, Object> changedFields,
    LocalDateTime updatedAt
) implements DomainEvent {}


// === Landing Module Events ===

// 이벤트(행사) 생성 이벤트
public record EventCreatedEvent(
    Long eventId,
    String title,
    LocalDateTime eventStartAt,
    LocalDateTime eventEndAt,
    String location,
    Integer maxParticipants,
    LocalDateTime createdAt
) implements DomainEvent {}

// 인스타그램 포스트 동기화 이벤트
public record InstagramPostSyncedEvent(
    Long instagramPostId,
    String instagramOriginalId,
    String caption,
    String mediaUrl,
    LocalDateTime syncedAt
) implements DomainEvent {}


// === Community Module Events ===

// 게시글 작성 이벤트
public record PostCreatedEvent(
    Long postId,
    Long boardId,
    String boardSlug,
    Long authorId,
    String title,
    boolean isNotice,
    LocalDateTime createdAt
) implements DomainEvent {}

// 댓글 추가 이벤트
public record CommentAddedEvent(
    Long commentId,
    Long postId,
    Long authorId,
    Long parentCommentId,      // null이면 일반 댓글, 값이 있으면 대댓글
    LocalDateTime createdAt
) implements DomainEvent {}

// 좋아요 이벤트
public record PostLikedEvent(
    Long postId,
    Long userId,
    Integer totalLikeCount,
    LocalDateTime likedAt
) implements DomainEvent {}


// === Chat Module Events ===

// 채팅방 생성 이벤트
public record ChatRoomCreatedEvent(
    Long roomId,
    ChatRoomType roomType,
    Long eventId,              // EVENT 타입일 경우
    List<Long> participantIds,
    LocalDateTime createdAt
) implements DomainEvent {}

// 메시지 전송 이벤트
public record MessageSentEvent(
    Long messageId,
    Long roomId,
    Long senderId,
    MessageType messageType,
    LocalDateTime sentAt
) implements DomainEvent {}


// === NaverCafe Module Events ===

// 카페 글 동기화 이벤트
public record CafePostSyncedEvent(
    Long cafePostId,
    String cafeArticleId,
    String title,
    SyncDirection direction,
    LocalDateTime syncedAt
) implements DomainEvent {}

// 크로스 포스팅 완료 이벤트
public record CrossPostingCompletedEvent(
    Long sourcePostId,
    String sourceType,         // "COMMUNITY" or "CAFE"
    Long targetPostId,
    String targetType,
    LocalDateTime completedAt
) implements DomainEvent {}


// === Membership Module Events (🆕) ===

// 정회원 신청서 제출 이벤트
// Membership application submitted event
public record MembershipAppliedEvent(
    Long applicationId,
    Long userId,
    String applicantName,
    String carNumber,
    VehicleOwnershipType ownershipType,
    LocalDateTime appliedAt
) implements DomainEvent {}

// 서류 검증 완료 이벤트 (승인)
// Document verification approved event
public record MembershipApprovedEvent(
    Long applicationId,
    Long userId,
    Long approvedByAdminId,
    String approverName,
    LocalDateTime approvedAt
) implements DomainEvent {}

// 서류 검증 반려 이벤트
// Document verification rejected event
public record MembershipRejectedEvent(
    Long applicationId,
    Long userId,
    Long rejectedByAdminId,
    String rejectionReason,
    LocalDateTime rejectedAt
) implements DomainEvent {}

// 입금 확인 완료 이벤트
// Payment confirmed event
public record PaymentConfirmedEvent(
    Long paymentRecordId,
    Long userId,
    PaymentType paymentType,
    BigDecimal amount,
    Integer targetYear,                      // 연회비 대상 년도
    boolean autoConfirmed,                   // 오픈뱅킹 자동 확인 여부
    Long confirmedByAdminId,                 // NULL이면 자동 확인
    LocalDateTime confirmedAt
) implements DomainEvent {}

// 멤버십 만료 이벤트 (연회비 미납으로 인한 강등)
// Membership expired event (downgrade due to unpaid annual fee)
public record MembershipExpiredEvent(
    Long userId,
    Integer memberNumber,
    Integer expiredYear,                     // 만료된 연회비 년도
    LocalDateTime expiredAt
) implements DomainEvent {}

// 차량 등록 이벤트
// Vehicle registered event
public record VehicleAddedEvent(
    Long vehicleId,
    Long userId,
    String carNumber,
    String vinNumber,
    String carModel,
    boolean isPrimary,
    LocalDateTime addedAt
) implements DomainEvent {}

// 차량 매각 이벤트
// Vehicle sold event
public record VehicleSoldEvent(
    Long vehicleId,
    Long userId,
    String carNumber,
    String vinNumber,
    LocalDate gracePeriodEndAt,              // 유예 종료일 (M차량 없을 때)
    LocalDateTime soldAt
) implements DomainEvent {}

// 차량 유예 기간 만료 이벤트 (M차량 없어서 준회원 강등)
// Vehicle grace period expired event (downgrade to associate due to no M car)
public record VehicleGracePeriodExpiredEvent(
    Long userId,
    Integer memberNumber,
    LocalDateTime expiredAt
) implements DomainEvent {}

// 연회비 갱신 알림 이벤트
// Annual fee renewal notice event
public record AnnualFeeRenewalNoticeEvent(
    Long userId,
    Integer targetYear,
    LocalDate renewalDeadline,
    BigDecimal amount,
    Integer daysUntilDeadline,               // 마감까지 남은 일수
    LocalDateTime notifiedAt
) implements DomainEvent {}


// === Notification Module Events (🆕) ===

// 알림 발송 이벤트
// Notification sent event
public record NotificationSentEvent(
    Long notificationId,
    Long userId,
    NotificationType type,
    Set<NotificationChannel> channels,       // EMAIL, PUSH, SMS
    boolean isSuccess,
    LocalDateTime sentAt
) implements DomainEvent {}


// === Admin Module Events ===

// 관리자 작업 로그 이벤트
public record AdminActionLoggedEvent(
    Long adminUserId,
    ActionType actionType,
    String targetEntity,
    Long targetEntityId,
    String actionDetail,
    String ipAddress,
    LocalDateTime loggedAt
) implements DomainEvent {}

// 면제 부여 이벤트 (🆕)
// Exemption granted event
public record ExemptionGrantedEvent(
    Long userId,
    Integer memberNumber,
    ExemptionType exemptionType,
    String exemptionReason,
    Integer exemptionYear,                   // 1회성 면제 시 적용 년도
    Long grantedByAdminId,
    LocalDateTime grantedAt
) implements DomainEvent {}

// 권한 그룹 변경 이벤트 (🆕)
// Permission group changed event
public record PermissionGroupChangedEvent(
    Long permissionGroupId,
    String groupName,
    String action,                           // CREATED, UPDATED, DELETED
    Long changedByAdminId,
    LocalDateTime changedAt
) implements DomainEvent {}
```

### 3.2 Event Listeners / 이벤트 리스너

```java
// === Community Module - Event Listeners ===

@Component
@RequiredArgsConstructor
public class CommunityEventListener {

    private final PostService postService;

    // 사용자 등록 시 -> 환영 메시지 또는 초기 데이터 처리
    @ApplicationModuleListener
    public void onUserRegistered(UserRegisteredEvent event) {
        // 필요 시 사용자 관련 초기화 처리
        log.info("New user registered: {} ({})", event.username(), event.userId());
    }

    // 이벤트(행사) 생성 시 -> 공지 게시글 자동 생성 옵션
    @ApplicationModuleListener
    public void onEventCreated(EventCreatedEvent event) {
        // 옵션에 따라 공지 게시판에 이벤트 안내 게시글 자동 생성
        log.info("Event created: {} - may create announcement post", event.title());
    }
}


// === Chat Module - Event Listeners ===

@Component
@RequiredArgsConstructor
public class ChatEventListener {

    private final ChatRoomService chatRoomService;

    // 이벤트(행사) 생성 시 -> 이벤트 채팅방 자동 생성
    @ApplicationModuleListener
    public void onEventCreated(EventCreatedEvent event) {
        chatRoomService.createEventChatRoom(
            event.eventId(),
            event.title() + " 채팅방"
        );
        log.info("Chat room created for event: {}", event.eventId());
    }
}


// === NaverCafe Module - Event Listeners ===

@Component
@RequiredArgsConstructor
public class NaverCafeEventListener {

    private final CafePostingService cafePostingService;

    // 공지 게시글 작성 시 -> 네이버 카페에 크로스 포스팅
    @ApplicationModuleListener
    public void onPostCreated(PostCreatedEvent event) {
        if (event.isNotice()) {
            cafePostingService.crossPostToNaverCafe(event.postId());
            log.info("Cross-posting to Naver Cafe: {}", event.postId());
        }
    }
}


// === Admin Module - Event Listeners (Audit Log) ===

@Component
@RequiredArgsConstructor
public class AdminAuditEventListener {

    private final AdminActionRepository adminActionRepository;

    // 모든 주요 이벤트를 감사 로그로 기록
    @ApplicationModuleListener
    public void onUserGradeChanged(UserGradeChangedEvent event) {
        logAdminAction(
            event.changedByAdminId(),
            ActionType.USER_GRADE_CHANGE,
            "User",
            event.userId(),
            Map.of(
                "previousGrade", event.previousGrade(),
                "newGrade", event.newGrade()
            )
        );
    }

    @ApplicationModuleListener
    public void onPostCreated(PostCreatedEvent event) {
        // 통계 메트릭 업데이트
        updateDashboardMetric(MetricType.POSTS, 1L);
    }

    @ApplicationModuleListener
    public void onUserRegistered(UserRegisteredEvent event) {
        // 통계 메트릭 업데이트
        updateDashboardMetric(MetricType.NEW_USERS, 1L);
    }

    // 🆕 멤버십 관련 감사 로그
    @ApplicationModuleListener
    public void onMembershipApproved(MembershipApprovedEvent event) {
        logAdminAction(
            event.approvedByAdminId(),
            ActionType.MEMBERSHIP_APPROVE,
            "MembershipApplication",
            event.applicationId(),
            Map.of("userId", event.userId(), "approverName", event.approverName())
        );
    }

    @ApplicationModuleListener
    public void onPaymentConfirmed(PaymentConfirmedEvent event) {
        if (!event.autoConfirmed()) {
            logAdminAction(
                event.confirmedByAdminId(),
                ActionType.PAYMENT_CONFIRM,
                "PaymentRecord",
                event.paymentRecordId(),
                Map.of(
                    "userId", event.userId(),
                    "paymentType", event.paymentType(),
                    "amount", event.amount(),
                    "targetYear", event.targetYear()
                )
            );
        }
        // 통계 메트릭 업데이트
        updateDashboardMetric(MetricType.PAYMENTS, 1L);
    }

    @ApplicationModuleListener
    public void onExemptionGranted(ExemptionGrantedEvent event) {
        logAdminAction(
            event.grantedByAdminId(),
            ActionType.EXEMPTION_GRANT,
            "User",
            event.userId(),
            Map.of(
                "memberNumber", event.memberNumber(),
                "exemptionType", event.exemptionType(),
                "exemptionReason", event.exemptionReason(),
                "exemptionYear", event.exemptionYear()
            )
        );
    }
}


// === Membership Module - Event Listeners (🆕) ===

/**
 * MembershipEventListener - 멤버십 이벤트 리스너
 * MembershipEventListener - Membership event listener
 *
 * @description
 * KR: 멤버십 관련 이벤트를 수신하여 후속 처리 (등급 변경, 알림 발송 등)
 * EN: Handle membership-related events for follow-up processing (grade change, notifications, etc.)
 */
@Component
@RequiredArgsConstructor
public class MembershipEventListener {

    private final UserService userService;
    private final NotificationService notificationService;
    private final MemberNumberService memberNumberService;

    // 입금 확인 완료 시 -> 정회원 승급
    // Payment confirmed -> Upgrade to regular member
    @ApplicationModuleListener
    public void onPaymentConfirmed(PaymentConfirmedEvent event) {
        // 입회비인 경우 (신규 가입)
        if (event.paymentType() == PaymentType.ENROLLMENT_FEE) {
            // 정회원 번호 발급 및 등급 승급
            Integer memberNumber = memberNumberService.issueNextMemberNumber();
            userService.upgradeToRegularMember(event.userId(), memberNumber);

            // 정회원 승급 완료 알림
            notificationService.sendMembershipCompleteNotification(event.userId(), memberNumber);
            log.info("User {} upgraded to regular member with number {}", event.userId(), memberNumber);
        }

        // 연회비인 경우 (갱신)
        if (event.paymentType() == PaymentType.ANNUAL_FEE) {
            userService.renewMembership(event.userId(), event.targetYear());
            notificationService.sendRenewalCompleteNotification(event.userId(), event.targetYear());
            log.info("User {} membership renewed for year {}", event.userId(), event.targetYear());
        }
    }

    // 멤버십 만료 시 -> 준회원 강등
    // Membership expired -> Downgrade to associate
    @ApplicationModuleListener
    public void onMembershipExpired(MembershipExpiredEvent event) {
        userService.downgradeToAssociate(event.userId(), AssociateStatus.EXPIRED);
        notificationService.sendMembershipExpiredNotification(event.userId(), event.expiredYear());
        log.info("User {} (#{}) membership expired for year {}",
            event.userId(), event.memberNumber(), event.expiredYear());
    }

    // 차량 유예 기간 만료 시 -> 준회원 강등
    // Vehicle grace period expired -> Downgrade to associate
    @ApplicationModuleListener
    public void onVehicleGracePeriodExpired(VehicleGracePeriodExpiredEvent event) {
        userService.downgradeToAssociate(event.userId(), AssociateStatus.EXPIRED);
        notificationService.sendVehicleGraceExpiredNotification(event.userId());
        log.info("User {} (#{}) downgraded due to vehicle grace period expiration",
            event.userId(), event.memberNumber());
    }

    // 서류 승인 시 -> 입금 안내 알림
    // Document approved -> Send payment instructions
    @ApplicationModuleListener
    public void onMembershipApproved(MembershipApprovedEvent event) {
        userService.updateAssociateStatus(event.userId(), AssociateStatus.REVIEWING);
        notificationService.sendPaymentInstructionNotification(event.userId());
        log.info("Membership application {} approved, payment instruction sent", event.applicationId());
    }

    // 서류 반려 시 -> 반려 알림
    // Document rejected -> Send rejection notification
    @ApplicationModuleListener
    public void onMembershipRejected(MembershipRejectedEvent event) {
        userService.updateAssociateStatus(event.userId(), AssociateStatus.REJECTED);
        notificationService.sendMembershipRejectedNotification(event.userId(), event.rejectionReason());
        log.info("Membership application {} rejected: {}", event.applicationId(), event.rejectionReason());
    }
}


// === Notification Module - Event Listeners (🆕) ===

/**
 * NotificationEventListener - 알림 이벤트 리스너
 * NotificationEventListener - Notification event listener
 *
 * @description
 * KR: 각종 이벤트를 수신하여 알림 발송 처리
 * EN: Handle various events to trigger notification delivery
 */
@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private final NotificationService notificationService;

    // 정회원 신청서 제출 시 -> 신청 접수 알림
    @ApplicationModuleListener
    public void onMembershipApplied(MembershipAppliedEvent event) {
        notificationService.sendApplicationReceivedNotification(event.userId());
        // 관리자에게도 알림 (신규 신청 알림)
        notificationService.sendNewApplicationAlertToAdmins(event.applicationId(), event.applicantName());
        log.info("Membership application {} received notification sent", event.applicationId());
    }

    // 연회비 갱신 알림 (스케줄러에서 발행)
    @ApplicationModuleListener
    public void onAnnualFeeRenewalNotice(AnnualFeeRenewalNoticeEvent event) {
        notificationService.sendAnnualFeeRenewalNotification(
            event.userId(),
            event.targetYear(),
            event.renewalDeadline(),
            event.amount(),
            event.daysUntilDeadline()
        );
        log.info("Annual fee renewal notice sent to user {} for year {} ({} days remaining)",
            event.userId(), event.targetYear(), event.daysUntilDeadline());
    }

    // 게시글에 댓글 달림 -> 작성자에게 알림
    @ApplicationModuleListener
    public void onCommentAdded(CommentAddedEvent event) {
        notificationService.sendCommentNotification(event.postId(), event.authorId());
    }

    // 이벤트(행사) 생성 시 -> 회원들에게 알림
    @ApplicationModuleListener
    public void onEventCreated(EventCreatedEvent event) {
        notificationService.sendEventCreatedNotificationToMembers(event.eventId(), event.title());
        log.info("Event created notification sent for event: {}", event.title());
    }
}
```

---

## 4. gRPC Chat Service / gRPC 채팅 서비스

### 4.1 Proto Definition / Proto 정의

```protobuf
// src/main/proto/chat.proto
syntax = "proto3";

package kr.mclub.apiserver.chat;

option java_multiple_files = true;
option java_package = "kr.mclub.apiserver.chat.grpc";

service ChatService {
  // Bidirectional streaming for real-time chat
  rpc Connect(stream ChatRequest) returns (stream ChatResponse);

  // Server streaming for subscribing to a room
  rpc SubscribeRoom(SubscribeRequest) returns (stream ChatMessage);

  // Unary call for sending a message
  rpc SendMessage(SendMessageRequest) returns (SendMessageResponse);
}

message ChatRequest {
  oneof request {
    JoinRoomRequest join_room = 1;
    SendMessageRequest send_message = 2;
    LeaveRoomRequest leave_room = 3;
    TypingIndicator typing = 4;
  }
}

message ChatResponse {
  oneof response {
    ChatMessage message = 1;
    UserJoinedNotification user_joined = 2;
    UserLeftNotification user_left = 3;
    TypingNotification typing = 4;
    ErrorResponse error = 5;
  }
}

message ChatMessage {
  int64 message_id = 1;
  int64 room_id = 2;
  int64 sender_id = 3;
  string sender_username = 4;
  string content = 5;
  MessageType message_type = 6;
  string attachment_url = 7;
  int64 timestamp = 8;
}

enum MessageType {
  TEXT = 0;
  IMAGE = 1;
  FILE = 2;
  SYSTEM = 3;
}

message JoinRoomRequest {
  int64 room_id = 1;
}

message SendMessageRequest {
  int64 room_id = 1;
  string content = 2;
  MessageType message_type = 3;
  string attachment_url = 4;
}

message LeaveRoomRequest {
  int64 room_id = 1;
}

message SubscribeRequest {
  int64 room_id = 1;
  int64 last_message_id = 2;  // For fetching missed messages
}

message TypingIndicator {
  int64 room_id = 1;
  bool is_typing = 2;
}

message TypingNotification {
  int64 room_id = 1;
  int64 user_id = 2;
  string username = 3;
  bool is_typing = 4;
}

message UserJoinedNotification {
  int64 room_id = 1;
  int64 user_id = 2;
  string username = 3;
}

message UserLeftNotification {
  int64 room_id = 1;
  int64 user_id = 2;
  string username = 3;
}

message SendMessageResponse {
  int64 message_id = 1;
  int64 timestamp = 2;
}

message ErrorResponse {
  string code = 1;
  string message = 2;
}
```

---

## 5. Security Configuration / 보안 설정

### 5.1 Security Config / 보안 설정 클래스

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final OAuth2UserService oauth2UserService;
    private final JwtTokenProvider jwtTokenProvider;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Public APIs
                .requestMatchers("/api/v1/auth/**").permitAll()
                .requestMatchers("/api/v1/landing/**").permitAll()
                .requestMatchers("/api/v1/histories/**").permitAll()
                .requestMatchers("/api/v1/executives/**").permitAll()
                .requestMatchers("/api/v1/events").permitAll()
                .requestMatchers("/api/v1/instagram/**").permitAll()
                .requestMatchers("/api/v1/webhooks/**").permitAll()

                // Admin APIs
                .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")

                // Authenticated APIs
                .anyRequest().authenticated()
            )
            .oauth2Login(oauth2 -> oauth2
                .userInfoEndpoint(userInfo ->
                    userInfo.userService(oauth2UserService))
                .successHandler(oauth2SuccessHandler())
            )
            .addFilterBefore(
                new JwtAuthenticationFilter(jwtTokenProvider),
                UsernamePasswordAuthenticationFilter.class
            )
            .build();
    }
}
```

### 5.2 JWT Token Provider / JWT 토큰 제공자

```java
@Component
public class JwtTokenProvider {

    // Access Token Claims
    public String createAccessToken(User user) {
        Claims claims = Jwts.claims()
            .subject(user.getId().toString())
            .add("username", user.getUsername())
            .add("email", user.getEmail())
            .add("grade", user.getGrade().name())
            .add("roles", List.of(user.getGrade().getSecurityRole()))
            .build();

        return Jwts.builder()
            .claims(claims)
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + accessTokenValidity))
            .signWith(secretKey)
            .compact();
    }

    // Refresh Token (더 긴 유효기간, 최소 정보만)
    public String createRefreshToken(Long userId) {
        return Jwts.builder()
            .subject(userId.toString())
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + refreshTokenValidity))
            .signWith(secretKey)
            .compact();
    }
}
```

### 5.3 Board Access Checker / 게시판 접근 권한 체커

```java
@Component("boardAccessChecker")
@RequiredArgsConstructor
public class BoardAccessChecker {

    private final BoardRepository boardRepository;
    private final UserGradeRepository userGradeRepository;  // 🆕

    public boolean canRead(Long boardId, Authentication auth) {
        Board board = boardRepository.findById(boardId).orElseThrow();
        UserGrade userGrade = getUserGrade(auth);
        UserGrade requiredGrade = userGradeRepository.findById(board.getRequiredGradeToReadId()).orElseThrow();
        // 🆕 permissionLevel 비교 (높을수록 상위 등급)
        return userGrade.getPermissionLevel() >= requiredGrade.getPermissionLevel();
    }

    public boolean canWrite(Long boardId, Authentication auth) {
        Board board = boardRepository.findById(boardId).orElseThrow();
        UserGrade userGrade = getUserGrade(auth);
        UserGrade requiredGrade = userGradeRepository.findById(board.getRequiredGradeToWriteId()).orElseThrow();
        // 🆕 permissionLevel 비교 (높을수록 상위 등급)
        return userGrade.getPermissionLevel() >= requiredGrade.getPermissionLevel();
    }
}
```

---

## 6. File Upload Service / 파일 업로드 서비스

```java
@Service
@RequiredArgsConstructor
public class FileUploadService {

    private final S3Client s3Client;

    @Value("${digitalocean.spaces.bucket}")
    private String bucketName;

    @Value("${digitalocean.spaces.cdn-endpoint}")
    private String cdnEndpoint;

    public FileUploadResponse uploadFile(MultipartFile file, String directory) {
        String originalFileName = file.getOriginalFilename();
        String storedFileName = generateStoredFileName(originalFileName);
        String key = directory + "/" + storedFileName;

        PutObjectRequest putRequest = PutObjectRequest.builder()
            .bucket(bucketName)
            .key(key)
            .contentType(file.getContentType())
            .acl(ObjectCannedACL.PUBLIC_READ)
            .build();

        s3Client.putObject(putRequest,
            RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

        String fileUrl = cdnEndpoint + "/" + key;

        return new FileUploadResponse(
            storedFileName,
            originalFileName,
            fileUrl,
            file.getSize(),
            file.getContentType()
        );
    }

    public String generatePresignedUploadUrl(String fileName, String contentType) {
        // Presigned URL 생성 (클라이언트 직접 업로드용)
        // ...
    }
}
```

---

## 7. Configuration Properties / 환경 설정

### 7.1 application.yml

```yaml
spring:
  application:
    name: mck-api-server

  datasource:
    url: jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5432}/${DB_NAME:mck_db}
    username: ${DB_USERNAME:mck_user}
    password: ${DB_PASSWORD:secret}

  jpa:
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: true

  flyway:
    enabled: true
    locations: classpath:db/migration
    baseline-on-migrate: true

  mail:
    host: ${MAIL_HOST:smtp.gmail.com}
    port: ${MAIL_PORT:587}
    username: ${MAIL_USERNAME}
    password: ${MAIL_PASSWORD}
    properties:
      mail.smtp.auth: true
      mail.smtp.starttls.enable: true

  security:
    oauth2:
      client:
        registration:
          google:
            client-id: ${GOOGLE_CLIENT_ID}
            client-secret: ${GOOGLE_CLIENT_SECRET}
            scope: email, profile
            redirect-uri: "{baseUrl}/api/v1/auth/oauth2/google/callback"

          apple:
            client-id: ${APPLE_CLIENT_ID}
            client-secret: ${APPLE_CLIENT_SECRET}
            scope: email, name
            authorization-grant-type: authorization_code
            redirect-uri: "{baseUrl}/api/v1/auth/oauth2/apple/callback"

          naver:
            client-id: ${NAVER_CLIENT_ID}
            client-secret: ${NAVER_CLIENT_SECRET}
            scope: name, email, profile_image
            redirect-uri: "{baseUrl}/api/v1/auth/oauth2/naver/callback"
            authorization-grant-type: authorization_code
            client-name: Naver

        provider:
          naver:
            authorization-uri: https://nid.naver.com/oauth2.0/authorize
            token-uri: https://nid.naver.com/oauth2.0/token
            user-info-uri: https://openapi.naver.com/v1/nid/me
            user-name-attribute: response

# JWT Configuration
jwt:
  secret: ${JWT_SECRET}
  access-token-validity: 3600000      # 1시간
  refresh-token-validity: 604800000   # 7일

# DigitalOcean Spaces Configuration
digitalocean:
  spaces:
    region: ${DO_SPACES_REGION:sgp1}
    endpoint: ${DO_SPACES_ENDPOINT}
    bucket: ${DO_SPACES_BUCKET}
    cdn-endpoint: ${DO_SPACES_CDN_ENDPOINT}
    access-key: ${DO_SPACES_ACCESS_KEY}
    secret-key: ${DO_SPACES_SECRET_KEY}

# Instagram API Configuration
instagram:
  access-token: ${INSTAGRAM_ACCESS_TOKEN}
  webhook-verify-token: ${INSTAGRAM_WEBHOOK_VERIFY_TOKEN}

# Naver Cafe API Configuration
navercafe:
  client-id: ${NAVER_CAFE_CLIENT_ID}
  client-secret: ${NAVER_CAFE_CLIENT_SECRET}
  cafe-id: ${NAVER_CAFE_ID}
  webhook-verify-token: ${NAVER_CAFE_WEBHOOK_VERIFY_TOKEN}

# gRPC Configuration
grpc:
  server:
    port: ${GRPC_PORT:9090}
```

---

## 8. Spring Modulith Test / 모듈 테스트

```java
@SpringBootTest
class ModularityTests {

    @Autowired
    private ApplicationModules modules;

    @Test
    void verifyModularStructure() {
        // 모듈 구조 검증
        modules.verify();
    }

    @Test
    void createModuleDocumentation() throws IOException {
        // 모듈 문서 생성
        new Documenter(modules)
            .writeDocumentation()
            .writeModulesAsPlantUml();
    }
}

// 개별 모듈 테스트
@ApplicationModuleTest
class UserModuleTests {

    @Test
    void userModuleBootstraps() {
        // User 모듈 독립 테스트
    }
}

@ApplicationModuleTest
class CommunityModuleTests {

    @Test
    void communityModuleBootstraps() {
        // Community 모듈 독립 테스트
    }
}
```

---

## Document History / 문서 이력

| Version | Date | Author | Description |
|---------|------|--------|-------------|
| 1.0 | 2025-12-30 | Claude | Initial detailed design |