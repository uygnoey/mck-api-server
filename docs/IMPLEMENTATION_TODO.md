# BMW M Club Korea API Server - Implementation TODO
# BMW M Club Korea API 서버 - 구현 TODO 목록

> 모듈별, 기능별 상세 구현 항목
> Last Updated: 2025-01-01

---

## 목차

1. [Phase 1: Foundation](#phase-1-foundation)
   - [1.1 Shared Kernel](#11-shared-kernel-공유-커널)
   - [1.2 User Module](#12-user-module-사용자-모듈)
2. [Phase 2: Core Features](#phase-2-core-features)
   - [2.1 Membership Module](#21-membership-module-정회원-가입-모듈)
   - [2.2 Community Module](#22-community-module-커뮤니티-모듈)
   - [2.3 Landing Module](#23-landing-module-랜딩-모듈)
3. [Phase 3: Admin & Integration](#phase-3-admin--integration)
   - [3.1 Admin Module](#31-admin-module-관리자-모듈)
   - [3.2 NaverCafe Module](#32-navercafe-module-네이버-카페-모듈)
4. [Phase 4: Real-time & Polish](#phase-4-real-time--polish)
   - [4.1 Chat Module](#41-chat-module-채팅-모듈)
   - [4.2 Testing & Quality](#42-testing--quality)
5. [Database Migration](#database-migration)
6. [External Integration](#external-integration)

---

## Phase 1: Foundation

### 1.1 Shared Kernel (공유 커널)

#### 1.1.1 Domain Base Classes
| 항목 | 파일 경로 | 상태 | 우선순위 |
|------|----------|------|----------|
| BaseEntity 구현 | `shared/domain/BaseEntity.java` | [ ] | P0 |
| BaseTimeEntity 구현 | `shared/domain/BaseTimeEntity.java` | [ ] | P0 |
| DomainEvent 인터페이스 | `shared/domain/DomainEvent.java` | [ ] | P0 |

**BaseEntity 상세 항목**:
- [ ] `id` (Long, @Id, @GeneratedValue)
- [ ] `createdAt` (LocalDateTime, @CreatedDate)
- [ ] `updatedAt` (LocalDateTime, @LastModifiedDate)
- [ ] `createdBy` (Long, @CreatedBy)
- [ ] `updatedBy` (Long, @LastModifiedBy)
- [ ] JPA Auditing 설정 (@EnableJpaAuditing)

#### 1.1.2 Exception Handling
| 항목 | 파일 경로 | 상태 | 우선순위 |
|------|----------|------|----------|
| BusinessException 기본 클래스 | `shared/exception/BusinessException.java` | [ ] | P0 |
| ErrorCode Enum 정의 | `shared/exception/ErrorCode.java` | [ ] | P0 |
| GlobalExceptionHandler | `shared/exception/GlobalExceptionHandler.java` | [ ] | P0 |

**ErrorCode 상세 항목**:
- [ ] Common errors (INVALID_INPUT, NOT_FOUND, FORBIDDEN, etc.)
- [ ] Auth errors (UNAUTHORIZED, TOKEN_EXPIRED, OAUTH_FAILED, etc.)
- [ ] User errors (USER_NOT_FOUND, DUPLICATE_EMAIL, etc.)
- [ ] Membership errors (APPLICATION_NOT_FOUND, DOCUMENT_REQUIRED, etc.)
- [ ] Community errors (BOARD_NOT_FOUND, POST_NOT_FOUND, etc.)
- [ ] Payment errors (PAYMENT_NOT_FOUND, AMOUNT_MISMATCH, etc.)

#### 1.1.3 Security Configuration
| 항목 | 파일 경로 | 상태 | 우선순위 |
|------|----------|------|----------|
| SecurityConfig 메인 설정 | `shared/security/SecurityConfig.java` | [ ] | P0 |
| JwtTokenProvider 토큰 관리 | `shared/security/JwtTokenProvider.java` | [ ] | P0 |
| JwtAuthenticationFilter | `shared/security/JwtAuthenticationFilter.java` | [ ] | P0 |
| CurrentUser 어노테이션 | `shared/security/CurrentUser.java` | [ ] | P1 |
| CurrentUserArgumentResolver | `shared/security/CurrentUserArgumentResolver.java` | [ ] | P1 |

**JwtTokenProvider 상세 항목**:
- [ ] Access Token 생성 (15분 만료)
- [ ] Refresh Token 생성 (7일 만료)
- [ ] Token 검증 로직
- [ ] Token에서 사용자 정보 추출
- [ ] Token 갱신 로직

**SecurityConfig 상세 항목**:
- [ ] CORS 설정 (프론트엔드 도메인)
- [ ] CSRF 비활성화 (REST API)
- [ ] 세션 STATELESS 설정
- [ ] 경로별 인증 요구 설정
- [ ] OAuth2 로그인 설정
- [ ] JWT 필터 등록

#### 1.1.4 Utility Classes
| 항목 | 파일 경로 | 상태 | 우선순위 |
|------|----------|------|----------|
| ApiResponse 표준 응답 | `shared/util/ApiResponse.java` | [ ] | P0 |
| PageResponse 페이지네이션 | `shared/util/PageResponse.java` | [ ] | P1 |
| DateTimeUtils 날짜 유틸 | `shared/util/DateTimeUtils.java` | [ ] | P2 |

---

### 1.2 User Module (사용자 모듈)

#### 1.2.1 Domain Entities
| 항목 | 파일 경로 | 상태 | 우선순위 |
|------|----------|------|----------|
| User 엔티티 | `user/domain/User.java` | [ ] | P0 |
| UserGrade 엔티티 (DB 테이블) | `user/domain/UserGrade.java` | [ ] | P0 |
| AssociateStatus Enum | `user/domain/AssociateStatus.java` | [ ] | P0 |
| ExemptionType Enum | `user/domain/ExemptionType.java` | [ ] | P0 |
| OAuthProvider Enum | `user/domain/OAuthProvider.java` | [ ] | P0 |
| OAuthAccount 엔티티 | `user/domain/OAuthAccount.java` | [ ] | P0 |
| PasskeyCredential 엔티티 | `user/domain/PasskeyCredential.java` | [ ] | P1 |

**User 엔티티 필드**:
- [ ] `id` (Long, PK)
- [ ] `memberNumber` (Integer, UNIQUE, 정회원 번호)
- [ ] `realName` (String, 실명)
- [ ] `email` (String, UNIQUE)
- [ ] `phoneNumber` (String)
- [ ] `profileImageUrl` (String)
- [ ] `grade` (UserGrade, @ManyToOne)
- [ ] `associateStatus` (AssociateStatus)
- [ ] `exemptionType` (ExemptionType)
- [ ] `exemptionReason` (String)
- [ ] `exemptionYear` (Integer)
- [ ] `directorPartId` (Long)
- [ ] `partnerCompanyName` (String)
- [ ] `isWithdrawn` (boolean)
- [ ] `withdrawnAt` (LocalDateTime)
- [ ] `withdrawalReason` (String)
- [ ] `getDisplayName()` 메서드 구현

**UserGrade 엔티티 필드** (DB 테이블):
- [ ] `id` (Long, PK)
- [ ] `code` (String, UNIQUE - DEVELOPER, ADVISOR, etc.)
- [ ] `name` (String - 개발자, 고문, etc.)
- [ ] `roleName` (String - ROLE_DEVELOPER, etc.)
- [ ] `permissionLevel` (Integer - 10, 9, 8, ...)
- [ ] `isExecutive` (boolean)
- [ ] `isStaff` (boolean)
- [ ] `isSystemGrade` (boolean - 삭제 불가)
- [ ] `displaySuffix` (String - "(고문)", "(회장)", etc.)
- [ ] `displayOrder` (Integer)
- [ ] `isActive` (boolean)
- [ ] `isHigherOrEqualTo(UserGrade)` 메서드

#### 1.2.2 Repositories
| 항목 | 파일 경로 | 상태 | 우선순위 |
|------|----------|------|----------|
| UserRepository | `user/repository/UserRepository.java` | [ ] | P0 |
| UserGradeRepository | `user/repository/UserGradeRepository.java` | [ ] | P0 |
| OAuthAccountRepository | `user/repository/OAuthAccountRepository.java` | [ ] | P0 |
| PasskeyCredentialRepository | `user/repository/PasskeyCredentialRepository.java` | [ ] | P1 |

**UserRepository 메서드**:
- [ ] `findByEmail(String email)`
- [ ] `findByMemberNumber(Integer memberNumber)`
- [ ] `findByPhoneNumber(String phoneNumber)`
- [ ] `existsByEmail(String email)`
- [ ] `existsByGradeId(Long gradeId)`
- [ ] `findByGradeCodeAndIsWithdrawnFalse(String gradeCode)`
- [ ] `findRegularMembersForRenewal(Integer targetYear)` (연회비 갱신 대상)

**UserGradeRepository 메서드**:
- [ ] `findByCode(String code)`
- [ ] `findByRoleName(String roleName)`
- [ ] `findByIsActiveOrderByDisplayOrderAsc(boolean isActive)`
- [ ] `findDeletableGrades()` (시스템 등급 제외)
- [ ] `existsByCode(String code)`

#### 1.2.3 Services
| 항목 | 파일 경로 | 상태 | 우선순위 |
|------|----------|------|----------|
| UserService | `user/service/UserService.java` | [ ] | P0 |
| UserGradeService | `user/service/UserGradeService.java` | [ ] | P0 |
| OAuth2UserService | `user/service/OAuth2UserService.java` | [ ] | P0 |
| PasskeyService | `user/service/PasskeyService.java` | [ ] | P1 |
| ProfileService | `user/service/ProfileService.java` | [ ] | P1 |

**UserService 메서드**:
- [ ] `getUserById(Long id)`
- [ ] `getUserByEmail(String email)`
- [ ] `updateProfile(Long userId, ProfileUpdateRequest request)`
- [ ] `changeGrade(Long userId, Long newGradeId, Long adminId)`
- [ ] `withdraw(Long userId, String reason)`
- [ ] `getDisplayName(Long userId)`

**UserGradeService 메서드**:
- [ ] `getAllActiveGrades()`
- [ ] `getGradeByCode(String code)`
- [ ] `createGrade(UserGradeCreateRequest request, Long creatorId)`
- [ ] `updateGrade(Long gradeId, UserGradeUpdateRequest request)`
- [ ] `deleteGrade(Long gradeId)` (시스템 등급 삭제 방지)
- [ ] `getDefaultGradeForNewUser()` (ASSOCIATE 반환)

**OAuth2UserService 메서드**:
- [ ] `processOAuthLogin(OAuth2UserInfo info, OAuthProvider provider)`
- [ ] `linkOAuthAccount(Long userId, OAuth2UserInfo info)`
- [ ] `unlinkOAuthAccount(Long userId, OAuthProvider provider)`
- [ ] `matchLegacyMember(String phoneNumber)` (기존 회원 매칭)

#### 1.2.4 Controllers & DTOs
| 항목 | 파일 경로 | 상태 | 우선순위 |
|------|----------|------|----------|
| AuthController | `user/api/AuthController.java` | [ ] | P0 |
| UserController | `user/api/UserController.java` | [ ] | P0 |
| ProfileController | `user/api/ProfileController.java` | [ ] | P1 |

**AuthController 엔드포인트**:
- [ ] `POST /api/v1/auth/oauth/{provider}` - OAuth 로그인
- [ ] `POST /api/v1/auth/refresh` - 토큰 갱신
- [ ] `POST /api/v1/auth/logout` - 로그아웃
- [ ] `POST /api/v1/auth/passkey/register` - Passkey 등록
- [ ] `POST /api/v1/auth/passkey/authenticate` - Passkey 인증

**UserController 엔드포인트**:
- [ ] `GET /api/v1/users/me` - 내 정보 조회
- [ ] `PUT /api/v1/users/me` - 내 정보 수정
- [ ] `DELETE /api/v1/users/me` - 회원 탈퇴

**DTO 목록**:
- [ ] `OAuthLoginRequest`, `OAuthLoginResponse`
- [ ] `TokenRefreshRequest`, `TokenRefreshResponse`
- [ ] `UserProfileResponse`
- [ ] `ProfileUpdateRequest`
- [ ] `RealNameChangeRequest`
- [ ] `UserGradeCreateRequest`, `UserGradeUpdateRequest`
- [ ] `PasskeyRegistrationRequest`, `PasskeyRegistrationResponse`

#### 1.2.5 Events
| 항목 | 파일 경로 | 상태 | 우선순위 |
|------|----------|------|----------|
| UserRegisteredEvent | `user/event/UserRegisteredEvent.java` | [ ] | P1 |
| UserGradeChangedEvent | `user/event/UserGradeChangedEvent.java` | [ ] | P1 |
| UserWithdrawnEvent | `user/event/UserWithdrawnEvent.java` | [ ] | P2 |
| UserEventPublisher | `user/event/UserEventPublisher.java` | [ ] | P1 |

---

## Phase 2: Core Features

### 2.1 Membership Module (정회원 가입 모듈)

#### 2.1.1 Domain Entities
| 항목 | 파일 경로 | 상태 | 우선순위 |
|------|----------|------|----------|
| MembershipApplication 신청서 | `membership/domain/MembershipApplication.java` | [ ] | P0 |
| VehicleOwnershipType Enum | `membership/domain/VehicleOwnershipType.java` | [ ] | P0 |
| ApplicationDocument 서류 | `membership/domain/ApplicationDocument.java` | [ ] | P0 |
| DocumentType Enum | `membership/domain/DocumentType.java` | [ ] | P0 |
| OcrResult OCR 결과 | `membership/domain/OcrResult.java` | [ ] | P1 |
| VerificationStatus Enum | `membership/domain/VerificationStatus.java` | [ ] | P0 |
| PaymentRecord 입금 기록 | `membership/domain/PaymentRecord.java` | [ ] | P0 |
| PaymentType Enum | `membership/domain/PaymentType.java` | [ ] | P0 |
| PaymentStatus Enum | `membership/domain/PaymentStatus.java` | [ ] | P0 |
| MembershipPeriod 회원권 기간 | `membership/domain/MembershipPeriod.java` | [ ] | P0 |
| DirectorPart 이사 파트 | `membership/domain/DirectorPart.java` | [ ] | P1 |
| MemberVehicle 회원 차량 | `membership/domain/MemberVehicle.java` | [ ] | P0 |
| VehicleStatus Enum | `membership/domain/VehicleStatus.java` | [ ] | P0 |
| AnnualFeeConfig 연회비 설정 | `membership/domain/AnnualFeeConfig.java` | [ ] | P0 |

**MembershipApplication 필드**:
- [ ] `id` (Long, PK)
- [ ] `userId` (Long)
- [ ] `status` (VerificationStatus)
- [ ] `realName` (String)
- [ ] `phoneNumber` (String)
- [ ] `carNumber` (String)
- [ ] `vinNumber` (String)
- [ ] `ownershipType` (VehicleOwnershipType)
- [ ] `rejectionReason` (String)
- [ ] `reviewedByAdminId` (Long)
- [ ] `reviewedAt` (LocalDateTime)

**VehicleOwnershipType Enum 값**:
- [ ] `PERSONAL` - 개인
- [ ] `CORPORATE` - 법인
- [ ] `LEASE` - 리스
- [ ] `RENTAL` - 렌트
- [ ] `CORPORATE_LEASE` - 법인 리스
- [ ] `CORPORATE_RENTAL` - 법인 렌트

**DocumentType Enum 값**:
- [ ] `VEHICLE_REGISTRATION` - 차량등록증
- [ ] `ID_CARD` - 신분증
- [ ] `BUSINESS_LICENSE` - 사업자등록증
- [ ] `EMPLOYMENT_CERTIFICATE` - 재직증명서
- [ ] `LEASE_CONTRACT` - 리스 계약서
- [ ] `RENTAL_CONTRACT` - 렌트 계약서

**PaymentRecord 필드**:
- [ ] `id` (Long, PK)
- [ ] `userId` (Long)
- [ ] `paymentType` (PaymentType - ENROLLMENT_FEE, ANNUAL_FEE)
- [ ] `amount` (BigDecimal)
- [ ] `depositorName` (String)
- [ ] `depositDate` (LocalDate)
- [ ] `status` (PaymentStatus - PENDING, CONFIRMED, CANCELLED)
- [ ] `confirmedByAdminId` (Long)
- [ ] `confirmedAt` (LocalDateTime)
- [ ] `autoConfirmed` (boolean)
- [ ] `bankTransactionId` (String)

**AnnualFeeConfig 필드**:
- [ ] `id` (Long, PK)
- [ ] `targetYear` (Integer)
- [ ] `carryOverDeadline` (LocalDate - 이월 마감일)
- [ ] `renewalStartDate` (LocalDate)
- [ ] `renewalDeadline` (LocalDate)
- [ ] `annualFeeAmount` (BigDecimal)
- [ ] `configuredByAdminId` (Long)
- [ ] `configuredAt` (LocalDateTime)
- [ ] `notes` (String)

**MemberVehicle 필드**:
- [ ] `id` (Long, PK)
- [ ] `userId` (Long)
- [ ] `carNumber` (String)
- [ ] `vinNumber` (String, UNIQUE)
- [ ] `carModel` (String)
- [ ] `ownershipType` (VehicleOwnershipType)
- [ ] `status` (VehicleStatus - ACTIVE, SOLD, GRACE_PERIOD)
- [ ] `registeredAt` (LocalDate)
- [ ] `soldAt` (LocalDate)
- [ ] `gracePeriodEndAt` (LocalDate)
- [ ] `isPrimary` (boolean)

#### 2.1.2 Repositories
| 항목 | 파일 경로 | 상태 | 우선순위 |
|------|----------|------|----------|
| MembershipApplicationRepository | `membership/repository/MembershipApplicationRepository.java` | [ ] | P0 |
| ApplicationDocumentRepository | `membership/repository/ApplicationDocumentRepository.java` | [ ] | P0 |
| PaymentRecordRepository | `membership/repository/PaymentRecordRepository.java` | [ ] | P0 |
| MembershipPeriodRepository | `membership/repository/MembershipPeriodRepository.java` | [ ] | P0 |
| DirectorPartRepository | `membership/repository/DirectorPartRepository.java` | [ ] | P1 |
| MemberVehicleRepository | `membership/repository/MemberVehicleRepository.java` | [ ] | P0 |
| AnnualFeeConfigRepository | `membership/repository/AnnualFeeConfigRepository.java` | [ ] | P0 |

**MembershipApplicationRepository 메서드**:
- [ ] `findByUserId(Long userId)`
- [ ] `findByStatus(VerificationStatus status)`
- [ ] `findPendingApplications()` (심사 대기 목록)

**MemberVehicleRepository 메서드**:
- [ ] `findByUserId(Long userId)`
- [ ] `findByVinNumber(String vinNumber)`
- [ ] `findByStatusAndGracePeriodEndAtBefore(VehicleStatus status, LocalDate date)`
- [ ] `existsByVinNumber(String vinNumber)`
- [ ] `findPrimaryVehicleByUserId(Long userId)`

**AnnualFeeConfigRepository 메서드**:
- [ ] `findByTargetYear(Integer targetYear)`
- [ ] `findLatestConfig()`

#### 2.1.3 Services
| 항목 | 파일 경로 | 상태 | 우선순위 |
|------|----------|------|----------|
| MembershipApplicationService | `membership/service/MembershipApplicationService.java` | [ ] | P0 |
| DocumentVerificationService | `membership/service/DocumentVerificationService.java` | [ ] | P0 |
| OcrService 인터페이스 | `membership/service/OcrService.java` | [ ] | P0 |
| PaddleOcrService 구현체 | `membership/service/PaddleOcrService.java` | [ ] | P1 |
| PaymentService | `membership/service/PaymentService.java` | [ ] | P0 |
| OpenBankingService 인터페이스 | `membership/service/OpenBankingService.java` | [ ] | P2 |
| MembershipRenewalService | `membership/service/MembershipRenewalService.java` | [ ] | P0 |
| DirectorPartService | `membership/service/DirectorPartService.java` | [ ] | P1 |
| VehicleManagementService | `membership/service/VehicleManagementService.java` | [ ] | P0 |
| AnnualFeeService | `membership/service/AnnualFeeService.java` | [ ] | P0 |

**MembershipApplicationService 메서드**:
- [ ] `submitApplication(Long userId, MembershipApplicationRequest request)`
- [ ] `uploadDocument(Long applicationId, DocumentType type, MultipartFile file)`
- [ ] `getApplicationStatus(Long userId)`
- [ ] `approveApplication(Long applicationId, Long adminId)`
- [ ] `rejectApplication(Long applicationId, String reason, Long adminId)`

**AnnualFeeService 메서드**:
- [ ] `determineAnnualFeeYear(LocalDate paymentDate)` - 이월 기간 판단
- [ ] `getRenewalTargetMembers(Integer targetYear)`
- [ ] `isExemptForYear(Long userId, Integer targetYear)`
- [ ] `getConfig(Integer targetYear)`
- [ ] `createOrUpdateConfig(AnnualFeeConfigRequest request, Long adminId)`

**VehicleManagementService 메서드**:
- [ ] `registerVehicle(Long userId, VehicleRegistrationRequest request)`
- [ ] `markVehicleAsSold(Long vehicleId, Long userId)`
- [ ] `setPrimaryVehicle(Long vehicleId, Long userId)`
- [ ] `getUserVehicles(Long userId)`
- [ ] `checkDuplicateVin(String vinNumber)`
- [ ] `processGracePeriodExpiration()` (스케줄러 호출)

**PaymentService 메서드**:
- [ ] `createPendingPayment(Long userId, PaymentType type)`
- [ ] `confirmPayment(Long paymentId, PaymentConfirmRequest request, Long adminId)`
- [ ] `getPendingPayments()`
- [ ] `getPaymentHistory(Long userId)`

#### 2.1.4 Controllers & DTOs
| 항목 | 파일 경로 | 상태 | 우선순위 |
|------|----------|------|----------|
| MembershipController | `membership/api/MembershipController.java` | [ ] | P0 |
| PaymentController | `membership/api/PaymentController.java` | [ ] | P0 |
| VehicleController | `membership/api/VehicleController.java` | [ ] | P0 |
| AdminMembershipController | `membership/api/AdminMembershipController.java` | [ ] | P0 |

**MembershipController 엔드포인트**:
- [ ] `POST /api/v1/membership/apply` - 정회원 신청
- [ ] `POST /api/v1/membership/documents` - 서류 업로드
- [ ] `GET /api/v1/membership/status` - 신청 상태 조회
- [ ] `GET /api/v1/membership/period` - 멤버십 기간 조회
- [ ] `POST /api/v1/membership/renew` - 연회비 갱신 신청

**VehicleController 엔드포인트**:
- [ ] `GET /api/v1/vehicles` - 내 차량 목록
- [ ] `POST /api/v1/vehicles` - 차량 등록
- [ ] `PUT /api/v1/vehicles/{id}/primary` - 대표 차량 설정
- [ ] `DELETE /api/v1/vehicles/{id}` - 차량 매각/삭제

**AdminMembershipController 엔드포인트**:
- [ ] `GET /api/v1/admin/membership/applications` - 신청 목록
- [ ] `GET /api/v1/admin/membership/applications/{id}` - 신청 상세
- [ ] `POST /api/v1/admin/membership/applications/{id}/approve` - 승인
- [ ] `POST /api/v1/admin/membership/applications/{id}/reject` - 반려
- [ ] `GET /api/v1/admin/payments/pending` - 입금 대기 목록
- [ ] `POST /api/v1/admin/payments/{id}/confirm` - 입금 확인
- [ ] `GET /api/v1/admin/annual-fee/config/{year}` - 연회비 설정 조회
- [ ] `POST /api/v1/admin/annual-fee/config` - 연회비 설정 생성/수정
- [ ] `POST /api/v1/admin/users/{id}/exemption` - 면제 부여
- [ ] `DELETE /api/v1/admin/users/{id}/exemption` - 면제 해제

**DTO 목록**:
- [ ] `MembershipApplicationRequest`, `MembershipApplicationResponse`
- [ ] `DocumentUploadRequest`, `DocumentUploadResponse`
- [ ] `ApplicationStatusResponse`
- [ ] `MembershipPeriodResponse`
- [ ] `VehicleRegistrationRequest`, `VehicleResponse`
- [ ] `PaymentConfirmRequest`, `PaymentResponse`
- [ ] `AnnualFeeConfigRequest`, `AnnualFeeConfigResponse`
- [ ] `ExemptionRequest`

#### 2.1.5 Schedulers
| 항목 | 파일 경로 | 상태 | 우선순위 |
|------|----------|------|----------|
| MembershipExpirationScheduler | `membership/scheduler/MembershipExpirationScheduler.java` | [ ] | P1 |
| VehicleGracePeriodScheduler | `membership/scheduler/VehicleGracePeriodScheduler.java` | [ ] | P1 |
| RenewalReminderScheduler | `membership/scheduler/RenewalReminderScheduler.java` | [ ] | P2 |

**스케줄러 작업**:
- [ ] 연회비 만료 체크 (매일 자정)
- [ ] 차량 유예 기간 만료 체크 (매일 자정)
- [ ] 갱신 알림 발송 (매년 1월 1일)
- [ ] 만료 30일 전 알림
- [ ] 만료 7일 전 알림

#### 2.1.6 Events
| 항목 | 파일 경로 | 상태 | 우선순위 |
|------|----------|------|----------|
| MembershipAppliedEvent | `membership/event/MembershipAppliedEvent.java` | [ ] | P1 |
| MembershipApprovedEvent | `membership/event/MembershipApprovedEvent.java` | [ ] | P1 |
| MembershipRejectedEvent | `membership/event/MembershipRejectedEvent.java` | [ ] | P1 |
| MembershipExpiredEvent | `membership/event/MembershipExpiredEvent.java` | [ ] | P1 |
| PaymentConfirmedEvent | `membership/event/PaymentConfirmedEvent.java` | [ ] | P1 |
| VehicleAddedEvent | `membership/event/VehicleAddedEvent.java` | [ ] | P2 |
| VehicleSoldEvent | `membership/event/VehicleSoldEvent.java` | [ ] | P2 |
| VehicleGracePeriodExpiredEvent | `membership/event/VehicleGracePeriodExpiredEvent.java` | [ ] | P2 |

---

### 2.2 Community Module (커뮤니티 모듈)

#### 2.2.1 Domain Entities
| 항목 | 파일 경로 | 상태 | 우선순위 |
|------|----------|------|----------|
| Board 게시판 | `community/domain/Board.java` | [ ] | P0 |
| BoardType Enum | `community/domain/BoardType.java` | [ ] | P0 |
| Post 게시글 | `community/domain/Post.java` | [ ] | P0 |
| Comment 댓글 | `community/domain/Comment.java` | [ ] | P0 |
| Attachment 첨부파일 | `community/domain/Attachment.java` | [ ] | P0 |
| PostLike 좋아요 | `community/domain/PostLike.java` | [ ] | P1 |
| PermissionGroup 권한 그룹 | `community/domain/PermissionGroup.java` | [ ] | P0 |
| BoardPermission Enum | `community/domain/BoardPermission.java` | [ ] | P0 |
| BoardPermissionMapping | `community/domain/BoardPermissionMapping.java` | [ ] | P0 |
| UserPermissionGroup | `community/domain/UserPermissionGroup.java` | [ ] | P0 |

**Board 필드**:
- [ ] `id` (Long, PK)
- [ ] `name` (String)
- [ ] `slug` (String, UNIQUE)
- [ ] `description` (String)
- [ ] `boardType` (BoardType)
- [ ] `displayOrder` (Integer)
- [ ] `isActive` (boolean)
- [ ] `requiredGradeToReadId` (Long)
- [ ] `requiredGradeToWriteId` (Long)

**Post 필드**:
- [ ] `id` (Long, PK)
- [ ] `boardId` (Long)
- [ ] `authorId` (Long)
- [ ] `title` (String)
- [ ] `content` (String, TEXT)
- [ ] `viewCount` (Integer)
- [ ] `likeCount` (Integer)
- [ ] `commentCount` (Integer)
- [ ] `isPinned` (boolean)
- [ ] `isNotice` (boolean)
- [ ] `isDeleted` (boolean)
- [ ] `deletedAt` (LocalDateTime)

**PermissionGroup 필드**:
- [ ] `id` (Long, PK)
- [ ] `name` (String, UNIQUE)
- [ ] `description` (String)
- [ ] `permissions` (Set<BoardPermission>)
- [ ] `isDefault` (boolean)
- [ ] `createdByAdminId` (Long)

**BoardPermission Enum 값**:
- [ ] `READ` - 글읽기
- [ ] `WRITE` - 글쓰기
- [ ] `MOVE` - 게시글 이동
- [ ] `COMMENT` - 댓글쓰기
- [ ] `DELETE` - 삭제 (Soft)
- [ ] `HARD_DELETE` - 완전 삭제
- [ ] `SHARE` - 게시글 공유

#### 2.2.2 Repositories
| 항목 | 파일 경로 | 상태 | 우선순위 |
|------|----------|------|----------|
| BoardRepository | `community/repository/BoardRepository.java` | [ ] | P0 |
| PostRepository | `community/repository/PostRepository.java` | [ ] | P0 |
| CommentRepository | `community/repository/CommentRepository.java` | [ ] | P0 |
| AttachmentRepository | `community/repository/AttachmentRepository.java` | [ ] | P0 |
| PermissionGroupRepository | `community/repository/PermissionGroupRepository.java` | [ ] | P0 |
| BoardPermissionMappingRepository | `community/repository/BoardPermissionMappingRepository.java` | [ ] | P0 |
| UserPermissionGroupRepository | `community/repository/UserPermissionGroupRepository.java` | [ ] | P0 |

#### 2.2.3 Services
| 항목 | 파일 경로 | 상태 | 우선순위 |
|------|----------|------|----------|
| BoardService | `community/service/BoardService.java` | [ ] | P0 |
| PostService | `community/service/PostService.java` | [ ] | P0 |
| CommentService | `community/service/CommentService.java` | [ ] | P0 |
| FileUploadService | `community/service/FileUploadService.java` | [ ] | P0 |
| BoardPermissionChecker | `community/service/BoardPermissionChecker.java` | [ ] | P0 |
| PermissionGroupService | `community/service/PermissionGroupService.java` | [ ] | P0 |

**BoardPermissionChecker 메서드**:
- [ ] `hasPermission(Long userId, Long boardId, BoardPermission permission)`
- [ ] `canRead(Long userId, Long boardId)`
- [ ] `canWrite(Long userId, Long boardId)`
- [ ] `canDelete(Long userId, Long boardId, Long postId)`
- [ ] `getDefaultGroupIdForGrade(UserGrade grade)`

**PermissionGroupService 메서드**:
- [ ] `getAllGroups()`
- [ ] `createGroup(PermissionGroupCreateRequest request, Long adminId)`
- [ ] `updateGroup(Long groupId, PermissionGroupUpdateRequest request)`
- [ ] `deleteGroup(Long groupId)`
- [ ] `assignGroupToUser(Long userId, Long groupId, String reason)`
- [ ] `removeGroupFromUser(Long userId, Long groupId)`
- [ ] `setBoardPermissions(Long boardId, Long groupId, Set<BoardPermission> permissions)`

#### 2.2.4 Controllers & DTOs
| 항목 | 파일 경로 | 상태 | 우선순위 |
|------|----------|------|----------|
| BoardController | `community/api/BoardController.java` | [ ] | P0 |
| PostController | `community/api/PostController.java` | [ ] | P0 |
| CommentController | `community/api/CommentController.java` | [ ] | P0 |
| AdminPermissionController | `community/api/AdminPermissionController.java` | [ ] | P0 |

**AdminPermissionController 엔드포인트**:
- [ ] `GET /api/v1/admin/permission-groups` - 권한 그룹 목록
- [ ] `POST /api/v1/admin/permission-groups` - 권한 그룹 생성
- [ ] `PUT /api/v1/admin/permission-groups/{id}` - 권한 그룹 수정
- [ ] `DELETE /api/v1/admin/permission-groups/{id}` - 권한 그룹 삭제
- [ ] `GET /api/v1/admin/boards/{boardId}/permissions` - 게시판 권한 조회
- [ ] `POST /api/v1/admin/boards/{boardId}/permissions` - 게시판 권한 설정
- [ ] `PUT /api/v1/admin/boards/{boardId}/permissions/{groupId}` - 게시판 권한 수정
- [ ] `DELETE /api/v1/admin/boards/{boardId}/permissions/{groupId}` - 게시판 권한 삭제
- [ ] `GET /api/v1/admin/users/{userId}/permission-groups` - 사용자 권한 그룹 조회
- [ ] `POST /api/v1/admin/users/{userId}/permission-groups` - 사용자 권한 그룹 추가
- [ ] `DELETE /api/v1/admin/users/{userId}/permission-groups/{groupId}` - 사용자 권한 그룹 제거

---

### 2.3 Landing Module (랜딩 모듈)

#### 2.3.1 Domain Entities
| 항목 | 파일 경로 | 상태 | 우선순위 |
|------|----------|------|----------|
| ClubHistory 클럽 연혁 | `landing/domain/ClubHistory.java` | [ ] | P1 |
| Executive 임원진 | `landing/domain/Executive.java` | [ ] | P1 |
| Event 이벤트 | `landing/domain/Event.java` | [ ] | P0 |
| EventStatus Enum | `landing/domain/EventStatus.java` | [ ] | P0 |
| EventParticipant 참가자 | `landing/domain/EventParticipant.java` | [ ] | P1 |
| InstagramPost 인스타그램 | `landing/domain/InstagramPost.java` | [ ] | P2 |

#### 2.3.2 Services & Controllers
| 항목 | 파일 경로 | 상태 | 우선순위 |
|------|----------|------|----------|
| LandingService | `landing/service/LandingService.java` | [ ] | P0 |
| EventService | `landing/service/EventService.java` | [ ] | P0 |
| InstagramWebhookController | `landing/webhook/InstagramWebhookController.java` | [ ] | P2 |
| LandingController | `landing/api/LandingController.java` | [ ] | P0 |
| EventController | `landing/api/EventController.java` | [ ] | P0 |

---

## Phase 3: Admin & Integration

### 3.1 Admin Module (관리자 모듈)

#### 3.1.1 Domain & Services
| 항목 | 파일 경로 | 상태 | 우선순위 |
|------|----------|------|----------|
| AuditLog 감사 로그 | `admin/domain/AuditLog.java` | [ ] | P1 |
| DashboardService | `admin/service/DashboardService.java` | [ ] | P1 |
| MemberManagementService | `admin/service/MemberManagementService.java` | [ ] | P0 |
| BoardManagementService | `admin/service/BoardManagementService.java` | [ ] | P1 |
| AuditLogService | `admin/service/AuditLogService.java` | [ ] | P1 |

#### 3.1.2 Controllers
| 항목 | 파일 경로 | 상태 | 우선순위 |
|------|----------|------|----------|
| AdminDashboardController | `admin/api/AdminDashboardController.java` | [ ] | P1 |
| AdminMemberController | `admin/api/AdminMemberController.java` | [ ] | P0 |
| AdminBoardController | `admin/api/AdminBoardController.java` | [ ] | P1 |
| AdminGradeController | `admin/api/AdminGradeController.java` | [ ] | P0 |
| AdminDirectorPartController | `admin/api/AdminDirectorPartController.java` | [ ] | P1 |

**AdminGradeController 엔드포인트**:
- [ ] `GET /api/v1/admin/grades` - 등급 목록 조회
- [ ] `POST /api/v1/admin/grades` - 등급 생성 (회장만)
- [ ] `PUT /api/v1/admin/grades/{id}` - 등급 수정
- [ ] `DELETE /api/v1/admin/grades/{id}` - 등급 삭제 (시스템 등급 제외)
- [ ] `PUT /api/v1/admin/users/{userId}/grade` - 사용자 등급 변경

**AdminDirectorPartController 엔드포인트**:
- [ ] `GET /api/v1/admin/director-parts` - 이사 파트 목록
- [ ] `POST /api/v1/admin/director-parts` - 이사 파트 생성 (회장만)
- [ ] `PUT /api/v1/admin/director-parts/{id}` - 이사 파트 수정
- [ ] `DELETE /api/v1/admin/director-parts/{id}` - 이사 파트 삭제
- [ ] `PUT /api/v1/admin/users/{userId}/director-part` - 이사 파트 지정

---

### 3.2 NaverCafe Module (네이버 카페 모듈)

#### 3.2.1 Domain & Services
| 항목 | 파일 경로 | 상태 | 우선순위 |
|------|----------|------|----------|
| NaverCafePost | `navercafe/domain/NaverCafePost.java` | [ ] | P2 |
| CrossPostingService | `navercafe/service/CrossPostingService.java` | [ ] | P2 |
| NaverCafeWebhookController | `navercafe/webhook/NaverCafeWebhookController.java` | [ ] | P2 |

---

## Phase 4: Real-time & Polish

### 4.1 Chat Module (채팅 모듈)

#### 4.1.1 Domain & Proto
| 항목 | 파일 경로 | 상태 | 우선순위 |
|------|----------|------|----------|
| ChatRoom 채팅방 | `chat/domain/ChatRoom.java` | [ ] | P1 |
| ChatMessage 메시지 | `chat/domain/ChatMessage.java` | [ ] | P1 |
| ChatParticipant 참여자 | `chat/domain/ChatParticipant.java` | [ ] | P1 |
| chat.proto gRPC 정의 | `proto/chat.proto` | [ ] | P1 |

#### 4.1.2 gRPC Services
| 항목 | 파일 경로 | 상태 | 우선순위 |
|------|----------|------|----------|
| ChatGrpcService | `chat/grpc/ChatGrpcService.java` | [ ] | P1 |
| ChatRoomService | `chat/service/ChatRoomService.java` | [ ] | P1 |
| ChatMessageService | `chat/service/ChatMessageService.java` | [ ] | P1 |

---

### 4.2 Testing & Quality

#### 4.2.1 Unit Tests
| 모듈 | 테스트 파일 | 상태 | 우선순위 |
|------|------------|------|----------|
| User Module | `user/service/UserServiceTest.java` | [ ] | P0 |
| User Module | `user/service/UserGradeServiceTest.java` | [ ] | P0 |
| User Module | `user/service/OAuth2UserServiceTest.java` | [ ] | P0 |
| Membership Module | `membership/service/MembershipApplicationServiceTest.java` | [ ] | P0 |
| Membership Module | `membership/service/AnnualFeeServiceTest.java` | [ ] | P0 |
| Membership Module | `membership/service/VehicleManagementServiceTest.java` | [ ] | P0 |
| Community Module | `community/service/BoardPermissionCheckerTest.java` | [ ] | P0 |
| Community Module | `community/service/PermissionGroupServiceTest.java` | [ ] | P0 |

#### 4.2.2 Integration Tests
| 모듈 | 테스트 파일 | 상태 | 우선순위 |
|------|------------|------|----------|
| User Module | `user/UserModuleIntegrationTest.java` | [ ] | P1 |
| Membership Module | `membership/MembershipModuleIntegrationTest.java` | [ ] | P1 |
| Community Module | `community/CommunityModuleIntegrationTest.java` | [ ] | P1 |
| Modulith Test | `MckApiServerApplicationTests.java` | [ ] | P1 |

#### 4.2.3 Spring Modulith Tests
| 항목 | 파일 경로 | 상태 | 우선순위 |
|------|----------|------|----------|
| 모듈 구조 검증 | `ModularityTests.java` | [ ] | P1 |
| 이벤트 흐름 테스트 | `ModuleEventFlowTest.java` | [ ] | P1 |

---

## Database Migration

### Flyway Migration Files
| 버전 | 파일명 | 내용 | 상태 |
|------|--------|------|------|
| V1 | `V1__create_user_module_tables.sql` | users, user_grades, oauth_accounts, passkey_credentials | [ ] |
| V2 | `V2__create_membership_module_tables.sql` | membership_applications, application_documents, ocr_results, payment_records, membership_periods, director_parts, member_vehicles, annual_fee_configs | [ ] |
| V3 | `V3__create_community_module_tables.sql` | boards, posts, comments, attachments, permission_groups, board_permission_mappings, user_permission_groups | [ ] |
| V4 | `V4__create_landing_module_tables.sql` | club_histories, executives, events, event_participants, instagram_posts | [ ] |
| V5 | `V5__create_admin_module_tables.sql` | audit_logs | [ ] |
| V6 | `V6__create_chat_module_tables.sql` | chat_rooms, chat_messages, chat_participants | [ ] |
| V7 | `V7__create_navercafe_module_tables.sql` | naver_cafe_posts | [ ] |
| V8 | `V8__seed_initial_data.sql` | 초기 등급, 권한 그룹, 게시판 데이터 | [ ] |
| V9 | `V9__create_legacy_members_table.sql` | 기존 회원 마이그레이션용 임시 테이블 | [ ] |

---

## External Integration

### 외부 서비스 연동
| 서비스 | 구현 항목 | 상태 | 우선순위 |
|--------|----------|------|----------|
| Google OAuth2 | `OAuth2UserService` | [ ] | P0 |
| Apple OAuth2 | `OAuth2UserService` | [ ] | P0 |
| Naver OAuth2 | `OAuth2UserService` | [ ] | P0 |
| DigitalOcean Spaces | `FileUploadService` (S3 호환) | [ ] | P0 |
| OCR (PaddleOCR/Tesseract) | `PaddleOcrService` | [ ] | P1 |
| 금융결제원 오픈뱅킹 | `OpenBankingService` | [ ] | P2 |
| Instagram Webhook | `InstagramWebhookController` | [ ] | P2 |
| Naver Cafe Webhook | `NaverCafeWebhookController` | [ ] | P2 |
| FCM/APNs 푸시 | `PushNotificationService` | [ ] | P2 |
| SMS (문자) | `SmsNotificationService` | [ ] | P2 |

---

## Configuration Files

### application.yml 설정
| 설정 항목 | 상태 | 우선순위 |
|----------|------|----------|
| 데이터베이스 연결 (PostgreSQL) | [ ] | P0 |
| JPA/Hibernate 설정 | [ ] | P0 |
| OAuth2 클라이언트 설정 (Google, Apple, Naver) | [ ] | P0 |
| JWT 설정 (secret, expiration) | [ ] | P0 |
| DigitalOcean Spaces 설정 | [ ] | P0 |
| gRPC 서버 설정 | [ ] | P1 |
| Flyway 마이그레이션 설정 | [ ] | P0 |
| Spring Modulith 설정 | [ ] | P1 |
| 로깅 설정 | [ ] | P1 |
| 프로파일별 설정 분리 (dev, prod) | [ ] | P1 |

---

## 우선순위 범례

| 우선순위 | 설명 |
|---------|------|
| **P0** | 필수 - 서비스 구동에 반드시 필요 |
| **P1** | 중요 - 핵심 기능 동작에 필요 |
| **P2** | 부가 - 향후 추가 가능 |

---

## 진행 상태 표기

- `[ ]` : 미완료
- `[x]` : 완료
- `[~]` : 진행 중
- `[-]` : 스킵/해당 없음

---

## 변경 이력

| 날짜 | 변경 내용 |
|------|----------|
| 2025-01-01 | 초기 TODO 문서 생성 |
