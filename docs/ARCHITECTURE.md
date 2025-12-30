# BMW M Club Korea API Server - Architecture Design
# BMW M Club Korea API 서버 - 아키텍처 설계서

## 1. Project Overview / 프로젝트 개요

BMW M Club Korea 커뮤니티 홈페이지의 백엔드 API 서버입니다.
Spring Modulith를 활용한 모듈러 모놀리스 아키텍처를 채택하여
도메인별 독립성을 유지하면서도 단일 배포 단위로 운영합니다.

### 1.1 Tech Stack / 기술 스택

| Category | Technology | Version |
|----------|------------|---------|
| Language | Java | 21 (LTS) |
| Framework | Spring Boot | 4.0.1 |
| Module System | Spring Modulith | 2.0.1 |
| Database | PostgreSQL | 16+ |
| Migration | Flyway | - |
| Auth | OAuth2 + Passkey + JWT | - |
| Real-time | gRPC Streaming | - |
| File Storage | DigitalOcean Spaces | S3 Compatible |
| Scheduling | Quartz | - |

### 1.2 Design Principles / 설계 원칙

1. **Modular Monolith**: 마이크로서비스의 장점 + 모놀리스의 단순함
2. **Event-Driven**: 모듈 간 직접 의존성 제거, 이벤트 기반 통신
3. **Domain-Driven Design**: 도메인 중심 모듈 분리
4. **Clean Architecture**: 각 모듈 내 계층 분리

---

## 2. Module Architecture / 모듈 아키텍처

### 2.1 Module Overview / 모듈 개요

```
kr.mclub.apiserver/
├── MckApiServerApplication.java     # Main Entry Point
│
├── shared/                          # Shared Kernel / 공유 커널
│   ├── domain/                      # 공통 엔티티 (BaseEntity, DomainEvent)
│   ├── exception/                   # 전역 예외 처리
│   ├── security/                    # 보안 설정 (SecurityConfig, JWT)
│   └── util/                        # 유틸리티 (ApiResponse, PageResponse)
│
├── user/                            # User Module / 사용자 모듈
│   ├── domain/                      # User, UserGrade, AssociateStatus, OAuthProvider
│   ├── repository/                  # UserRepository, PasskeyCredentialRepository
│   ├── service/                     # UserService, OAuth2UserService, PasskeyService
│   ├── api/                         # UserController, ProfileController
│   └── event/                       # UserRegisteredEvent, UserGradeChangedEvent
│
├── membership/                      # 🆕 Membership Module / 정회원 가입 모듈
│   ├── domain/                      # MembershipApplication, MemberVehicle, PaymentRecord, DirectorPart
│   ├── repository/                  # 각 도메인 Repository
│   ├── service/                     # MembershipService, OcrService, PaymentService, VehicleService
│   ├── api/                         # MembershipController, VehicleController, PaymentController
│   ├── event/                       # MembershipApprovedEvent, MembershipExpiredEvent, PaymentConfirmedEvent
│   └── scheduler/                   # MembershipExpirationScheduler, VehicleGracePeriodScheduler
│
├── landing/                         # Landing Module / 랜딩 모듈
│   ├── domain/                      # ClubHistory, Executive, Event, InstagramPost
│   ├── repository/                  # 각 도메인 Repository
│   ├── service/                     # LandingService, EventService, InstagramSyncService
│   ├── api/                         # LandingController, EventController
│   ├── webhook/                     # InstagramWebhookController
│   └── event/                       # EventCreatedEvent
│
├── community/                       # Community Module / 커뮤니티 모듈
│   ├── domain/                      # Board, Post, Comment, Attachment, PermissionGroup
│   ├── repository/                  # 각 도메인 Repository
│   ├── service/                     # BoardService, PostService, FileUploadService, PermissionService
│   ├── api/                         # BoardController, PostController, CommentController
│   └── event/                       # PostCreatedEvent, CommentAddedEvent
│
├── admin/                           # Admin Module / 어드민 모듈
│   ├── domain/                      # AdminAction, DashboardMetric, AuditLog
│   ├── repository/                  # AdminActionRepository, AuditLogRepository
│   ├── service/                     # MemberManagementService, DashboardService, DirectorPartService
│   └── api/                         # AdminController, DirectorPartController
│
├── chat/                            # Chat Module / 채팅 모듈
│   ├── domain/                      # ChatRoom, ChatMessage, ChatParticipant
│   ├── repository/                  # 각 도메인 Repository
│   ├── service/                     # ChatRoomService, ChatMessageService
│   ├── grpc/                        # ChatGrpcService
│   ├── api/                         # ChatRestController
│   └── event/                       # ChatRoomCreatedEvent
│
├── navercafe/                       # NaverCafe Module / 네이버 카페 모듈
│   ├── domain/                      # CafePost, CafeSyncLog
│   ├── repository/                  # CafePostRepository
│   ├── service/                     # CafeFetchService, CafePostingService
│   ├── webhook/                     # NaverCafeWebhookController
│   └── event/                       # CafePostSyncedEvent
│
└── notification/                    # 🆕 Notification Module / 알림 모듈
    ├── domain/                      # NotificationPreference, NotificationLog
    ├── repository/                  # NotificationRepository
    ├── service/                     # NotificationService, EmailChannel, PushChannel, SmsChannel
    └── event/                       # NotificationEventListener
```

### 2.2 Module Responsibilities / 모듈 책임

| Module | Responsibility | Key Features |
|--------|---------------|--------------|
| **shared** | 공통 기능 | BaseEntity, Security, Exception Handling |
| **user** | 사용자 관리 | OAuth2/Passkey 로그인, 프로필, 등급 (8단계) |
| **membership** | 🆕 정회원 가입 | 신청서, OCR 서류검증, 결제, 연회비 갱신, 다중 차량 |
| **landing** | 외부 홍보 | History, 임원진, 이벤트, 인스타그램 |
| **community** | 커뮤니티 | 동적 게시판, 게시글, 댓글, 그룹 기반 권한 |
| **admin** | 관리자 기능 | 회원/게시판 관리, 이사 파트 관리, 대시보드 |
| **chat** | 실시간 채팅 | gRPC Streaming, 1:1/그룹 채팅 |
| **navercafe** | 카페 연동 | Webhook, 크로스 포스팅 |
| **notification** | 🆕 알림 | 이메일, 앱 푸시 (FCM/APNs), SMS 알림 |

---

## 3. Module Dependencies / 모듈 의존성

### 3.1 Dependency Diagram / 의존성 다이어그램

```
                          ┌─────────────────────────────────────────────────────┐
                          │                    SHARED KERNEL                     │
                          │  (BaseEntity, Security, Exception, Utils)            │
                          └─────────────────────────────────────────────────────┘
                                                     ▲
       ┌───────────────────────────┬─────────────────┼─────────────────┬───────────────────────────┐
       │                           │                 │                 │                           │
┌──────┴──────┐          ┌─────────┴─────────┐  ┌────┴────┐  ┌─────────┴─────────┐          ┌──────┴──────┐
│             │          │                   │  │         │  │                   │          │             │
│    USER     │◄─Event───│    MEMBERSHIP     │  │ LANDING │  │      CHAT         │◄─Event───│   ADMIN     │
│   MODULE    │          │     MODULE        │  │ MODULE  │  │     MODULE        │          │   MODULE    │
│             │          │                   │  │         │  │                   │          │             │
│ - OAuth2    │          │ - 정회원 신청     │  │- History│  │ - gRPC Streaming  │          │ - 회원관리  │
│ - Passkey   │          │ - OCR 서류검증    │  │- 임원진 │  │ - 1:1/그룹 채팅   │          │ - 이사 파트 │
│ - 프로필    │          │ - 결제/입금확인   │  │- Events │  │ - 메시지 저장     │          │ - Dashboard │
│ - 8단계등급 │          │ - 연회비 갱신     │  │- 인스타 │  │                   │          │ - 감사로그  │
│             │          │ - 다중 차량 관리  │  │         │  │                   │          │             │
└──────┬──────┘          └─────────┬─────────┘  └────┬────┘  └─────────┬─────────┘          └──────┬──────┘
       │                           │                 │                 │                           │
       │         Event             │ Event           │ Event           │ Event                     │
       │     ┌─────────────────────┼─────────────────┼─────────────────┼───────────────────────────┤
       ▼     ▼                     ▼                 ▼                 ▼                           ▼
┌─────────────────────────────────────────────────────────────────────────────────────────────────────────┐
│                                                                                                         │
│                                         COMMUNITY MODULE                                                │
│                                                                                                         │
│    - Dynamic Boards    - Posts CRUD    - Comments    - File Upload    - Like/Bookmark   - 그룹 권한    │
│                                                                                                         │
└─────────────────────────────────────────────────────────────────────────────────────────────────────────┘
       │                           │                                                               │
       │ Event                     │ Event                                                         │ Event
       ▼                           ▼                                                               ▼
┌──────────────────────┐  ┌──────────────────────┐                                  ┌──────────────────────┐
│                      │  │                      │                                  │                      │
│  NAVERCAFE MODULE    │  │  NOTIFICATION MODULE │◄───────────── All Events ───────│  (All Modules)       │
│                      │  │                      │                                  │                      │
│  - Cafe Sync         │  │  - 이메일 알림       │                                  │                      │
│  - Cross-posting     │  │  - 앱 푸시 (FCM)     │                                  │                      │
│  - Webhook 수신      │  │  - SMS 알림          │                                  │                      │
│                      │  │                      │                                  │                      │
└──────────────────────┘  └──────────────────────┘                                  └──────────────────────┘
```

### 3.2 Dependency Rules / 의존성 규칙

| Source Module | Allowed Dependencies | Description |
|---------------|---------------------|-------------|
| user | shared | 핵심 모듈, Membership 이벤트 수신 (등급 변경) |
| membership | shared | 정회원 가입/결제/갱신 처리, 이벤트 발행 |
| landing | shared | 행사 생성 시 이벤트 발행 |
| community | shared | 게시판 권한은 UserGrade enum 공유 |
| admin | shared | 모든 이벤트 구독, 이사 파트 관리 |
| chat | shared | 이벤트 생성 시 채팅방 자동 생성 |
| navercafe | shared | 공지 게시글 작성 시 크로스 포스팅 |
| notification | shared | 모든 모듈 이벤트 구독, 알림 발송 |

**핵심 원칙**: 모듈 간 직접 의존성 금지. 모든 통신은 **이벤트 기반**으로 수행.

---

## 4. Event-Driven Communication / 이벤트 기반 통신

### 4.1 Domain Events / 도메인 이벤트

| Event | Publisher | Subscribers | Description |
|-------|-----------|-------------|-------------|
| `UserRegisteredEvent` | user | chat, admin, notification | 사용자 등록 시 발행 |
| `UserGradeChangedEvent` | user | admin, notification | 등급 변경 시 감사 로그 + 알림 |
| `MembershipAppliedEvent` | membership | admin, notification | 정회원 신청서 제출 시 |
| `MembershipApprovedEvent` | membership | user, admin, notification | 정회원 승인 시 등급 변경 + 알림 |
| `MembershipRejectedEvent` | membership | notification | 정회원 신청 반려 시 알림 |
| `MembershipExpiredEvent` | membership | user, admin, notification | 연회비 만료 시 준회원 강등 |
| `PaymentConfirmedEvent` | membership | admin, notification | 입금 확인 완료 시 |
| `VehicleGracePeriodExpiredEvent` | membership | user, notification | M차량 유예 기간 만료 시 |
| `EventCreatedEvent` | landing | chat | 행사 생성 시 채팅방 자동 생성 |
| `PostCreatedEvent` | community | navercafe, admin | 공지 작성 시 크로스 포스팅 |
| `CommentAddedEvent` | community | admin | 댓글 추가 시 통계 업데이트 |

### 4.2 Event Flow Example / 이벤트 흐름 예시

**행사(Event) 생성 시**:
```
1. Admin이 Event 생성 (Landing Module)
2. EventCreatedEvent 발행
3. Chat Module이 이벤트 구독
4. 자동으로 이벤트용 그룹 채팅방 생성
5. 참가 신청 시 채팅방에 자동 초대
```

**공지 게시글 작성 시**:
```
1. Executive가 공지 게시글 작성 (Community Module)
2. PostCreatedEvent 발행 (isNotice = true)
3. NaverCafe Module이 이벤트 구독
4. 자동으로 네이버 카페에 크로스 포스팅
5. Admin Module에서 감사 로그 기록
```

### 4.3 Event Implementation / 이벤트 구현 패턴

```java
// 이벤트 정의 (Java Record 사용)
// Event Definition (Using Java Record)
public record UserRegisteredEvent(
    Long userId,
    String username,
    String email,
    UserGrade grade,
    OAuthProvider provider,
    LocalDateTime registeredAt
) implements DomainEvent {}

// 이벤트 발행 (Publisher)
// Event Publishing (Publisher)
@Service
@RequiredArgsConstructor
public class UserEventPublisher {
    private final ApplicationEventPublisher eventPublisher;

    public void publishUserRegistered(User user) {
        eventPublisher.publishEvent(new UserRegisteredEvent(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getGrade(),
            user.getOauthProvider(),
            LocalDateTime.now()
        ));
    }
}

// 이벤트 구독 (Subscriber)
// Event Subscription (Subscriber)
@Component
@RequiredArgsConstructor
public class ChatEventListener {
    private final ChatRoomService chatRoomService;

    @ApplicationModuleListener
    public void onEventCreated(EventCreatedEvent event) {
        chatRoomService.createEventChatRoom(
            event.eventId(),
            event.title() + " 채팅방"
        );
    }
}
```

---

## 5. Package Structure per Module / 모듈별 패키지 구조

### 5.1 Standard Module Structure / 표준 모듈 구조

```
kr.mclub.apiserver.{module}/
├── package-info.java              # Spring Modulith 모듈 설정
├── {Module}Module.java            # 모듈 Configuration (선택)
│
├── domain/                        # 도메인 계층
│   ├── {Entity}.java              # JPA 엔티티
│   ├── {ValueObject}.java         # 값 객체
│   └── {Enum}.java                # Enum 정의
│
├── repository/                    # 영속성 계층
│   └── {Entity}Repository.java    # JPA Repository
│
├── service/                       # 비즈니스 계층
│   ├── {Domain}Service.java       # 비즈니스 로직
│   └── {Integration}Service.java  # 외부 연동 서비스
│
├── api/                           # 표현 계층
│   ├── {Domain}Controller.java    # REST Controller
│   └── dto/                       # DTO 패키지
│       ├── {Action}Request.java   # 요청 DTO
│       └── {Action}Response.java  # 응답 DTO
│
├── event/                         # 이벤트 계층
│   ├── {Domain}Event.java         # 도메인 이벤트
│   └── {Domain}EventListener.java # 이벤트 리스너
│
├── webhook/                       # Webhook 핸들러 (필요시)
│   └── {External}WebhookController.java
│
├── grpc/                          # gRPC 서비스 (Chat 모듈)
│   ├── {Service}GrpcService.java
│   └── proto/
│       └── {service}.proto
│
└── internal/                      # 내부 전용 패키지
    └── {Internal}Service.java     # 다른 모듈 접근 불가
```

### 5.2 Package-Info Configuration / 패키지 정보 설정

```java
// kr/mclub/apiserver/user/package-info.java
@org.springframework.modulith.ApplicationModule(
    allowedDependencies = {"shared"}  // 의존 가능한 모듈 명시
)
package kr.mclub.apiserver.user;

// kr/mclub/apiserver/community/package-info.java
@org.springframework.modulith.ApplicationModule(
    allowedDependencies = {"shared"}
)
package kr.mclub.apiserver.community;
```

---

## 6. Security Architecture / 보안 아키텍처

### 6.1 Authentication Methods / 인증 방식

본 시스템은 두 가지 인증 방식을 지원합니다:

1. **OAuth2 Social Login**: Google, Apple, Naver를 통한 소셜 로그인
2. **Passkey (WebAuthn)**: 생체인증 기반 패스워드리스 로그인

### 6.2 OAuth2 Authentication Flow / OAuth2 인증 흐름

```
┌──────────┐     ┌──────────────┐     ┌─────────────┐     ┌──────────────┐
│  Client  │────▶│ OAuth2 Login │────▶│   Provider  │────▶│  Callback    │
└──────────┘     │ (Google/Apple│     │   (Google/  │     │  Handler     │
                 │  /Naver)     │     │  Apple/Naver│     └──────┬───────┘
                 └──────────────┘     └─────────────┘            │
                                                                  ▼
┌──────────┐     ┌──────────────┐     ┌─────────────┐     ┌──────────────┐
│  Client  │◀────│  JWT Token   │◀────│   User      │◀────│  Create/     │
│  (Token) │     │  Response    │     │   Service   │     │  Find User   │
└──────────┘     └──────────────┘     └─────────────┘     └──────────────┘
```

### 6.3 Passkey (WebAuthn) Authentication Flow / 패스키 인증 흐름

**Passkey 등록 (Registration)**:
```
┌──────────┐     ┌──────────────┐     ┌─────────────────┐     ┌──────────────┐
│  Client  │────▶│ Registration │────▶│ Generate        │────▶│ Authenticator│
│          │     │ Options      │     │ Challenge       │     │ (Face ID,    │
└──────────┘     └──────────────┘     │ + RP Info       │     │  Touch ID)   │
                                      └─────────────────┘     └──────┬───────┘
                                                                      │
                                                                      ▼
┌──────────┐     ┌──────────────┐     ┌─────────────────┐     ┌──────────────┐
│  Client  │◀────│  JWT Token   │◀────│ Verify &        │◀────│ Public Key   │
│  (Token) │     │  Response    │     │ Store Credential│     │ Credential   │
└──────────┘     └──────────────┘     └─────────────────┘     └──────────────┘
```

**Passkey 로그인 (Authentication)**:
```
┌──────────┐     ┌──────────────┐     ┌─────────────────┐     ┌──────────────┐
│  Client  │────▶│ Auth Options │────▶│ Generate        │────▶│ Authenticator│
│          │     │              │     │ Challenge       │     │ (Face ID,    │
└──────────┘     └──────────────┘     └─────────────────┘     │  Touch ID)   │
                                                               └──────┬───────┘
                                                                      │
                                                                      ▼
┌──────────┐     ┌──────────────┐     ┌─────────────────┐     ┌──────────────┐
│  Client  │◀────│  JWT Token   │◀────│ Verify          │◀────│  Signed      │
│  (Token) │     │  Response    │     │ Assertion       │     │  Challenge   │
└──────────┘     └──────────────┘     └─────────────────┘     └──────────────┘
```

### 6.4 Authentication Strategy / 인증 전략

| 시나리오 | 권장 방식 | 설명 |
|----------|-----------|------|
| 최초 가입 | OAuth2 | 소셜 계정으로 간편 가입 |
| 일반 로그인 (모바일) | Passkey | Face ID/Touch ID로 빠른 인증 |
| 일반 로그인 (웹) | Passkey 또는 OAuth2 | 브라우저 지원에 따라 선택 |
| Passkey 미지원 기기 | OAuth2 | 폴백 옵션 |
| 추가 보안 필요 시 | Passkey | 피싱 방지, 생체 인증 |

**핵심 원칙**:
- OAuth2로 최초 가입 → Passkey 등록 권장
- 하나의 계정에 여러 Passkey 등록 가능 (기기별)
- OAuth2와 Passkey 모두 동일한 JWT 토큰 발급

### 6.5 Role-Based Access Control / 역할 기반 접근 제어

본 시스템은 8단계 등급 체계를 사용합니다.

> **중요**: UserGrade는 **Enum이 아닌 DB 테이블**로 관리됩니다.
> 임원진(회장)이 등급을 동적으로 추가/삭제할 수 있으며,
> `isSystemGrade=true`인 등급(DEVELOPER, ASSOCIATE)은 삭제할 수 없습니다.

| Grade | Code | Description | Permission Level | System Grade |
|-------|------|-------------|------------------|--------------|
| 개발자 | `DEVELOPER` | 시스템 개발자 (모든 권한) | 10 | O (삭제 불가) |
| 고문 | `ADVISOR` | 역대 회장 등 원로 | 9 | X |
| 회장 | `PRESIDENT` | 현 회장 | 8 | X |
| 부회장 | `VICE_PRESIDENT` | 현 부회장 | 7 | X |
| 이사 | `DIRECTOR` | 파트별 이사 (동적 파트 지정) | 6 | X |
| 정회원 | `REGULAR` | 검증 완료 + 회비 납부 | 5 | X |
| 준회원 | `ASSOCIATE` | 미검증 or 회비 미납 | 3 | O (삭제 불가) |
| 파트너사 | `PARTNER` | 협력 업체 | 2 | X |

**준회원 상태 구분 (AssociateStatus)**:
- `PENDING`: OAuth 가입만 완료, 정회원 신청 전
- `REVIEWING`: 정회원 신청서 제출, 심사 진행 중
- `EXPIRED`: 정회원 → 연회비 미납으로 강등
- `REJECTED`: 정회원 신청 반려됨

### 6.6 Board-Level Permissions / 게시판별 권한

그룹 기반 권한 시스템을 사용하여 유연하게 관리:

```java
// 권한 그룹 (등급과 별개로 정의)
@Entity
public class PermissionGroup {
    private String name;                    // 그룹명 (예: "정회원 기본", "운영진")
    private Set<BoardPermission> permissions; // READ, WRITE, MOVE, COMMENT, DELETE, HARD_DELETE, SHARE
}

// 게시판-권한그룹 매핑
@Entity
public class BoardPermissionMapping {
    private Long boardId;
    private Long permissionGroupId;
    private Set<BoardPermission> permissions; // 이 게시판에서의 권한
}
```

**권한 종류 (BoardPermission)**:
- `READ`: 글읽기 (댓글 포함)
- `WRITE`: 글쓰기
- `MOVE`: 게시글 이동
- `COMMENT`: 댓글쓰기
- `DELETE`: 삭제 (Soft)
- `HARD_DELETE`: 완전 삭제
- `SHARE`: 게시글 공유

### 6.7 API Security Configuration / API 보안 설정

| Path Pattern | Access |
|--------------|--------|
| `/api/v1/auth/**` | Public (인증 없이 접근) |
| `/api/v1/landing/**` | Public (랜딩 페이지) |
| `/api/v1/events` (GET) | Public (이벤트 목록) |
| `/api/v1/admin/**` | ADMIN only |
| `/**` | Authenticated (로그인 필요) |

---

## 7. Real-time Communication / 실시간 통신

### 7.1 gRPC Streaming Architecture / gRPC 스트리밍 아키텍처

```
┌──────────────┐          ┌──────────────┐          ┌──────────────┐
│   Client A   │◀────────▶│              │◀────────▶│   Client B   │
│   (Mobile)   │  gRPC    │  Chat gRPC   │  gRPC    │   (Web)      │
└──────────────┘ Stream   │   Service    │ Stream   └──────────────┘
                          │              │
                          └──────┬───────┘
                                 │
                          ┌──────▼───────┐
                          │   PostgreSQL │
                          │   (Messages) │
                          └──────────────┘
```

### 7.2 Chat Room Types / 채팅방 유형

| Type | Description | Auto Creation |
|------|-------------|---------------|
| DIRECT | 1:1 채팅 | 사용자 요청 시 |
| GROUP | 그룹 채팅 | 사용자 요청 시 |
| EVENT | 이벤트 채팅 | 행사 생성 시 자동 |

---

## 8. External Integrations / 외부 연동

### 8.1 Instagram Integration / 인스타그램 연동

```
┌──────────────┐     ┌──────────────┐     ┌──────────────┐
│  Instagram   │────▶│   Webhook    │────▶│  Instagram   │
│  Graph API   │     │   Endpoint   │     │  SyncService │
└──────────────┘     └──────────────┘     └──────┬───────┘
                                                  │
                                           ┌──────▼───────┐
                                           │  Instagram   │
                                           │  Post Table  │
                                           └──────────────┘
```

### 8.2 Naver Cafe Integration / 네이버 카페 연동

```
┌──────────────┐     ┌──────────────┐     ┌──────────────┐
│  Naver Cafe  │◀───▶│   Webhook    │◀───▶│  CafeSync    │
│     API      │     │   Endpoint   │     │   Service    │
└──────────────┘     └──────────────┘     └──────────────┘
        │                                        │
        │ Cross-posting                          │
        ▼                                        ▼
┌──────────────┐                          ┌──────────────┐
│  Naver Cafe  │◀─────────────────────────│  Community   │
│    (Post)    │    PostCreatedEvent      │    Module    │
└──────────────┘                          └──────────────┘
```

### 8.3 DigitalOcean Spaces / 파일 저장소

```
┌──────────────┐     ┌──────────────┐     ┌──────────────┐
│   Client     │────▶│  Upload API  │────▶│  FileUpload  │
│   (File)     │     │  /api/files  │     │   Service    │
└──────────────┘     └──────────────┘     └──────┬───────┘
                                                  │
                                           ┌──────▼───────┐
                                           │  DigitalOcean│
                                           │    Spaces    │
                                           │  (S3 API)    │
                                           └──────────────┘
```

---

## 9. Configuration / 환경 설정

### 9.1 Environment Variables / 환경 변수

```yaml
# Database / 데이터베이스
DB_HOST: localhost
DB_PORT: 5432
DB_NAME: mck_db
DB_USERNAME: mck_user
DB_PASSWORD: ******

# JWT
JWT_SECRET: ******
JWT_ACCESS_TOKEN_VALIDITY: 3600000      # 1시간
JWT_REFRESH_TOKEN_VALIDITY: 604800000   # 7일

# OAuth2 Providers
GOOGLE_CLIENT_ID: ******
GOOGLE_CLIENT_SECRET: ******
APPLE_CLIENT_ID: ******
APPLE_CLIENT_SECRET: ******
NAVER_CLIENT_ID: ******
NAVER_CLIENT_SECRET: ******

# DigitalOcean Spaces
DO_SPACES_REGION: sgp1
DO_SPACES_ENDPOINT: ******
DO_SPACES_BUCKET: mck-uploads
DO_SPACES_CDN_ENDPOINT: ******
DO_SPACES_ACCESS_KEY: ******
DO_SPACES_SECRET_KEY: ******

# External APIs
INSTAGRAM_ACCESS_TOKEN: ******
INSTAGRAM_WEBHOOK_VERIFY_TOKEN: ******
NAVER_CAFE_CLIENT_ID: ******
NAVER_CAFE_CLIENT_SECRET: ******
NAVER_CAFE_ID: ******

# gRPC
GRPC_PORT: 9090
```

---

## 10. Future Considerations / 향후 고려사항

### 10.1 Scalability / 확장성

1. **Microservices Migration**: 필요시 모듈 단위로 분리 가능
2. **Event Store**: 이벤트 소싱 도입 시 히스토리 추적
3. **CQRS**: 읽기/쓰기 분리로 성능 최적화

### 10.2 Potential New Modules / 추가 가능 모듈

- **analytics**: 사용자 행동 분석, 대시보드 고급 통계
- **marketplace**: 중고거래/공동구매
- **gallery**: 사진 갤러리 전용 (드라이브/서킷 사진)
- **payment-gateway**: 결제 게이트웨이 직접 연동 (PG사 연동)

---

## Document History / 문서 이력

| Version | Date | Author | Description |
|---------|------|--------|-------------|
| 1.0 | 2025-12-30 | Claude | Initial architecture design |
| 1.1 | 2025-12-30 | Claude | 8단계 등급 체계, Membership/Notification 모듈, 그룹 권한 시스템 추가 |
