# BMW M Club Korea API Server - API Specification
# BMW M Club Korea API 서버 - API 명세서

## 1. Overview / 개요

- **Base URL**: `https://api.m-club.kr` (Production) / `http://localhost:8080` (Development)
- **API Version**: v1
- **Authentication**: JWT Bearer Token
- **Content-Type**: `application/json`

### 1.1 Common Response Format / 공통 응답 형식

```json
{
  "success": true,
  "data": { ... },
  "message": "Success",
  "timestamp": "2025-12-30T12:00:00Z"
}
```

### 1.2 Error Response Format / 에러 응답 형식

```json
{
  "success": false,
  "error": {
    "code": "USER_NOT_FOUND",
    "message": "사용자를 찾을 수 없습니다.",
    "details": { ... }
  },
  "timestamp": "2025-12-30T12:00:00Z"
}
```

### 1.3 Pagination Response / 페이지네이션 응답

```json
{
  "success": true,
  "data": {
    "content": [ ... ],
    "page": 0,
    "size": 20,
    "totalElements": 100,
    "totalPages": 5,
    "first": true,
    "last": false
  }
}
```

---

## 2. Authentication APIs / 인증 API

### 2.1 OAuth2 Login / OAuth2 로그인

#### GET /api/v1/auth/oauth2/{provider}
OAuth2 로그인 시작 (리다이렉트)

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| provider | path | Yes | OAuth 제공자: `google`, `apple`, `naver` |

**Response**: Redirect to OAuth provider

---

#### GET /api/v1/auth/oauth2/{provider}/callback
OAuth2 콜백 처리

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| provider | path | Yes | OAuth 제공자 |
| code | query | Yes | Authorization code |
| state | query | No | State parameter |

**Response 200**:
```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "expiresIn": 3600,
    "tokenType": "Bearer",
    "isNewUser": true,
    "requiresUsername": true
  }
}
```

---

#### POST /api/v1/auth/refresh
토큰 갱신

**Request Body**:
```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Response 200**:
```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "expiresIn": 3600
  }
}
```

---

#### POST /api/v1/auth/logout
로그아웃

**Headers**: `Authorization: Bearer {token}`

**Response 200**:
```json
{
  "success": true,
  "message": "로그아웃되었습니다."
}
```

---

### 2.2 Passkey (WebAuthn) APIs / 패스키 인증 API

#### POST /api/v1/auth/passkey/register/options
Passkey 등록 옵션 요청 (등록 시작)

**Headers**: `Authorization: Bearer {token}` (이미 로그인한 사용자)

**Response 200**:
```json
{
  "success": true,
  "data": {
    "challenge": "SGVsbG8gV29ybGQh...",
    "rp": {
      "id": "m-club.kr",
      "name": "BMW M Club Korea"
    },
    "user": {
      "id": "dXNlcl8xMjM0NTY=",
      "name": "bmw_lover",
      "displayName": "BMW 마니아"
    },
    "pubKeyCredParams": [
      { "type": "public-key", "alg": -7 },
      { "type": "public-key", "alg": -257 }
    ],
    "timeout": 60000,
    "attestation": "none",
    "authenticatorSelection": {
      "authenticatorAttachment": "platform",
      "residentKey": "preferred",
      "userVerification": "required"
    }
  }
}
```

---

#### POST /api/v1/auth/passkey/register/verify
Passkey 등록 검증 (등록 완료)

**Headers**: `Authorization: Bearer {token}`

**Request Body**:
```json
{
  "id": "credential-id-base64url",
  "rawId": "credential-raw-id-base64url",
  "type": "public-key",
  "response": {
    "clientDataJSON": "eyJ0eXBlIjoid2ViYXV0aG4uY3...",
    "attestationObject": "o2NmbXRkbm9uZWdhdHRTdG10...",
    "transports": ["internal", "hybrid"]
  },
  "deviceName": "iPhone 15 Pro"
}
```

**Response 200**:
```json
{
  "success": true,
  "data": {
    "credentialId": 1,
    "deviceName": "iPhone 15 Pro",
    "registeredAt": "2025-12-30T12:00:00Z"
  },
  "message": "Passkey가 성공적으로 등록되었습니다."
}
```

---

#### POST /api/v1/auth/passkey/login/options
Passkey 로그인 옵션 요청 (로그인 시작)

**Request Body** (선택적):
```json
{
  "username": "bmw_lover"
}
```

**Response 200**:
```json
{
  "success": true,
  "data": {
    "challenge": "dGVzdCBjaGFsbGVuZ2U=",
    "timeout": 60000,
    "rpId": "m-club.kr",
    "userVerification": "required",
    "allowCredentials": [
      {
        "type": "public-key",
        "id": "credential-id-base64url",
        "transports": ["internal", "hybrid"]
      }
    ]
  }
}
```

---

#### POST /api/v1/auth/passkey/login/verify
Passkey 로그인 검증 (로그인 완료)

**Request Body**:
```json
{
  "id": "credential-id-base64url",
  "rawId": "credential-raw-id-base64url",
  "type": "public-key",
  "response": {
    "clientDataJSON": "eyJ0eXBlIjoid2ViYXV0aG4uZ2V0...",
    "authenticatorData": "SZYN5YgOjGh0NBcPZHZgW4...",
    "signature": "MEUCIQC8Ns..."
  }
}
```

**Response 200**:
```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "expiresIn": 3600,
    "tokenType": "Bearer"
  }
}
```

**Error 401**: 인증 실패
```json
{
  "success": false,
  "error": {
    "code": "PASSKEY_VERIFICATION_FAILED",
    "message": "Passkey 인증에 실패했습니다."
  }
}
```

---

#### GET /api/v1/users/me/passkeys
내 Passkey 목록 조회

**Headers**: `Authorization: Bearer {token}`

**Response 200**:
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "deviceName": "iPhone 15 Pro",
      "lastUsedAt": "2025-12-30T10:00:00Z",
      "registeredAt": "2025-12-01T00:00:00Z"
    },
    {
      "id": 2,
      "deviceName": "MacBook Pro",
      "lastUsedAt": "2025-12-29T15:00:00Z",
      "registeredAt": "2025-12-15T00:00:00Z"
    }
  ]
}
```

---

#### DELETE /api/v1/users/me/passkeys/{credentialId}
Passkey 삭제

**Headers**: `Authorization: Bearer {token}`

**Response 200**:
```json
{
  "success": true,
  "message": "Passkey가 삭제되었습니다."
}
```

**Error 400**: 마지막 인증 수단 삭제 불가
```json
{
  "success": false,
  "error": {
    "code": "CANNOT_DELETE_LAST_CREDENTIAL",
    "message": "최소 하나의 인증 수단이 필요합니다."
  }
}
```

---

## 3. User APIs / 사용자 API

### 3.1 User Profile / 사용자 프로필

#### GET /api/v1/users/me
내 프로필 조회

**Headers**: `Authorization: Bearer {token}`

**Response 200**:
```json
{
  "success": true,
  "data": {
    "id": 1,
    "username": "bmw_lover",
    "email": "user@example.com",
    "nickname": "BMW 마니아",
    "profileImageUrl": "https://cdn.m-club.kr/profiles/user1.jpg",
    "grade": "MEMBER",
    "phoneNumber": "010-1234-5678",
    "carModel": "M3 Competition",
    "carYear": "2024",
    "createdAt": "2025-01-01T00:00:00Z",
    "lastLoginAt": "2025-12-30T10:00:00Z"
  }
}
```

---

#### PUT /api/v1/users/me
내 프로필 수정

**Headers**: `Authorization: Bearer {token}`

**Request Body**:
```json
{
  "nickname": "BMW 마니아",
  "phoneNumber": "010-1234-5678",
  "carModel": "M3 Competition",
  "carYear": "2024"
}
```

**Response 200**:
```json
{
  "success": true,
  "data": {
    "id": 1,
    "username": "bmw_lover",
    "nickname": "BMW 마니아",
    ...
  }
}
```

---

#### PUT /api/v1/users/me/username
사용자명 설정 (최초 1회 필수)

**Headers**: `Authorization: Bearer {token}`

**Request Body**:
```json
{
  "username": "bmw_lover"
}
```

**Response 200**:
```json
{
  "success": true,
  "data": {
    "username": "bmw_lover"
  }
}
```

**Error 400**: 이미 사용 중인 사용자명
```json
{
  "success": false,
  "error": {
    "code": "USERNAME_ALREADY_EXISTS",
    "message": "이미 사용 중인 사용자명입니다."
  }
}
```

---

#### GET /api/v1/users/{userId}
특정 사용자 공개 프로필 조회

**Response 200**:
```json
{
  "success": true,
  "data": {
    "id": 1,
    "username": "bmw_lover",
    "nickname": "BMW 마니아",
    "profileImageUrl": "https://cdn.m-club.kr/profiles/user1.jpg",
    "grade": "MEMBER",
    "carModel": "M3 Competition"
  }
}
```

---

#### GET /api/v1/users/me/posts
내가 작성한 게시글 목록

**Headers**: `Authorization: Bearer {token}`

**Query Parameters**:
| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| page | int | 0 | 페이지 번호 |
| size | int | 20 | 페이지 크기 |

**Response 200**: Paginated post list

---

#### GET /api/v1/users/me/comments
내가 작성한 댓글 목록

**Headers**: `Authorization: Bearer {token}`

---

#### GET /api/v1/users/me/bookmarks
내 북마크 목록

**Headers**: `Authorization: Bearer {token}`

---

## 4. Membership APIs / 정회원 가입 API (🆕)

### 4.1 Membership Application / 정회원 신청

#### POST /api/v1/membership/apply
정회원 신청서 제출

**Headers**: `Authorization: Bearer {token}`

**Request Body**:
```json
{
  "realName": "홍길동",
  "phoneNumber": "010-1234-5678",
  "vehicleOwnershipType": "PERSONAL",
  "vehicles": [
    {
      "carNumber": "12가3456",
      "vinNumber": "WBSWD93527P123456",
      "carModel": "M3"
    }
  ]
}
```

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| realName | string | Yes | 실명 |
| phoneNumber | string | Yes | 전화번호 |
| vehicleOwnershipType | string | Yes | 차량 소유 유형: `PERSONAL`, `CORPORATE`, `LEASE`, `RENTAL`, `CORPORATE_LEASE`, `CORPORATE_RENTAL` |
| vehicles | array | Yes | 차량 목록 (최소 1대) |
| vehicles[].carNumber | string | Yes | 차량번호 |
| vehicles[].vinNumber | string | Yes | 차대번호 (VIN) |
| vehicles[].carModel | string | Yes | 차종 (예: M3, M4, M5) |

**Response 201**:
```json
{
  "success": true,
  "data": {
    "applicationId": 12345,
    "status": "DOCUMENT_PENDING",
    "requiredDocuments": ["VEHICLE_REGISTRATION", "ID_CARD"],
    "message": "신청서가 접수되었습니다. 서류를 업로드해주세요."
  }
}
```

---

#### GET /api/v1/membership/status
내 정회원 신청 상태 조회

**Headers**: `Authorization: Bearer {token}`

**Response 200**:
```json
{
  "success": true,
  "data": {
    "applicationId": 12345,
    "status": "DOCUMENT_REVIEWING",
    "statusDescription": "서류 심사 중",
    "submittedAt": "2025-12-25T10:00:00Z",
    "documents": [
      {
        "type": "VEHICLE_REGISTRATION",
        "status": "VERIFIED",
        "uploadedAt": "2025-12-25T10:30:00Z"
      },
      {
        "type": "ID_CARD",
        "status": "PENDING",
        "uploadedAt": "2025-12-25T10:35:00Z"
      }
    ],
    "ocrResults": {
      "vehicleNumber": "12가3456",
      "ownerName": "홍길동",
      "matchStatus": "MATCHED"
    }
  }
}
```

---

### 4.2 Document Upload / 서류 업로드

#### POST /api/v1/membership/documents
정회원 신청 서류 업로드

**Headers**:
- `Authorization: Bearer {token}`
- `Content-Type: multipart/form-data`

**Request**:
| Field | Type | Required | Description |
|-------|------|----------|-------------|
| applicationId | number | Yes | 신청서 ID |
| documentType | string | Yes | 서류 유형: `VEHICLE_REGISTRATION`, `ID_CARD`, `BUSINESS_LICENSE`, `EMPLOYMENT_CERTIFICATE`, `LEASE_CONTRACT`, `RENTAL_CONTRACT` |
| file | file | Yes | 서류 이미지 (JPG, PNG, PDF) |

**Response 200**:
```json
{
  "success": true,
  "data": {
    "documentId": 67890,
    "documentType": "VEHICLE_REGISTRATION",
    "fileUrl": "https://cdn.m-club.kr/documents/xxx.jpg",
    "ocrStatus": "PROCESSING",
    "message": "서류가 업로드되었습니다. OCR 처리 중입니다."
  }
}
```

---

#### GET /api/v1/membership/documents/{applicationId}
신청 서류 목록 조회

**Headers**: `Authorization: Bearer {token}`

**Response 200**:
```json
{
  "success": true,
  "data": {
    "applicationId": 12345,
    "documents": [
      {
        "id": 67890,
        "type": "VEHICLE_REGISTRATION",
        "typeName": "차량등록증",
        "fileUrl": "https://cdn.m-club.kr/documents/xxx.jpg",
        "status": "VERIFIED",
        "ocrResult": {
          "vehicleNumber": "12가3456",
          "vinNumber": "WBSWD93527P123456",
          "ownerName": "홍길동"
        },
        "uploadedAt": "2025-12-25T10:30:00Z"
      }
    ]
  }
}
```

---

### 4.3 Payment / 결제 및 입금

#### GET /api/v1/membership/payment-info
입금 안내 정보 조회 (서류 승인 후)

**Headers**: `Authorization: Bearer {token}`

**Response 200**:
```json
{
  "success": true,
  "data": {
    "applicationId": 12345,
    "paymentType": "ENROLLMENT_AND_ANNUAL",
    "enrollmentFee": 200000,
    "annualFee": 200000,
    "totalAmount": 400000,
    "bankAccount": {
      "bankName": "신한은행",
      "accountNumber": "110-xxx-xxxxxx",
      "accountHolder": "BMW M클럽코리아"
    },
    "depositorName": "610홍길동",
    "dueDate": "2025-12-31",
    "message": "입금자명을 '610홍길동'으로 입력해주세요."
  }
}
```

---

#### POST /api/v1/membership/payment/notify
입금 완료 알림 (사용자가 입금 후 알림)

**Headers**: `Authorization: Bearer {token}`

**Request Body**:
```json
{
  "applicationId": 12345,
  "depositDate": "2025-12-26",
  "depositorName": "610홍길동",
  "amount": 400000
}
```

**Response 200**:
```json
{
  "success": true,
  "data": {
    "message": "입금 알림이 접수되었습니다. 확인 후 처리됩니다."
  }
}
```

---

### 4.4 Membership Period / 멤버십 기간

#### GET /api/v1/membership/period
내 멤버십 기간 조회 (정회원만)

**Headers**: `Authorization: Bearer {token}`

**Response 200**:
```json
{
  "success": true,
  "data": {
    "memberNumber": 610,
    "status": "ACTIVE",
    "currentPeriod": {
      "year": 2025,
      "startDate": "2025-01-01",
      "endDate": "2025-12-31",
      "paidAt": "2025-01-05T10:00:00Z"
    },
    "exemptionType": "NONE",
    "renewalRequired": false,
    "daysUntilExpiry": 180
  }
}
```

---

#### POST /api/v1/membership/renew
연회비 갱신 신청

**Headers**: `Authorization: Bearer {token}`

**Response 200**:
```json
{
  "success": true,
  "data": {
    "renewalId": 99999,
    "targetYear": 2026,
    "amount": 200000,
    "bankAccount": {
      "bankName": "신한은행",
      "accountNumber": "110-xxx-xxxxxx",
      "accountHolder": "BMW M클럽코리아"
    },
    "depositorName": "610홍길동",
    "dueDate": "2026-01-31"
  }
}
```

---

### 4.5 Vehicle Management / 차량 관리

#### GET /api/v1/membership/vehicles
내 등록 차량 목록

**Headers**: `Authorization: Bearer {token}`

**Response 200**:
```json
{
  "success": true,
  "data": {
    "vehicles": [
      {
        "id": 1001,
        "carNumber": "12가3456",
        "vinNumber": "WBSWD93527P123456",
        "carModel": "M3",
        "ownershipType": "PERSONAL",
        "status": "ACTIVE",
        "isPrimary": true,
        "registeredAt": "2025-01-10T10:00:00Z"
      },
      {
        "id": 1002,
        "carNumber": "78나9012",
        "vinNumber": "WBSWF93527P789012",
        "carModel": "M5",
        "ownershipType": "PERSONAL",
        "status": "ACTIVE",
        "isPrimary": false,
        "registeredAt": "2025-06-15T10:00:00Z"
      }
    ]
  }
}
```

---

#### POST /api/v1/membership/vehicles
신규 차량 추가 등록

**Headers**: `Authorization: Bearer {token}`

**Request Body**:
```json
{
  "carNumber": "34다5678",
  "vinNumber": "WBSWD93527P345678",
  "carModel": "M4",
  "ownershipType": "PERSONAL"
}
```

**Response 201**:
```json
{
  "success": true,
  "data": {
    "vehicleId": 1003,
    "status": "DOCUMENT_PENDING",
    "requiredDocuments": ["VEHICLE_REGISTRATION"],
    "message": "차량이 등록 신청되었습니다. 서류를 업로드해주세요."
  }
}
```

---

#### DELETE /api/v1/membership/vehicles/{vehicleId}
차량 매각/폐차 처리

**Headers**: `Authorization: Bearer {token}`

**Request Body**:
```json
{
  "reason": "SOLD",
  "soldDate": "2025-12-20"
}
```

**Response 200**:
```json
{
  "success": true,
  "data": {
    "message": "차량이 매각 처리되었습니다.",
    "gracePeriodEndDate": "2026-12-20",
    "remainingActiveVehicles": 1
  }
}
```

---

#### PUT /api/v1/membership/vehicles/{vehicleId}/primary
대표 차량 변경

**Headers**: `Authorization: Bearer {token}`

**Response 200**:
```json
{
  "success": true,
  "data": {
    "message": "대표 차량이 변경되었습니다."
  }
}
```

---

### 4.6 Admin Membership Management / 관리자 정회원 관리

#### GET /api/v1/admin/membership/applications
정회원 신청 목록 조회 (임원만)

**Headers**: `Authorization: Bearer {token}`

**Query Parameters**:
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| status | string | No | 필터: `DOCUMENT_PENDING`, `DOCUMENT_REVIEWING`, `APPROVED`, `REJECTED`, `PAYMENT_PENDING`, `COMPLETED` |
| page | number | No | 페이지 번호 (기본: 0) |
| size | number | No | 페이지 크기 (기본: 20) |

**Response 200**:
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 12345,
        "applicantName": "홍길동",
        "phoneNumber": "010-1234-5678",
        "vehicleInfo": "M3 (12가3456)",
        "status": "DOCUMENT_REVIEWING",
        "submittedAt": "2025-12-25T10:00:00Z",
        "documentCount": 2
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 15
  }
}
```

---

#### GET /api/v1/admin/membership/applications/{id}
정회원 신청 상세 + OCR 결과 조회 (임원만)

**Headers**: `Authorization: Bearer {token}`

**Response 200**:
```json
{
  "success": true,
  "data": {
    "id": 12345,
    "applicant": {
      "userId": 67890,
      "realName": "홍길동",
      "phoneNumber": "010-1234-5678",
      "email": "hong@example.com"
    },
    "vehicleOwnershipType": "PERSONAL",
    "vehicles": [
      {
        "carNumber": "12가3456",
        "vinNumber": "WBSWD93527P123456",
        "carModel": "M3"
      }
    ],
    "documents": [
      {
        "id": 111,
        "type": "VEHICLE_REGISTRATION",
        "fileUrl": "https://cdn.m-club.kr/documents/xxx.jpg",
        "ocrResult": {
          "vehicleNumber": "12가3456",
          "vinNumber": "WBSWD93527P123456",
          "ownerName": "홍길동",
          "confidence": 0.95
        },
        "verificationStatus": "MATCHED"
      }
    ],
    "status": "DOCUMENT_REVIEWING",
    "submittedAt": "2025-12-25T10:00:00Z"
  }
}
```

---

#### POST /api/v1/admin/membership/applications/{id}/approve
서류 승인 (임원만)

**Headers**: `Authorization: Bearer {token}`

**Request Body**:
```json
{
  "assignedMemberNumber": 611,
  "notes": "서류 확인 완료"
}
```

**Response 200**:
```json
{
  "success": true,
  "data": {
    "message": "서류가 승인되었습니다. 입금 안내가 발송됩니다.",
    "assignedMemberNumber": 611
  }
}
```

---

#### POST /api/v1/admin/membership/applications/{id}/reject
서류 반려 (임원만)

**Headers**: `Authorization: Bearer {token}`

**Request Body**:
```json
{
  "reason": "차량등록증의 소유자명과 신분증 이름이 일치하지 않습니다.",
  "resubmissionAllowed": true
}
```

**Response 200**:
```json
{
  "success": true,
  "data": {
    "message": "서류가 반려되었습니다."
  }
}
```

---

#### GET /api/v1/admin/payments/pending
입금 대기 목록 (임원만)

**Headers**: `Authorization: Bearer {token}`

**Response 200**:
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "paymentId": 9999,
        "applicationId": 12345,
        "userId": 67890,
        "userName": "홍길동",
        "expectedDepositorName": "611홍길동",
        "paymentType": "ENROLLMENT_AND_ANNUAL",
        "amount": 400000,
        "dueDate": "2025-12-31",
        "userNotifiedAt": "2025-12-26T10:00:00Z"
      }
    ]
  }
}
```

---

#### POST /api/v1/admin/payments/{id}/confirm
입금 확인 (임원만)

**Headers**: `Authorization: Bearer {token}`

**Request Body**:
```json
{
  "confirmedAmount": 400000,
  "depositDate": "2025-12-26",
  "bankTransactionId": "TXN123456789"
}
```

**Response 200**:
```json
{
  "success": true,
  "data": {
    "message": "입금이 확인되었습니다. 정회원으로 승급됩니다.",
    "memberNumber": 611,
    "membershipStartDate": "2025-12-26",
    "membershipEndDate": "2025-12-31"
  }
}
```

---

### 4.7 Annual Fee Configuration / 연회비 설정 API (🆕)

#### GET /api/v1/admin/annual-fee/config/{year}
특정 년도 연회비 설정 조회 (임원만)

**Headers**: `Authorization: Bearer {token}`

**Response 200**:
```json
{
  "success": true,
  "data": {
    "targetYear": 2025,
    "carryOverDeadline": "2025-01-15",
    "renewalStartDate": "2025-01-01",
    "renewalDeadline": "2025-01-31",
    "enrollmentFeeAmount": 200000,
    "annualFeeAmount": 200000,
    "configuredBy": {
      "id": 100,
      "name": "김회장"
    },
    "configuredAt": "2024-12-20T10:00:00Z",
    "notes": "설 연휴 고려하여 마감일 연장"
  }
}
```

---

#### POST /api/v1/admin/annual-fee/config
연회비 설정 생성/수정 (임원만)

**Headers**: `Authorization: Bearer {token}`

**Request Body**:
```json
{
  "targetYear": 2026,
  "carryOverDeadline": "2026-01-15",
  "renewalStartDate": "2026-01-01",
  "renewalDeadline": "2026-01-31",
  "enrollmentFeeAmount": 200000,
  "annualFeeAmount": 200000,
  "notes": "2026년 연회비 설정"
}
```

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| targetYear | number | Yes | 대상 년도 |
| carryOverDeadline | date | Yes | 이월 마감일 (이 날짜까지 가입 시 전년도 연회비로 처리) |
| renewalStartDate | date | Yes | 갱신 시작일 |
| renewalDeadline | date | Yes | 갱신 마감일 |
| enrollmentFeeAmount | number | No | 입회비 (기본: 200000) |
| annualFeeAmount | number | No | 연회비 (기본: 200000) |
| notes | string | No | 비고 |

**Response 201**:
```json
{
  "success": true,
  "data": {
    "id": 5,
    "targetYear": 2026,
    "message": "2026년 연회비 설정이 저장되었습니다."
  }
}
```

---

### 4.8 User Grade Management / 등급 관리 API (🆕)

#### GET /api/v1/admin/grades
등급 목록 조회 (임원만)

**Headers**: `Authorization: Bearer {token}`

**Response 200**:
```json
{
  "success": true,
  "data": {
    "grades": [
      {
        "id": 1,
        "code": "DEVELOPER",
        "name": "개발자",
        "roleName": "ROLE_DEVELOPER",
        "permissionLevel": 10,
        "isExecutive": false,
        "isStaff": true,
        "isMember": false,
        "requiresAnnualFee": false,
        "isSystemGrade": true,
        "displaySuffix": null,
        "displayOrder": 1,
        "isActive": true
      },
      {
        "id": 6,
        "code": "REGULAR",
        "name": "정회원",
        "roleName": "ROLE_REGULAR",
        "permissionLevel": 5,
        "isExecutive": false,
        "isStaff": false,
        "isMember": true,
        "requiresAnnualFee": true,
        "isSystemGrade": false,
        "displaySuffix": null,
        "displayOrder": 6,
        "isActive": true
      }
    ]
  }
}
```

---

#### POST /api/v1/admin/grades
새 등급 생성 (회장만)

**Headers**: `Authorization: Bearer {token}`

**Request Body**:
```json
{
  "code": "HONORARY",
  "name": "명예정회원",
  "permissionLevel": 6,
  "isExecutive": false,
  "isStaff": true,
  "isMember": true,
  "requiresAnnualFee": false,
  "displaySuffix": "(명예)",
  "displayOrder": 5
}
```

**Response 201**:
```json
{
  "success": true,
  "data": {
    "id": 9,
    "code": "HONORARY",
    "name": "명예정회원",
    "roleName": "ROLE_HONORARY",
    "message": "새 등급이 생성되었습니다."
  }
}
```

---

#### PUT /api/v1/admin/grades/{gradeId}
등급 수정 (회장만, 시스템 등급 제외)

**Headers**: `Authorization: Bearer {token}`

**Request Body**:
```json
{
  "name": "명예정회원 (특별)",
  "displaySuffix": "(명예특별)",
  "displayOrder": 4
}
```

**Response 200**:
```json
{
  "success": true,
  "data": {
    "message": "등급이 수정되었습니다."
  }
}
```

---

#### DELETE /api/v1/admin/grades/{gradeId}
등급 삭제 (회장만, 시스템 등급 및 사용 중인 등급 제외)

**Headers**: `Authorization: Bearer {token}`

**Response 200**:
```json
{
  "success": true,
  "data": {
    "message": "등급이 삭제되었습니다."
  }
}
```

**Error 400** (시스템 등급 삭제 시도):
```json
{
  "success": false,
  "error": {
    "code": "SYSTEM_GRADE_DELETE_NOT_ALLOWED",
    "message": "시스템 등급은 삭제할 수 없습니다: DEVELOPER"
  }
}
```

**Error 400** (사용 중인 등급 삭제 시도):
```json
{
  "success": false,
  "error": {
    "code": "GRADE_IN_USE",
    "message": "해당 등급을 사용 중인 회원이 있어 삭제할 수 없습니다: 정회원 (150명)"
  }
}
```

---

### 4.9 Exemption Management / 면제 관리 API (🆕)

#### POST /api/v1/admin/users/{userId}/exemption
연회비 면제 부여 (회장만)

**Headers**: `Authorization: Bearer {token}`

**Request Body**:
```json
{
  "exemptionType": "PERMANENT",
  "reason": "역대 회장 (2020-2022)"
}
```

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| exemptionType | string | Yes | 면제 유형: `PERMANENT` (영구), `ONE_TIME` (1회성) |
| reason | string | Yes | 면제 사유 |
| exemptionYear | number | Conditional | 1회성 면제 시 적용 년도 (exemptionType=ONE_TIME 시 필수) |

**Response 200**:
```json
{
  "success": true,
  "data": {
    "userId": 67890,
    "exemptionType": "PERMANENT",
    "message": "영구 면제가 부여되었습니다."
  }
}
```

---

#### DELETE /api/v1/admin/users/{userId}/exemption
연회비 면제 해제 (회장만)

**Headers**: `Authorization: Bearer {token}`

**Response 200**:
```json
{
  "success": true,
  "data": {
    "message": "면제가 해제되었습니다."
  }
}
```

---

## 5. Landing APIs / 랜딩 페이지 API

### 5.1 Landing Page Data / 랜딩 페이지 데이터

#### GET /api/v1/landing
랜딩 페이지 전체 데이터

**Response 200**:
```json
{
  "success": true,
  "data": {
    "hero": {
      "title": "BMW M Club Korea",
      "subtitle": "대한민국 최고의 BMW M 동호회",
      "backgroundImageUrl": "https://cdn.m-club.kr/landing/hero.jpg"
    },
    "stats": {
      "memberCount": 1500,
      "eventCount": 120,
      "historyYears": 15
    },
    "upcomingEvents": [ ... ],
    "recentInstagramPosts": [ ... ]
  }
}
```

---

### 5.2 History / 클럽 역사

#### GET /api/v1/histories
클럽 역사 목록

**Response 200**:
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "year": 2010,
      "title": "BMW M Club Korea 창립",
      "description": "대한민국 최초의 BMW M 전문 동호회 설립",
      "imageUrl": "https://cdn.m-club.kr/history/2010.jpg"
    },
    ...
  ]
}
```

---

#### GET /api/v1/histories/{id}
특정 역사 상세

---

### 5.3 Executives / 임원진

#### GET /api/v1/executives
전체 임원진 목록

**Query Parameters**:
| Parameter | Type | Description |
|-----------|------|-------------|
| term | int | 특정 기수 필터 (optional) |
| current | boolean | 현재 임원만 (optional) |

**Response 200**:
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "termYear": 15,
      "position": "회장",
      "name": "홍길동",
      "profileImageUrl": "https://cdn.m-club.kr/executives/hong.jpg",
      "introduction": "15기 회장을 맡고 있는 홍길동입니다.",
      "isCurrent": true
    },
    ...
  ]
}
```

---

#### GET /api/v1/executives/current
현재 임원진

---

#### GET /api/v1/executives/terms/{termYear}
특정 기수 임원진

---

### 5.4 Events / 이벤트(행사)

#### GET /api/v1/events
이벤트 목록

**Query Parameters**:
| Parameter | Type | Description |
|-----------|------|-------------|
| status | string | 상태 필터: UPCOMING, ONGOING, COMPLETED |
| page | int | 페이지 번호 |
| size | int | 페이지 크기 |

**Response 200**:
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 1,
        "title": "2025 신년 정기모임",
        "description": "새해를 맞이하여...",
        "eventStartAt": "2025-01-15T14:00:00Z",
        "eventEndAt": "2025-01-15T18:00:00Z",
        "location": "서울 강남구 테헤란로 123",
        "locationMapUrl": "https://map.naver.com/...",
        "maxParticipants": 100,
        "currentParticipants": 45,
        "status": "UPCOMING",
        "coverImageUrl": "https://cdn.m-club.kr/events/2025-01.jpg"
      },
      ...
    ],
    "page": 0,
    "totalPages": 5
  }
}
```

---

#### GET /api/v1/events/{id}
이벤트 상세

---

#### POST /api/v1/events/{id}/participate
이벤트 참가 신청

**Headers**: `Authorization: Bearer {token}`

**Response 200**:
```json
{
  "success": true,
  "message": "참가 신청이 완료되었습니다."
}
```

**Error 400**: 정원 초과
```json
{
  "success": false,
  "error": {
    "code": "EVENT_FULL",
    "message": "이벤트 정원이 초과되었습니다."
  }
}
```

---

#### DELETE /api/v1/events/{id}/participate
이벤트 참가 취소

**Headers**: `Authorization: Bearer {token}`

---

### 5.5 Instagram / 인스타그램

#### GET /api/v1/instagram/posts
인스타그램 포스트 목록

**Query Parameters**:
| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| limit | int | 12 | 조회 개수 |

**Response 200**:
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "instagramPostId": "CxYz123ABC",
      "caption": "오늘의 드라이브 #BMWM3",
      "mediaUrl": "https://cdn.m-club.kr/instagram/CxYz123ABC.jpg",
      "mediaType": "IMAGE",
      "permalink": "https://www.instagram.com/p/CxYz123ABC/",
      "postedAt": "2025-12-25T10:00:00Z",
      "likeCount": 150,
      "commentCount": 12
    },
    ...
  ]
}
```

---

#### POST /api/v1/webhooks/instagram
Instagram Webhook 수신 (내부용)

---

## 6. Community APIs / 커뮤니티 API

### 6.1 Boards / 게시판

#### GET /api/v1/boards
게시판 목록

**Headers**: `Authorization: Bearer {token}` (선택)

**Response 200**:
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "slug": "notice",
      "name": "공지사항",
      "description": "클럽 공지사항",
      "boardType": "NOTICE",
      "requiredGradeToRead": "MEMBER",
      "requiredGradeToWrite": "EXECUTIVE",
      "allowComments": true,
      "allowAttachments": true
    },
    {
      "id": 2,
      "slug": "free",
      "name": "자유게시판",
      "description": "자유롭게 소통하는 공간",
      "boardType": "GENERAL",
      "requiredGradeToRead": "MEMBER",
      "requiredGradeToWrite": "MEMBER",
      "allowComments": true,
      "allowAttachments": true
    },
    ...
  ]
}
```

---

#### GET /api/v1/boards/{slug}
게시판 상세

---

### 6.2 Posts / 게시글

#### GET /api/v1/boards/{slug}/posts
게시판별 게시글 목록

**Headers**: `Authorization: Bearer {token}`

**Query Parameters**:
| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| page | int | 0 | 페이지 번호 |
| size | int | 20 | 페이지 크기 |
| sort | string | createdAt,desc | 정렬 기준 |
| search | string | | 검색어 (제목+내용) |

**Response 200**:
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 1,
        "boardSlug": "free",
        "title": "오늘 드라이브 후기",
        "author": {
          "id": 10,
          "username": "bmw_lover",
          "nickname": "BMW 마니아",
          "profileImageUrl": "..."
        },
        "viewCount": 150,
        "likeCount": 12,
        "commentCount": 5,
        "isPinned": false,
        "isNotice": false,
        "hasAttachments": true,
        "createdAt": "2025-12-29T10:00:00Z"
      },
      ...
    ],
    "page": 0,
    "totalElements": 100
  }
}
```

---

#### POST /api/v1/boards/{slug}/posts
게시글 작성

**Headers**: `Authorization: Bearer {token}`

**Request Body**:
```json
{
  "title": "오늘 드라이브 후기",
  "content": "<p>오늘 한강 드라이브를 다녀왔습니다...</p>",
  "isNotice": false,
  "attachmentIds": [1, 2, 3]
}
```

**Response 201**:
```json
{
  "success": true,
  "data": {
    "id": 123,
    "title": "오늘 드라이브 후기",
    ...
  }
}
```

---

#### GET /api/v1/posts/{postId}
게시글 상세

**Headers**: `Authorization: Bearer {token}`

**Response 200**:
```json
{
  "success": true,
  "data": {
    "id": 123,
    "boardSlug": "free",
    "title": "오늘 드라이브 후기",
    "content": "<p>오늘 한강 드라이브를 다녀왔습니다...</p>",
    "author": {
      "id": 10,
      "username": "bmw_lover",
      "nickname": "BMW 마니아"
    },
    "viewCount": 151,
    "likeCount": 12,
    "commentCount": 5,
    "isLiked": false,
    "isBookmarked": true,
    "attachments": [
      {
        "id": 1,
        "originalFileName": "drive.jpg",
        "fileUrl": "https://cdn.m-club.kr/attachments/...",
        "fileSize": 1024000,
        "contentType": "image/jpeg"
      }
    ],
    "createdAt": "2025-12-29T10:00:00Z",
    "updatedAt": "2025-12-29T10:00:00Z"
  }
}
```

---

#### PUT /api/v1/posts/{postId}
게시글 수정

**Headers**: `Authorization: Bearer {token}`

**Request Body**:
```json
{
  "title": "수정된 제목",
  "content": "<p>수정된 내용...</p>",
  "attachmentIds": [1, 2]
}
```

---

#### DELETE /api/v1/posts/{postId}
게시글 삭제

**Headers**: `Authorization: Bearer {token}`

---

### 6.3 Post Interactions / 게시글 상호작용

#### POST /api/v1/posts/{postId}/like
좋아요

**Headers**: `Authorization: Bearer {token}`

**Response 200**:
```json
{
  "success": true,
  "data": {
    "likeCount": 13,
    "isLiked": true
  }
}
```

---

#### DELETE /api/v1/posts/{postId}/like
좋아요 취소

---

#### POST /api/v1/posts/{postId}/bookmark
북마크 추가

---

#### DELETE /api/v1/posts/{postId}/bookmark
북마크 삭제

---

### 6.4 Comments / 댓글

#### GET /api/v1/posts/{postId}/comments
댓글 목록

**Headers**: `Authorization: Bearer {token}`

**Response 200**:
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "content": "좋은 글이네요!",
      "author": {
        "id": 5,
        "username": "m_power",
        "nickname": "M파워"
      },
      "likeCount": 3,
      "isLiked": false,
      "createdAt": "2025-12-29T11:00:00Z",
      "replies": [
        {
          "id": 2,
          "content": "감사합니다!",
          "author": { ... },
          "createdAt": "2025-12-29T11:30:00Z"
        }
      ]
    },
    ...
  ]
}
```

---

#### POST /api/v1/posts/{postId}/comments
댓글 작성

**Headers**: `Authorization: Bearer {token}`

**Request Body**:
```json
{
  "content": "좋은 글이네요!",
  "parentId": null
}
```

---

#### PUT /api/v1/comments/{commentId}
댓글 수정

---

#### DELETE /api/v1/comments/{commentId}
댓글 삭제

---

#### POST /api/v1/comments/{commentId}/like
댓글 좋아요

---

### 6.5 File Upload / 파일 업로드

#### POST /api/v1/files/upload
파일 업로드

**Headers**:
- `Authorization: Bearer {token}`
- `Content-Type: multipart/form-data`

**Request Body**: FormData with file

**Response 200**:
```json
{
  "success": true,
  "data": {
    "id": 1,
    "originalFileName": "photo.jpg",
    "storedFileName": "abc123-photo.jpg",
    "fileUrl": "https://cdn.m-club.kr/attachments/abc123-photo.jpg",
    "fileSize": 1024000,
    "contentType": "image/jpeg"
  }
}
```

---

#### DELETE /api/v1/files/{fileId}
파일 삭제

---

## 7. Admin APIs / 어드민 API

> **Note**: 모든 Admin API는 `ADMIN` 권한 필요

### 7.1 Member Management / 회원 관리

#### GET /api/v1/admin/members
회원 목록

**Query Parameters**:
| Parameter | Type | Description |
|-----------|------|-------------|
| search | string | 검색어 (username, email, nickname) |
| grade | string | 등급 필터 |
| isActive | boolean | 활성 상태 필터 |
| page | int | 페이지 번호 |
| size | int | 페이지 크기 |

---

#### GET /api/v1/admin/members/{userId}
회원 상세

---

#### PUT /api/v1/admin/members/{userId}/grade
회원 등급 변경

**Request Body**:
```json
{
  "grade": "EXECUTIVE",
  "reason": "임원진 선출"
}
```

---

#### PUT /api/v1/admin/members/{userId}/status
회원 상태 변경 (활성/비활성)

**Request Body**:
```json
{
  "isActive": false,
  "reason": "규정 위반"
}
```

---

### 7.2 Board Management / 게시판 관리

#### GET /api/v1/admin/boards
게시판 관리 목록

---

#### POST /api/v1/admin/boards
게시판 생성

**Request Body**:
```json
{
  "slug": "tech",
  "name": "기술게시판",
  "description": "차량 기술 정보 공유",
  "boardType": "GENERAL",
  "requiredGradeToRead": "MEMBER",
  "requiredGradeToWrite": "MEMBER",
  "allowComments": true,
  "allowAttachments": true,
  "displayOrder": 5
}
```

---

#### PUT /api/v1/admin/boards/{id}
게시판 수정

---

#### DELETE /api/v1/admin/boards/{id}
게시판 삭제 (Soft Delete)

---

#### PUT /api/v1/admin/boards/{id}/order
게시판 순서 변경

**Request Body**:
```json
{
  "displayOrder": 3
}
```

---

### 7.3 Content Management / 컨텐츠 관리

#### GET /api/v1/admin/posts
전체 게시글 관리

**Query Parameters**:
| Parameter | Type | Description |
|-----------|------|-------------|
| boardId | long | 게시판 필터 |
| search | string | 검색어 |
| isDeleted | boolean | 삭제 상태 필터 |

---

#### DELETE /api/v1/admin/posts/{id}
게시글 강제 삭제

---

#### PUT /api/v1/admin/posts/{id}/pin
게시글 고정/해제

**Request Body**:
```json
{
  "isPinned": true
}
```

---

### 7.4 Dashboard / 대시보드

#### GET /api/v1/admin/dashboard
대시보드 전체 데이터

**Response 200**:
```json
{
  "success": true,
  "data": {
    "overview": {
      "totalUsers": 1500,
      "activeUsers": 1200,
      "totalPosts": 5000,
      "totalComments": 25000
    },
    "todayStats": {
      "newUsers": 5,
      "newPosts": 20,
      "newComments": 100
    },
    "weeklyTrend": [ ... ],
    "recentActivity": [ ... ]
  }
}
```

---

#### GET /api/v1/admin/dashboard/stats
통계 데이터

**Query Parameters**:
| Parameter | Type | Description |
|-----------|------|-------------|
| startDate | date | 시작일 |
| endDate | date | 종료일 |
| metricType | string | 메트릭 유형 |

---

#### GET /api/v1/admin/dashboard/recent-activity
최근 활동

---

### 7.5 Audit Logs / 감사 로그

#### GET /api/v1/admin/audit-logs
관리자 활동 로그

**Query Parameters**:
| Parameter | Type | Description |
|-----------|------|-------------|
| adminId | long | 관리자 ID 필터 |
| actionType | string | 작업 유형 필터 |
| startDate | date | 시작일 |
| endDate | date | 종료일 |

---

### 7.6 Permission Group Management / 권한 그룹 관리 (🆕)

#### GET /api/v1/admin/permission-groups
권한 그룹 목록 조회 (임원만)

**Headers**: `Authorization: Bearer {token}`

**Response 200**:
```json
{
  "success": true,
  "data": {
    "groups": [
      {
        "id": 1,
        "name": "운영진",
        "description": "회장, 부회장, 이사, 고문 기본 그룹",
        "permissions": ["READ", "WRITE", "MOVE", "COMMENT", "DELETE", "HARD_DELETE", "SHARE"],
        "isDefault": true,
        "memberCount": 15,
        "createdAt": "2025-01-01T00:00:00Z"
      },
      {
        "id": 2,
        "name": "정회원 기본",
        "description": "정회원 기본 권한",
        "permissions": ["READ", "WRITE", "COMMENT", "DELETE", "SHARE"],
        "isDefault": true,
        "memberCount": 600,
        "createdAt": "2025-01-01T00:00:00Z"
      },
      {
        "id": 5,
        "name": "VIP 회원",
        "description": "특별 권한 그룹",
        "permissions": ["READ", "WRITE", "COMMENT", "DELETE", "SHARE", "MOVE"],
        "isDefault": false,
        "memberCount": 10,
        "createdAt": "2025-06-15T10:00:00Z"
      }
    ]
  }
}
```

---

#### POST /api/v1/admin/permission-groups
권한 그룹 생성 (임원만)

**Headers**: `Authorization: Bearer {token}`

**Request Body**:
```json
{
  "name": "VIP 회원",
  "description": "특별 권한이 부여된 VIP 회원 그룹",
  "permissions": ["READ", "WRITE", "COMMENT", "DELETE", "SHARE", "MOVE"]
}
```

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| name | string | Yes | 그룹명 (중복 불가) |
| description | string | No | 그룹 설명 |
| permissions | array | Yes | 권한 목록: `READ`, `WRITE`, `MOVE`, `COMMENT`, `DELETE`, `HARD_DELETE`, `SHARE` |

**Response 201**:
```json
{
  "success": true,
  "data": {
    "id": 5,
    "name": "VIP 회원",
    "message": "권한 그룹이 생성되었습니다."
  }
}
```

---

#### PUT /api/v1/admin/permission-groups/{groupId}
권한 그룹 수정 (임원만)

**Headers**: `Authorization: Bearer {token}`

**Request Body**:
```json
{
  "name": "VIP 회원 (프리미엄)",
  "description": "프리미엄 VIP 그룹",
  "permissions": ["READ", "WRITE", "COMMENT", "DELETE", "SHARE", "MOVE", "HARD_DELETE"]
}
```

**Response 200**:
```json
{
  "success": true,
  "data": {
    "message": "권한 그룹이 수정되었습니다."
  }
}
```

---

#### DELETE /api/v1/admin/permission-groups/{groupId}
권한 그룹 삭제 (임원만, 기본 그룹 제외)

**Headers**: `Authorization: Bearer {token}`

**Response 200**:
```json
{
  "success": true,
  "data": {
    "message": "권한 그룹이 삭제되었습니다."
  }
}
```

**Error 400** (기본 그룹 삭제 시도):
```json
{
  "success": false,
  "error": {
    "code": "DEFAULT_GROUP_DELETE_NOT_ALLOWED",
    "message": "기본 권한 그룹은 삭제할 수 없습니다: 운영진"
  }
}
```

---

### 7.7 Board Permission Mapping / 게시판별 그룹 권한 관리 (🆕)

#### GET /api/v1/admin/boards/{boardId}/permissions
게시판별 권한 그룹 매핑 조회 (임원만)

**Headers**: `Authorization: Bearer {token}`

**Response 200**:
```json
{
  "success": true,
  "data": {
    "boardId": 10,
    "boardName": "자유게시판",
    "permissionMappings": [
      {
        "id": 101,
        "groupId": 1,
        "groupName": "운영진",
        "permissions": ["READ", "WRITE", "MOVE", "COMMENT", "DELETE", "HARD_DELETE", "SHARE"]
      },
      {
        "id": 102,
        "groupId": 2,
        "groupName": "정회원 기본",
        "permissions": ["READ", "WRITE", "COMMENT", "DELETE", "SHARE"]
      },
      {
        "id": 103,
        "groupId": 3,
        "groupName": "준회원 제한",
        "permissions": ["READ"]
      }
    ]
  }
}
```

---

#### POST /api/v1/admin/boards/{boardId}/permissions
게시판에 권한 그룹 매핑 추가 (임원만)

**Headers**: `Authorization: Bearer {token}`

**Request Body**:
```json
{
  "groupId": 5,
  "permissions": ["READ", "WRITE", "COMMENT"]
}
```

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| groupId | number | Yes | 권한 그룹 ID |
| permissions | array | Yes | 이 게시판에서 부여할 권한 목록 |

**Response 201**:
```json
{
  "success": true,
  "data": {
    "mappingId": 104,
    "message": "게시판 권한 매핑이 추가되었습니다."
  }
}
```

---

#### PUT /api/v1/admin/boards/{boardId}/permissions/{mappingId}
게시판 권한 매핑 수정 (임원만)

**Headers**: `Authorization: Bearer {token}`

**Request Body**:
```json
{
  "permissions": ["READ", "WRITE", "COMMENT", "DELETE"]
}
```

**Response 200**:
```json
{
  "success": true,
  "data": {
    "message": "게시판 권한 매핑이 수정되었습니다."
  }
}
```

---

#### DELETE /api/v1/admin/boards/{boardId}/permissions/{mappingId}
게시판 권한 매핑 삭제 (임원만)

**Headers**: `Authorization: Bearer {token}`

**Response 200**:
```json
{
  "success": true,
  "data": {
    "message": "게시판 권한 매핑이 삭제되었습니다."
  }
}
```

---

### 7.8 User Permission Group Assignment / 사용자별 권한 그룹 부여 (🆕)

#### GET /api/v1/admin/users/{userId}/permission-groups
사용자의 권한 그룹 목록 조회 (임원만)

**Headers**: `Authorization: Bearer {token}`

**Response 200**:
```json
{
  "success": true,
  "data": {
    "userId": 67890,
    "userName": "홍길동",
    "gradeCode": "REGULAR",
    "defaultGroup": {
      "id": 2,
      "name": "정회원 기본"
    },
    "additionalGroups": [
      {
        "assignmentId": 501,
        "groupId": 5,
        "groupName": "VIP 회원",
        "assignedBy": {
          "id": 100,
          "name": "김회장"
        },
        "assignedAt": "2025-06-15T10:00:00Z",
        "reason": "10년 장기회원 VIP 지정",
        "expiresAt": null
      }
    ]
  }
}
```

---

#### POST /api/v1/admin/users/{userId}/permission-groups
사용자에게 권한 그룹 부여 (임원만)

**Headers**: `Authorization: Bearer {token}`

**Request Body**:
```json
{
  "groupId": 5,
  "reason": "10년 장기회원 VIP 지정",
  "expiresAt": null
}
```

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| groupId | number | Yes | 부여할 권한 그룹 ID |
| reason | string | No | 부여 사유 |
| expiresAt | date | No | 만료일 (NULL이면 무기한) |

**Response 201**:
```json
{
  "success": true,
  "data": {
    "assignmentId": 502,
    "message": "권한 그룹이 부여되었습니다."
  }
}
```

---

#### DELETE /api/v1/admin/users/{userId}/permission-groups/{assignmentId}
사용자의 권한 그룹 해제 (임원만)

**Headers**: `Authorization: Bearer {token}`

**Response 200**:
```json
{
  "success": true,
  "data": {
    "message": "권한 그룹이 해제되었습니다."
  }
}
```

---

## 8. Chat APIs / 채팅 API

### 8.1 REST APIs / REST API

#### GET /api/v1/chat/rooms
내 채팅방 목록

**Headers**: `Authorization: Bearer {token}`

**Response 200**:
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "name": "2025 신년모임 채팅방",
      "roomType": "EVENT",
      "eventId": 10,
      "participantCount": 45,
      "lastMessage": {
        "content": "다들 모이셨나요?",
        "senderUsername": "bmw_lover",
        "sentAt": "2025-12-30T10:00:00Z"
      },
      "unreadCount": 5,
      "lastMessageAt": "2025-12-30T10:00:00Z"
    },
    ...
  ]
}
```

---

#### POST /api/v1/chat/rooms
채팅방 생성

**Headers**: `Authorization: Bearer {token}`

**Request Body**:
```json
{
  "roomType": "DIRECT",
  "participantIds": [5],
  "name": null
}
```

또는 그룹 채팅:
```json
{
  "roomType": "GROUP",
  "participantIds": [5, 10, 15],
  "name": "드라이브 친구들"
}
```

---

#### GET /api/v1/chat/rooms/{roomId}
채팅방 정보

---

#### DELETE /api/v1/chat/rooms/{roomId}
채팅방 나가기

---

#### GET /api/v1/chat/rooms/{roomId}/messages
메시지 히스토리 조회

**Query Parameters**:
| Parameter | Type | Description |
|-----------|------|-------------|
| before | long | 이 메시지 ID 이전 메시지 조회 |
| limit | int | 조회 개수 (default: 50) |

**Response 200**:
```json
{
  "success": true,
  "data": {
    "messages": [
      {
        "id": 100,
        "senderId": 5,
        "senderUsername": "bmw_lover",
        "content": "안녕하세요!",
        "messageType": "TEXT",
        "attachmentUrl": null,
        "createdAt": "2025-12-30T10:00:00Z"
      },
      ...
    ],
    "hasMore": true
  }
}
```

---

#### GET /api/v1/chat/rooms/{roomId}/participants
참여자 목록

---

### 8.2 gRPC Services / gRPC 서비스

**Server Address**: `grpc://api.m-club.kr:9090`

| Service | Method | Type | Description |
|---------|--------|------|-------------|
| ChatService | Connect | Bidirectional | 실시간 양방향 통신 |
| ChatService | SubscribeRoom | Server Streaming | 채팅방 구독 |
| ChatService | SendMessage | Unary | 메시지 전송 |

---

## 9. NaverCafe APIs / 네이버 카페 API

### 9.1 Cafe Posts / 카페 글

#### GET /api/v1/navercafe/posts
동기화된 카페 글 목록

**Headers**: `Authorization: Bearer {token}`

**Query Parameters**:
| Parameter | Type | Description |
|-----------|------|-------------|
| page | int | 페이지 번호 |
| size | int | 페이지 크기 |

---

#### GET /api/v1/navercafe/posts/{id}
카페 글 상세

---

#### POST /api/v1/navercafe/posts/{id}/crosspost
커뮤니티에 가져오기

**Headers**: `Authorization: Bearer {token}` (EXECUTIVE 이상)

**Request Body**:
```json
{
  "targetBoardSlug": "news"
}
```

---

### 9.2 Sync Management (Admin) / 동기화 관리

#### POST /api/v1/admin/navercafe/sync
수동 동기화 실행

**Headers**: `Authorization: Bearer {token}` (ADMIN)

---

#### GET /api/v1/admin/navercafe/sync-logs
동기화 로그 조회

---

### 9.3 Webhook / 웹훅

#### POST /api/v1/webhooks/navercafe
Naver Cafe Webhook 수신 (내부용)

---

## 10. Error Codes / 에러 코드

### 10.1 Common Errors / 공통 에러

| Code | HTTP Status | Description |
|------|-------------|-------------|
| UNAUTHORIZED | 401 | 인증 필요 |
| FORBIDDEN | 403 | 권한 없음 |
| NOT_FOUND | 404 | 리소스 없음 |
| VALIDATION_ERROR | 400 | 유효성 검증 실패 |
| INTERNAL_ERROR | 500 | 서버 오류 |

### 10.2 User Errors / 사용자 에러

| Code | Description |
|------|-------------|
| USER_NOT_FOUND | 사용자를 찾을 수 없음 |
| USERNAME_ALREADY_EXISTS | 이미 사용 중인 사용자명 |
| USERNAME_REQUIRED | 사용자명 설정 필요 |
| INVALID_OAUTH_TOKEN | 유효하지 않은 OAuth 토큰 |

### 10.3 Community Errors / 커뮤니티 에러

| Code | Description |
|------|-------------|
| BOARD_NOT_FOUND | 게시판을 찾을 수 없음 |
| POST_NOT_FOUND | 게시글을 찾을 수 없음 |
| COMMENT_NOT_FOUND | 댓글을 찾을 수 없음 |
| BOARD_ACCESS_DENIED | 게시판 접근 권한 없음 |
| NOT_POST_AUTHOR | 게시글 작성자가 아님 |

### 10.4 Event Errors / 이벤트 에러

| Code | Description |
|------|-------------|
| EVENT_NOT_FOUND | 이벤트를 찾을 수 없음 |
| EVENT_FULL | 이벤트 정원 초과 |
| EVENT_ALREADY_PARTICIPATED | 이미 참가 신청됨 |
| EVENT_NOT_PARTICIPATED | 참가 신청하지 않음 |

### 10.5 Chat Errors / 채팅 에러

| Code | Description |
|------|-------------|
| CHAT_ROOM_NOT_FOUND | 채팅방을 찾을 수 없음 |
| NOT_ROOM_PARTICIPANT | 채팅방 참여자가 아님 |
| CANNOT_CREATE_DIRECT_ROOM | 1:1 채팅방 생성 불가 |

---

## Document History / 문서 이력

| Version | Date | Author | Description |
|---------|------|--------|-------------|
| 1.0 | 2025-12-30 | Claude | Initial API specification |
