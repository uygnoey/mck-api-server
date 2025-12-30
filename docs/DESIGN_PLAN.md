# BMW M Club Korea API Server - Spring Modulith 설계 및 구현 계획

## 프로젝트 개요
BMW M Club Korea 커뮤니티 홈페이지 API 서버
- **기술 스택**: Spring Boot 4.0.1, Java 21, Spring Modulith 2.0.1, PostgreSQL, gRPC
- **인증**: OAuth2 (Google, Apple, Naver) + Passkey + JWT
- **파일 저장소**: DigitalOcean Spaces (S3 호환)
- **실시간 통신**: gRPC Streaming (채팅)
- **외부 연동**: Instagram Webhook, Naver Cafe Webhook, OCR API

---

## 모듈 아키텍처 (8개 모듈)

```
kr.mclub.apiserver/
├── shared/          # 공유 커널 (BaseEntity, Security, Exception, Utils)
├── user/            # 사용자 모듈 (OAuth2, Passkey, 프로필, 등급)
├── membership/      # 정회원 가입 모듈 (신청서, 서류검증, 결제, 연회비)
├── landing/         # 랜딩 모듈 (History, 임원진, 이벤트, 인스타그램)
├── community/       # 커뮤니티 모듈 (동적 게시판, 게시글, 댓글, 파일)
├── admin/           # 어드민 모듈 (회원/게시판 관리, 대시보드)
├── chat/            # 채팅 모듈 (gRPC Streaming, 1:1/그룹)
└── navercafe/       # 네이버 카페 모듈 (Webhook, 크로스 포스팅)
```

## 모듈 간 통신: 이벤트 기반

| Event | Publisher | Subscribers |
|-------|-----------|-------------|
| `UserRegisteredEvent` | User | Chat, Admin |
| `MembershipApprovedEvent` | Membership | User (등급 변경), Admin (로그) |
| `MembershipExpiredEvent` | Membership | User (준회원 강등) |
| `PaymentConfirmedEvent` | Membership | Admin (입금 확인 로그) |
| `EventCreatedEvent` | Landing | Chat (자동 채팅방 생성) |
| `PostCreatedEvent` | Community | NaverCafe (공지 크로스포스팅), Admin |
| `UserGradeChangedEvent` | User | Admin (감사 로그) |

---

## 회원 등급 체계 (UserGrade) - DB 테이블로 관리

### 등급 계층 (높음 → 낮음)

| 등급 | 코드 | 설명 | 권한 수준 | 시스템 등급 |
|------|------|------|-----------|-------------|
| 개발자 | `DEVELOPER` | 시스템 개발자 (모든 권한) | 10 | O (삭제 불가) |
| 고문 | `ADVISOR` | 역대 회장 등 원로 | 9 | X |
| 회장 | `PRESIDENT` | 현 회장 | 8 | X |
| 부회장 | `VICE_PRESIDENT` | 현 부회장 | 7 | X |
| 이사 | `DIRECTOR` | 파트별 이사 (동적 파트 지정) | 6 | X |
| 정회원 | `REGULAR` | 검증 완료 + 회비 납부 | 5 | X |
| 준회원 | `ASSOCIATE` | 미검증 or 회비 미납 | 3 | O (삭제 불가) |
| 파트너사 | `PARTNER` | 협력 업체 | 2 | X |

**중요**: UserGrade는 Enum이 아닌 **DB 테이블**로 관리하여 임원진이 동적으로 등급 추가/삭제 가능

### 준회원 상태 구분 (AssociateStatus)

| 상태 | 코드 | 설명 |
|------|------|------|
| 신규 | `PENDING` | OAuth 가입만 완료, 정회원 신청 전 |
| 심사중 | `REVIEWING` | 정회원 신청서 제출, 심사 진행 중 |
| 만료 | `EXPIRED` | 정회원 → 연회비 미납으로 강등 |
| 반려 | `REJECTED` | 정회원 신청 반려됨 |

### 이사 파트 (동적 구성)

```
DirectorPart 테이블
- id, name (파트명: 행사, 홍보, 총무 등)
- permissions (JSON: 부여된 권한 목록)
- created_by (회장 ID)
- is_active
```

회장이 직접 파트를 생성/삭제하고 권한을 부여

---

## Membership 모듈 상세 설계

### 정회원 가입 프로세스 흐름

```
[1. OAuth 로그인] → 준회원(PENDING) 자동 부여
       ↓
[2. 정회원 신청서 작성]
   - 이름, 연락처
   - 차량번호, 차대번호
   - 차량 소유 유형 선택
       ↓
[3. 서류 업로드] (차량 소유 유형별)
   ┌─ 개인: 차량등록증 + 신분증
   ├─ 법인: 차량등록증 + 사업자등록증 + 재직증명서
   ├─ 리스/렌트: 차량등록증 + 리스/렌트 계약서
   └─ 법인 리스/렌트: 위 법인 + 리스/렌트 서류 모두
       ↓
[4. OCR 자동 추출 + 대조]
   - 차량등록증: 차량번호, 차대번호, 소유자명
   - 신분증: 이름 (주민번호 뒷자리 마스킹 확인)
   - 사업자등록증: 상호, 대표자명
   - 계약서: 계약자명, 차량정보
       ↓
[5. 관리자 최종 승인]
   - OCR 결과 검토
   - 불일치 시 반려 + 사유 작성
       ↓
[6. 입금 안내]
   - 입회비 20만원 + 연회비 20만원 = 40만원
   - 계좌 안내 + 입금자명 안내
       ↓
[7. 입금 확인] (관리자)
   - 입금자명 + 금액 확인
   - 확인 완료 시 정회원(REGULAR) 승급
       ↓
[8. 정회원 완료]
   - 멤버십 유효기간: 1년
   - 연회비 만료 30일 전 알림
```

### 연회비 갱신 프로세스

```
[만료 30일 전] → 알림 발송 (이메일/푸시)
       ↓
[만료일] → 미납 시 즉시 준회원(EXPIRED) 강등
       ↓
[재납부 시] → 정회원 복귀 (신청서 재작성 불필요)
```

### 연회비 갱신 시점 정책

**핵심 원칙**:
- 연회비는 **매년 초(1월)** 에 갱신 진행
- 해당 년도 연회비 = **1월 1일 ~ 12월 31일** 유효
- **이월 기간**: 임원진(이사)이 매년 설정 (예: 1월 15일까지 전년도 가입자로 인정)

**시나리오별 처리**:

```
┌─────────────────────────────────────────────────────────────────────┐
│                        2025년 연회비 갱신                            │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│  [2024년 12월 15일 가입자]                                           │
│  → 이월 기간(2025년 1월 15일) 내에 있으면 2024년 연회비로 처리        │
│  → 2025년 연회비는 2025년 1월~12월 납부                              │
│                                                                     │
│  [2025년 1월 10일 가입자]                                            │
│  → 이월 기간 설정에 따라 2024년 or 2025년 연회비로 처리               │
│  → 이월 기간이 1월 15일이면, 2024년 연회비로 처리됨                   │
│                                                                     │
│  [2025년 2월 1일 가입자]                                             │
│  → 2025년 연회비로 처리 (해당 년도 가입)                             │
│  → 2025년 12월 31일까지 유효                                        │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
```

**이월 기간 설정 엔티티**:

```java
// 연회비 기간 설정 (매년 임원진이 설정)
@Entity
public class AnnualFeeConfig {
    private Long id;
    private Integer targetYear;              // 대상 년도 (예: 2025)
    private LocalDate carryOverDeadline;     // 이월 마감일 (예: 2025-01-15)
    private LocalDate renewalStartDate;      // 갱신 시작일 (예: 2025-01-01)
    private LocalDate renewalDeadline;       // 갱신 마감일 (예: 2025-01-31)
    private BigDecimal annualFeeAmount;      // 연회비 금액 (기본 200,000원)
    private Long configuredBy;               // 설정한 임원 ID
    private LocalDateTime configuredAt;      // 설정 일시
    private String notes;                    // 비고 (예: "설 연휴로 마감일 연장")
}
```

### 연회비 갱신 면제 케이스

| 케이스 | 면제 유형 | 설명 |
|--------|-----------|------|
| 고문 | PERMANENT (영구 면제) | 역대 회장 등 원로 (평생 면제) |
| 명예정회원 | PERMANENT (영구 면제) | 특별 공로자 지정 (평생 면제) |
| 특별면제 | ONE_TIME (1회성 면제) | 관리자가 지정 (해당 년도만 면제, 다음 해부터 납부) |
| 일반 정회원 | NONE | 매년 갱신 (연 20만원) |

```java
// 면제 유형 Enum
public enum ExemptionType {
    NONE,           // 면제 아님 (일반 정회원)
    PERMANENT,      // 영구 면제 (고문, 명예정회원)
    ONE_TIME        // 1회성 면제 (해당 년도만)
}
```

---

## 다중 차량 등록 시스템

**다중 차량 지원**:
- 한 회원이 여러 M 차량 소유 시 **모두 등록 가능**
- 가입 신청 시: 최소 1대 필수, 추가 차량 선택
- 회원 정보 수정 시: 차량 추가/삭제 가능

```java
// MemberVehicle.java (다대다 관계)
@Entity
public class MemberVehicle {
    private Long id;
    private Long userId;
    private String carNumber;           // 차량번호
    private String vinNumber;           // 차대번호 (중복 불가)
    private String carModel;            // 차종 (예: M3, M4, M5 등)
    private VehicleOwnershipType ownershipType;  // 소유 유형
    private VehicleStatus status;       // ACTIVE, SOLD, GRACE_PERIOD
    private LocalDate registeredAt;     // 등록일
    private LocalDate soldAt;           // 매각일 (SOLD 시)
    private LocalDate gracePeriodEndAt; // 유예 종료일 (M차량 없을 때)
    private boolean isPrimary;          // 대표 차량 여부
}
```

### 차량 변경 및 유예 정책

**M 차량 없어질 경우 (매각/폐차)**:
- **1년 유예 기간** 부여
- 유예 기간 내 새 M 차량 등록 시 → 정회원 유지
- 유예 기간 만료 + 새 M 차량 미등록 → **준회원 강등**

---

## 정회원 번호 시스템

정회원이 되면 **고유 정회원 번호** 부여 (3자리 숫자, 순차 발급)
- 현재 610번대까지 발급됨
- **번호는 영구 소유**: 준회원 강등 후 재가입 시에도 기존 번호 유지
- **탈퇴 후에도 번호 유지**: 해당 번호는 다른 회원에게 재발급 불가
- 모든 회원은 **본명(실명)** 사용

### 등급별 활동명 표시 규칙

| 등급 | 표시 형식 | 예시 |
|------|-----------|------|
| 고문 | 번호 + 이름 (고문) | "001 홍길동 (고문)" |
| 회장 | 번호 + 이름 (회장) | "150 김회장 (회장)" |
| 부회장 | 번호 + 이름 (부회장) | "200 박부회장 (부회장)" |
| 이사 | 번호 + 이름 (○○이사) | "320 최이사 (행사이사)" |
| 정회원 | 번호 + 이름 | "610 홍길동" |
| 준회원 (정회원 출신) | 번호 + 이름 (준회원) | "523 김철수 (준회원)" |
| 준회원 (신규) | 이름 (준회원) | "이영희 (준회원)" |
| 파트너사 | 업체명 (파트너) | "BMW코리아 (파트너)" |

---

## 그룹 기반 권한 시스템 (커뮤니티 전용)

권한은 **그룹별**로 관리하며, **게시판(커뮤니티)에서만** 적용

### 권한 종류 (BoardPermission)

| 권한 코드 | 설명 | 비고 |
|-----------|------|------|
| `READ` | 글읽기 (댓글 읽기 포함) | 기본 권한 |
| `WRITE` | 글쓰기 | - |
| `MOVE` | 게시글 이동 | 권한 있는 게시판 간 이동 |
| `COMMENT` | 댓글쓰기 | - |
| `DELETE` | 삭제 (Soft Delete) | 본인 글/댓글 또는 관리 권한 |
| `HARD_DELETE` | 완전 삭제 | 관리자/운영진만 |
| `SHARE` | 게시글 공유 | 외부 공유 링크 생성 |

### 권한 그룹 (PermissionGroup) - 동적 관리 가능

임원진이 권한 그룹을 자유롭게 추가/삭제 가능

| 그룹명 | 기본 권한 | 대상 |
|--------|-----------|------|
| 운영진 | READ, WRITE, MOVE, COMMENT, DELETE, HARD_DELETE, SHARE | 회장, 부회장, 이사 |
| 정회원 기본 | READ, WRITE, COMMENT, DELETE, SHARE | 정회원 |
| 준회원 제한 | READ | 준회원 |
| 파트너 전용 | READ, WRITE, COMMENT | 파트너사 (특정 게시판만) |

### 게시판별 권한 매핑

각 게시판마다 권한 그룹별로 다른 권한 설정 가능

```
[자유게시판]
├─ 운영진 그룹: READ, WRITE, MOVE, COMMENT, DELETE, HARD_DELETE, SHARE
├─ 정회원 기본 그룹: READ, WRITE, COMMENT, DELETE, SHARE
└─ 준회원 제한 그룹: READ

[공지사항]
├─ 운영진 그룹: READ, WRITE, MOVE, DELETE, HARD_DELETE
├─ 정회원 기본 그룹: READ
└─ 준회원 제한 그룹: READ

[운영진 전용 게시판]
└─ 운영진 그룹: READ, WRITE, COMMENT, DELETE
    (다른 그룹은 접근 불가)
```

---

## 구현 로드맵

### Phase 1: Foundation
**목표**: 인증 시스템 및 핵심 인프라 구축

1. **Shared Kernel 구현**
   - BaseEntity, BaseTimeEntity, DomainEvent
   - GlobalExceptionHandler, ErrorCode
   - SecurityConfig, JwtTokenProvider
   - ApiResponse, PageResponse

2. **User Module 구현**
   - User, UserGrade, OAuthProvider 엔티티
   - OAuth2 로그인 (Google, Apple, Naver)
   - JWT 토큰 발급/검증

3. **DB Migration**
   - `V1__create_user_module_tables.sql`

### Phase 2: Core Features
**목표**: 커뮤니티, 멤버십, 랜딩 페이지 기능

1. **Membership Module 구현**
   - 정회원 신청서, 서류검증, 결제
   - 연회비 갱신, 차량 관리

2. **Community Module 구현**
   - Board (동적 게시판), Post, Comment, Attachment
   - 게시판별 권한 설정
   - DigitalOcean Spaces 파일 업로드

3. **Landing Module 구현**
   - ClubHistory, Executive, Event, InstagramPost

### Phase 3: Admin & Integration
**목표**: 관리자 기능 및 외부 서비스 연동

1. **Admin Module 구현**
   - 회원 관리, 게시판 관리
   - 대시보드 통계
   - 감사 로그

2. **NaverCafe Module 구현**
   - Naver Cafe Webhook 수신
   - 크로스 포스팅

### Phase 4: Real-time & Polish
**목표**: 실시간 채팅 및 품질 확보

1. **Chat Module 구현**
   - gRPC Bidirectional Streaming
   - 1:1 채팅, 그룹 채팅 (이벤트별)

2. **테스트 및 최적화**
   - Spring Modulith 모듈 구조 검증
   - 개별 모듈 테스트

---

## API 엔드포인트 요약

| 모듈 | Base Path | 주요 기능 |
|------|-----------|-----------|
| User | `/api/v1/auth/**`, `/api/v1/users/**` | OAuth2/Passkey 로그인, 프로필 |
| Membership | `/api/v1/membership/**` | 정회원 신청, 서류검증, 결제, 갱신 |
| Landing | `/api/v1/landing/**`, `/api/v1/events/**` | 랜딩 데이터, 이벤트 |
| Community | `/api/v1/boards/**`, `/api/v1/posts/**` | 게시판, 게시글 |
| Admin | `/api/v1/admin/**` | 관리자 기능, 회원 승인 |
| Chat | `/api/v1/chat/**` + gRPC | 채팅 |
| NaverCafe | `/api/v1/navercafe/**` | 카페 연동 |

---

## 기존 데이터 마이그레이션

기존 610명 정회원 데이터 마이그레이션 필요

### 마이그레이션 전략

```
[1. 기존 데이터 확인]
   - 기존 데이터 형식 확인 (Excel? DB? 구글시트?)
   - 필드 매핑 정의

[2. 마이그레이션 스크립트]
   - 기본 정보만 먼저 import (번호, 이름, 전화번호)
   - 차량정보는 향후 회원이 직접 등록하도록 유도
   - OAuth 연동 시 기존 회원과 매칭 (전화번호 or 이름)

[3. OAuth 연동 시나리오]
   - 기존 회원이 OAuth 가입 시 → 전화번호로 기존 데이터 매칭
   - 매칭 성공 → 정회원번호/등급 자동 부여
   - 매칭 실패 → 신규 준회원으로 처리
```
