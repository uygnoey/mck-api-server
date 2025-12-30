# BMW M Club Korea API Server - Database Schema
# BMW M Club Korea API 서버 - 데이터베이스 스키마

## 1. Overview / 개요

- **Database**: PostgreSQL 16+
- **Migration**: Flyway
- **Total Tables**: 35개 (🆕 업데이트됨)

### 1.1 Table Summary / 테이블 요약

| Module | Tables | Description |
|--------|--------|-------------|
| User | 5 | 🆕 등급 (동적 관리), 사용자, OAuth 계정, Passkey, 차량 |
| Membership | 7 | 🆕 정회원 신청, 서류, OCR, 결제, 기간, 이사파트, 연회비설정 |
| Landing | 4 | 랜딩 페이지, 역사, 임원진, 이벤트, 인스타그램 |
| Community | 10 | 🆕 게시판, 게시글, 댓글, 첨부파일, 권한그룹 시스템 |
| Admin | 2 | 관리자 활동 로그, 대시보드 메트릭 |
| Chat | 3 | 채팅방, 메시지, 참여자 |
| NaverCafe | 2 | 카페 글, 동기화 로그 |
| Notification | 4 | 🆕 알림 설정, 알림 로그, 푸시 토큰 |
| Migration | 1 | 🆕 기존 회원 마이그레이션 |

---

## 2. User Module Tables / 사용자 모듈 테이블 (🆕 전면 개편)

### 2.1 user_grades (사용자 등급) - 동적 관리 (🆕)

```sql
-- 등급은 Enum이 아닌 DB 테이블로 관리 (임원진이 동적으로 추가/제거 가능)
CREATE TABLE user_grades (
    id BIGSERIAL PRIMARY KEY,

    -- 등급 정보
    code VARCHAR(30) NOT NULL UNIQUE,                -- 등급 코드 (예: DEVELOPER, ADVISOR, REGULAR)
    name VARCHAR(50) NOT NULL,                       -- 등급명 (예: 개발자, 고문, 정회원)
    role_name VARCHAR(50) NOT NULL UNIQUE,           -- Spring Security Role (예: ROLE_DEVELOPER)

    -- 권한 레벨 (높을수록 상위 등급)
    permission_level INTEGER NOT NULL,               -- 권한 레벨 (예: 10, 9, 8, ...)

    -- 특성 플래그
    is_executive BOOLEAN NOT NULL DEFAULT FALSE,     -- 임원 여부 (PRESIDENT, VICE_PRESIDENT, DIRECTOR)
    is_staff BOOLEAN NOT NULL DEFAULT FALSE,         -- 운영진 여부 (임원 + 고문)
    is_member BOOLEAN NOT NULL DEFAULT FALSE,        -- 정/준회원 여부
    requires_annual_fee BOOLEAN NOT NULL DEFAULT TRUE,  -- 연회비 필요 여부
    is_system_grade BOOLEAN NOT NULL DEFAULT FALSE,  -- 시스템 등급 (삭제 불가: DEVELOPER, ASSOCIATE)

    -- 표시 설정
    display_suffix VARCHAR(20),                      -- 표시 접미사 (예: "(고문)", "(회장)")
    display_order INTEGER NOT NULL,                  -- 표시 순서

    -- 관리
    created_by BIGINT,                               -- 생성자 ID (NULL이면 시스템)
    is_active BOOLEAN NOT NULL DEFAULT TRUE,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes
CREATE INDEX idx_user_grades_code ON user_grades(code);
CREATE INDEX idx_user_grades_level ON user_grades(permission_level DESC);
CREATE INDEX idx_user_grades_active ON user_grades(is_active);

-- 기본 등급 데이터 삽입
INSERT INTO user_grades (code, name, role_name, permission_level, is_executive, is_staff, is_member, requires_annual_fee, is_system_grade, display_suffix, display_order) VALUES
    ('DEVELOPER', '개발자', 'ROLE_DEVELOPER', 10, FALSE, TRUE, FALSE, FALSE, TRUE, NULL, 1),
    ('ADVISOR', '고문', 'ROLE_ADVISOR', 9, FALSE, TRUE, FALSE, FALSE, FALSE, '(고문)', 2),
    ('PRESIDENT', '회장', 'ROLE_PRESIDENT', 8, TRUE, TRUE, FALSE, FALSE, FALSE, '(회장)', 3),
    ('VICE_PRESIDENT', '부회장', 'ROLE_VICE_PRESIDENT', 7, TRUE, TRUE, FALSE, FALSE, FALSE, '(부회장)', 4),
    ('DIRECTOR', '이사', 'ROLE_DIRECTOR', 6, TRUE, TRUE, FALSE, FALSE, FALSE, NULL, 5),  -- 이사 접미사는 파트명 사용
    ('REGULAR', '정회원', 'ROLE_REGULAR', 5, FALSE, FALSE, TRUE, TRUE, FALSE, NULL, 6),
    ('ASSOCIATE', '준회원', 'ROLE_ASSOCIATE', 3, FALSE, FALSE, TRUE, FALSE, TRUE, '(준회원)', 7),
    ('PARTNER', '파트너사', 'ROLE_PARTNER', 2, FALSE, FALSE, FALSE, FALSE, FALSE, '(파트너)', 8);

-- Comments
COMMENT ON TABLE user_grades IS '사용자 등급 (동적 관리 가능)';
COMMENT ON COLUMN user_grades.code IS '등급 코드: DEVELOPER, ADVISOR, PRESIDENT, VICE_PRESIDENT, DIRECTOR, REGULAR, ASSOCIATE, PARTNER 등';
COMMENT ON COLUMN user_grades.permission_level IS '권한 레벨 (높을수록 상위)';
COMMENT ON COLUMN user_grades.is_system_grade IS '시스템 등급 여부 (TRUE면 삭제 불가)';
COMMENT ON COLUMN user_grades.requires_annual_fee IS '연회비 필요 여부 (REGULAR만 TRUE)';
```

### 2.2 users (사용자)

```sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,

    -- 정회원 관련 필드 (불변)
    member_number INTEGER UNIQUE,                    -- 정회원 번호 (영구 소유, NULL이면 신규 준회원)
    real_name VARCHAR(50) NOT NULL,                  -- 실명 (모든 회원 필수)

    -- 연락처 정보
    email VARCHAR(255) NOT NULL,
    phone_number VARCHAR(20),
    profile_image_url VARCHAR(500),

    -- 등급 관련 (DB 테이블 참조)
    grade_id BIGINT NOT NULL REFERENCES user_grades(id),  -- 🆕 등급 테이블 참조
    associate_status VARCHAR(20),                    -- 준회원 상태: PENDING, REVIEWING, EXPIRED, REJECTED
    director_part_id BIGINT,                         -- 이사인 경우 담당 파트 ID
    partner_company_name VARCHAR(100),               -- 파트너사 업체명

    -- 연회비 면제 관련
    exemption_type VARCHAR(20) NOT NULL DEFAULT 'NONE',  -- NONE, PERMANENT, ONE_TIME
    exemption_reason VARCHAR(200),                   -- 면제 사유 (고문, 명예정회원, 특별공로 등)
    exemption_year INTEGER,                          -- 1회성 면제 적용 년도 (예: 2025)

    -- 탈퇴 관련
    is_withdrawn BOOLEAN NOT NULL DEFAULT FALSE,
    withdrawn_at TIMESTAMP,
    withdrawal_reason VARCHAR(500),

    -- 기타
    last_login_at TIMESTAMP,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Sequences (정회원 번호 발급용)
CREATE SEQUENCE member_number_seq START WITH 650;    -- 기존 610번대 이후부터 시작 (마이그레이션 후 조정)

-- Indexes
CREATE INDEX idx_users_member_number ON users(member_number);
CREATE INDEX idx_users_real_name ON users(real_name);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_grade_id ON users(grade_id);
CREATE INDEX idx_users_associate_status ON users(associate_status);
CREATE INDEX idx_users_exemption_type ON users(exemption_type);
CREATE INDEX idx_users_is_withdrawn ON users(is_withdrawn);
CREATE INDEX idx_users_is_active ON users(is_active);

-- Comments
COMMENT ON TABLE users IS '사용자 정보 테이블';
COMMENT ON COLUMN users.member_number IS '정회원 번호 (영구 소유, 탈퇴 후에도 유지)';
COMMENT ON COLUMN users.real_name IS '실명 (본명, 모든 회원 필수)';
COMMENT ON COLUMN users.grade_id IS '등급 ID (user_grades 테이블 참조)';
COMMENT ON COLUMN users.associate_status IS '준회원 상태: PENDING(신규), REVIEWING(심사중), EXPIRED(만료), REJECTED(반려)';
COMMENT ON COLUMN users.exemption_type IS '면제 유형: NONE(없음), PERMANENT(영구), ONE_TIME(1회성)';
COMMENT ON COLUMN users.exemption_year IS '1회성 면제 적용 년도';
```

### 2.3 oauth_accounts (OAuth 계정) - 다중 연결 지원

```sql
CREATE TABLE oauth_accounts (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    provider VARCHAR(20) NOT NULL,                   -- GOOGLE, APPLE, NAVER
    provider_id VARCHAR(255) NOT NULL,               -- OAuth 제공자의 사용자 ID
    email VARCHAR(255),                              -- OAuth 제공자에서 받은 이메일
    access_token TEXT,                               -- 암호화된 액세스 토큰
    refresh_token TEXT,                              -- 암호화된 리프레시 토큰
    token_expires_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_oauth_accounts UNIQUE (provider, provider_id)
);

-- Indexes
CREATE INDEX idx_oauth_accounts_user ON oauth_accounts(user_id);
CREATE INDEX idx_oauth_accounts_provider ON oauth_accounts(provider, provider_id);

-- Comments
COMMENT ON TABLE oauth_accounts IS '사용자별 OAuth 연결 계정 (다중 연결 가능)';
COMMENT ON COLUMN oauth_accounts.provider IS 'OAuth 제공자: GOOGLE, APPLE, NAVER';
```

### 2.4 passkey_credentials (Passkey 인증)

```sql
CREATE TABLE passkey_credentials (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    credential_id VARCHAR(500) NOT NULL UNIQUE,      -- Base64 인코딩된 Credential ID
    public_key TEXT NOT NULL,                        -- 공개키 (COSE 형식, Base64)
    sign_counter BIGINT NOT NULL DEFAULT 0,          -- 서명 카운터 (재생 공격 방지)
    transports VARCHAR(255),                         -- 지원 전송: usb, nfc, ble, internal 등
    device_name VARCHAR(100),                        -- 디바이스 이름 (예: "iPhone 15 Pro")
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    last_used_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes
CREATE INDEX idx_passkey_credentials_user ON passkey_credentials(user_id);
CREATE INDEX idx_passkey_credentials_credential ON passkey_credentials(credential_id);
CREATE INDEX idx_passkey_credentials_active ON passkey_credentials(user_id, is_active);

-- Comments
COMMENT ON TABLE passkey_credentials IS 'WebAuthn Passkey 인증 정보';
COMMENT ON COLUMN passkey_credentials.credential_id IS 'Passkey Credential ID (고유)';
COMMENT ON COLUMN passkey_credentials.sign_counter IS '서명 카운터 (재생 공격 방지)';
```

### 2.5 member_vehicles (회원 차량) - 다중 차량 지원 (🆕)

```sql
CREATE TABLE member_vehicles (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    car_number VARCHAR(20) NOT NULL,                 -- 차량번호
    vin_number VARCHAR(50) NOT NULL UNIQUE,          -- 차대번호 (중복 불가)
    car_model VARCHAR(100) NOT NULL,                 -- 차종 (예: M3, M4, M5 등)
    ownership_type VARCHAR(30) NOT NULL,             -- 소유 유형: PERSONAL, CORPORATE, LEASE, RENTAL, CORPORATE_LEASE, CORPORATE_RENTAL
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',    -- ACTIVE, SOLD, GRACE_PERIOD
    registered_at DATE NOT NULL DEFAULT CURRENT_DATE,
    sold_at DATE,                                    -- 매각일 (SOLD 시)
    grace_period_end_at DATE,                        -- 유예 종료일 (M차량 없을 때)
    is_primary BOOLEAN NOT NULL DEFAULT FALSE,       -- 대표 차량 여부
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes
CREATE INDEX idx_member_vehicles_user ON member_vehicles(user_id);
CREATE INDEX idx_member_vehicles_vin ON member_vehicles(vin_number);
CREATE INDEX idx_member_vehicles_status ON member_vehicles(status);
CREATE INDEX idx_member_vehicles_primary ON member_vehicles(user_id, is_primary);
CREATE INDEX idx_member_vehicles_grace ON member_vehicles(status, grace_period_end_at)
    WHERE status = 'GRACE_PERIOD';

-- Comments
COMMENT ON TABLE member_vehicles IS '회원 차량 정보 (다중 차량 등록 가능)';
COMMENT ON COLUMN member_vehicles.vin_number IS '차대번호 (중복 불가, 이중 등록 방지)';
COMMENT ON COLUMN member_vehicles.status IS '상태: ACTIVE(현재 소유), SOLD(매각), GRACE_PERIOD(유예기간)';
COMMENT ON COLUMN member_vehicles.grace_period_end_at IS 'M차량 없을 때 1년 유예 종료일';
```

---

## 3. Membership Module Tables / 멤버십 모듈 테이블 (🆕)

### 3.1 membership_applications (정회원 신청서)

```sql
CREATE TABLE membership_applications (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,

    -- 신청 정보
    application_number VARCHAR(20) NOT NULL UNIQUE,      -- 신청번호 (예: APP-2025-0001)
    status VARCHAR(30) NOT NULL DEFAULT 'DOCUMENT_PENDING',  -- 신청 상태

    -- 차량 소유 유형
    vehicle_ownership_type VARCHAR(30) NOT NULL,         -- PERSONAL, CORPORATE, LEASE, RENTAL, CORPORATE_LEASE, CORPORATE_RENTAL

    -- 신청자 정보 (신청 당시 스냅샷)
    applicant_name VARCHAR(50) NOT NULL,
    applicant_phone VARCHAR(20) NOT NULL,
    applicant_email VARCHAR(255),

    -- 차량 정보 (최초 등록 차량)
    car_number VARCHAR(20) NOT NULL,
    vin_number VARCHAR(50) NOT NULL,
    car_model VARCHAR(100) NOT NULL,

    -- 처리 정보
    reviewed_by BIGINT,                                  -- 검토한 관리자 ID
    reviewed_at TIMESTAMP,
    rejection_reason VARCHAR(500),                       -- 반려 사유

    -- 결제 정보
    payment_amount DECIMAL(10,2),                        -- 결제 금액 (입회비 + 연회비)
    target_year INTEGER,                                 -- 연회비 대상 년도 (이월 정책 적용 후)

    -- 완료 정보
    approved_at TIMESTAMP,
    assigned_member_number INTEGER,                      -- 부여된 정회원 번호

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes
CREATE INDEX idx_membership_applications_user ON membership_applications(user_id);
CREATE INDEX idx_membership_applications_status ON membership_applications(status);
CREATE INDEX idx_membership_applications_number ON membership_applications(application_number);
CREATE INDEX idx_membership_applications_created ON membership_applications(created_at DESC);
CREATE INDEX idx_membership_applications_target_year ON membership_applications(target_year);

-- Comments
COMMENT ON TABLE membership_applications IS '정회원 신청서';
COMMENT ON COLUMN membership_applications.status IS '상태: DOCUMENT_PENDING, DOCUMENT_SUBMITTED, UNDER_REVIEW, DOCUMENT_APPROVED, DOCUMENT_REJECTED, PAYMENT_PENDING, PAYMENT_CONFIRMED, COMPLETED, CANCELLED';
COMMENT ON COLUMN membership_applications.vehicle_ownership_type IS '차량 소유 유형: PERSONAL(개인), CORPORATE(법인), LEASE(리스), RENTAL(렌트), CORPORATE_LEASE(법인리스), CORPORATE_RENTAL(법인렌트)';
COMMENT ON COLUMN membership_applications.target_year IS '연회비 대상 년도 (이월 기간 정책 적용 후 결정)';
```

### 3.2 application_documents (제출 서류)

```sql
CREATE TABLE application_documents (
    id BIGSERIAL PRIMARY KEY,
    application_id BIGINT NOT NULL REFERENCES membership_applications(id) ON DELETE CASCADE,

    -- 서류 정보
    document_type VARCHAR(30) NOT NULL,                  -- 서류 유형
    file_url VARCHAR(500) NOT NULL,                      -- S3/Spaces 저장 URL
    original_file_name VARCHAR(255) NOT NULL,
    file_size BIGINT NOT NULL,
    content_type VARCHAR(100) NOT NULL,

    -- 검증 상태
    verification_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    verified_at TIMESTAMP,
    verified_by BIGINT,                                  -- 검증한 관리자 ID
    rejection_reason VARCHAR(500),

    -- OCR 연결
    ocr_result_id BIGINT,                                -- OCR 결과 ID

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes
CREATE INDEX idx_application_documents_application ON application_documents(application_id);
CREATE INDEX idx_application_documents_type ON application_documents(document_type);
CREATE INDEX idx_application_documents_status ON application_documents(verification_status);

-- Comments
COMMENT ON TABLE application_documents IS '정회원 신청 제출 서류';
COMMENT ON COLUMN application_documents.document_type IS '서류 유형: VEHICLE_REGISTRATION(차량등록증), ID_CARD(신분증), BUSINESS_LICENSE(사업자등록증), EMPLOYMENT_CERTIFICATE(재직증명서), LEASE_CONTRACT(리스계약서), RENTAL_CONTRACT(렌트계약서)';
COMMENT ON COLUMN application_documents.verification_status IS '검증 상태: PENDING, VERIFIED, REJECTED';
```

### 3.3 ocr_results (OCR 추출 결과)

```sql
CREATE TABLE ocr_results (
    id BIGSERIAL PRIMARY KEY,
    document_id BIGINT NOT NULL REFERENCES application_documents(id) ON DELETE CASCADE,

    -- OCR 메타데이터
    ocr_provider VARCHAR(30) NOT NULL,                   -- PADDLE_OCR, TESSERACT, NAVER_CLOVA
    ocr_version VARCHAR(20),
    processing_time_ms INTEGER,
    confidence_score DECIMAL(5,4),                       -- 0.0000 ~ 1.0000

    -- 추출된 데이터 (JSON)
    extracted_data JSONB NOT NULL,                       -- 서류별 추출 결과
    raw_text TEXT,                                       -- 원본 추출 텍스트

    -- 대조 결과
    match_result JSONB,                                  -- 신청 정보와 대조 결과
    is_matched BOOLEAN,                                  -- 전체 대조 성공 여부
    mismatch_fields TEXT[],                              -- 불일치 필드 목록

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes
CREATE INDEX idx_ocr_results_document ON ocr_results(document_id);
CREATE INDEX idx_ocr_results_matched ON ocr_results(is_matched);
CREATE INDEX idx_ocr_results_provider ON ocr_results(ocr_provider);

-- Comments
COMMENT ON TABLE ocr_results IS 'OCR 추출 결과';
COMMENT ON COLUMN ocr_results.extracted_data IS 'JSON 형식의 추출 결과 (서류별 다름): 차량등록증 {owner_name, car_number, vin_number}, 신분증 {name, is_masked}, 사업자등록증 {company_name, representative_name}';
COMMENT ON COLUMN ocr_results.match_result IS 'JSON 형식의 대조 결과: {field_name: {expected, actual, matched}}';
```

### 3.4 payment_records (결제 기록)

```sql
CREATE TABLE payment_records (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    application_id BIGINT REFERENCES membership_applications(id),

    -- 결제 유형
    payment_type VARCHAR(20) NOT NULL,                   -- ENROLLMENT_FEE, ANNUAL_FEE
    target_year INTEGER NOT NULL,                        -- 연회비 대상 년도

    -- 금액
    amount DECIMAL(10,2) NOT NULL,

    -- 입금 정보
    depositor_name VARCHAR(50) NOT NULL,                 -- 입금자명
    deposit_date DATE NOT NULL,                          -- 입금일

    -- 상태
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',       -- PENDING, CONFIRMED, CANCELLED, REFUNDED

    -- 확인 정보
    confirmed_by BIGINT,                                 -- 확인한 관리자 ID (NULL이면 자동확인)
    confirmed_at TIMESTAMP,
    auto_confirmed BOOLEAN NOT NULL DEFAULT FALSE,       -- 오픈뱅킹 자동 확인 여부

    -- 오픈뱅킹 연동
    bank_transaction_id VARCHAR(100),                    -- 은행 거래 ID
    bank_account_number VARCHAR(50),                     -- 입금 계좌번호 (마스킹)

    -- 취소/환불 정보
    cancelled_at TIMESTAMP,
    cancelled_by BIGINT,
    cancellation_reason VARCHAR(500),
    refunded_at TIMESTAMP,
    refund_amount DECIMAL(10,2),

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes
CREATE INDEX idx_payment_records_user ON payment_records(user_id);
CREATE INDEX idx_payment_records_application ON payment_records(application_id);
CREATE INDEX idx_payment_records_type ON payment_records(payment_type);
CREATE INDEX idx_payment_records_status ON payment_records(status);
CREATE INDEX idx_payment_records_target_year ON payment_records(target_year);
CREATE INDEX idx_payment_records_deposit_date ON payment_records(deposit_date);

-- Composite Index (연도별 결제 조회용)
CREATE INDEX idx_payment_records_user_year ON payment_records(user_id, target_year);

-- Comments
COMMENT ON TABLE payment_records IS '결제(입금) 기록';
COMMENT ON COLUMN payment_records.payment_type IS '결제 유형: ENROLLMENT_FEE(입회비 20만원), ANNUAL_FEE(연회비 20만원)';
COMMENT ON COLUMN payment_records.target_year IS '연회비 대상 년도 (이월 정책 적용 후 결정)';
COMMENT ON COLUMN payment_records.auto_confirmed IS '금융 오픈API 통한 자동 확인 여부';
```

### 3.5 membership_periods (멤버십 기간)

```sql
CREATE TABLE membership_periods (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,

    -- 기간 정보
    start_year INTEGER NOT NULL,                         -- 시작 년도
    end_year INTEGER NOT NULL,                           -- 종료 년도 (해당 년도 12월 31일까지 유효)

    -- 상태
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',        -- ACTIVE, EXPIRED, CANCELLED

    -- 갱신 정보
    is_renewed BOOLEAN NOT NULL DEFAULT FALSE,           -- 갱신 여부
    renewed_at TIMESTAMP,
    renewal_payment_id BIGINT,                           -- 갱신 결제 ID

    -- 만료 처리
    expired_at TIMESTAMP,
    expiration_notified_at TIMESTAMP,                    -- 만료 알림 발송 시각

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes
CREATE INDEX idx_membership_periods_user ON membership_periods(user_id);
CREATE INDEX idx_membership_periods_status ON membership_periods(status);
CREATE INDEX idx_membership_periods_end_year ON membership_periods(end_year);

-- Composite Index (활성 멤버십 조회용)
CREATE INDEX idx_membership_periods_active ON membership_periods(user_id, status)
    WHERE status = 'ACTIVE';

-- Comments
COMMENT ON TABLE membership_periods IS '멤버십(정회원) 유효 기간';
COMMENT ON COLUMN membership_periods.end_year IS '종료 년도 (해당 년도 12월 31일까지 유효)';
COMMENT ON COLUMN membership_periods.status IS '상태: ACTIVE(활성), EXPIRED(만료), CANCELLED(취소)';
```

### 3.6 director_parts (이사 파트)

```sql
CREATE TABLE director_parts (
    id BIGSERIAL PRIMARY KEY,

    -- 파트 정보
    name VARCHAR(50) NOT NULL UNIQUE,                    -- 파트명 (예: 행사, 홍보, 총무)
    description VARCHAR(200),

    -- 권한 설정
    can_manage_members BOOLEAN NOT NULL DEFAULT FALSE,   -- 회원 관리 권한
    can_manage_posts BOOLEAN NOT NULL DEFAULT TRUE,      -- 게시글 관리 권한
    can_manage_events BOOLEAN NOT NULL DEFAULT FALSE,    -- 이벤트 관리 권한
    can_assign_sub_permissions BOOLEAN NOT NULL DEFAULT FALSE,  -- 세부 권한 지정 가능
    custom_permissions JSONB,                            -- 추가 커스텀 권한 (JSON)

    -- 관리
    created_by BIGINT NOT NULL,                          -- 생성자 (회장 ID)
    is_active BOOLEAN NOT NULL DEFAULT TRUE,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes
CREATE INDEX idx_director_parts_name ON director_parts(name);
CREATE INDEX idx_director_parts_active ON director_parts(is_active);

-- Comments
COMMENT ON TABLE director_parts IS '이사 담당 파트 (회장이 동적으로 생성/삭제)';
COMMENT ON COLUMN director_parts.name IS '파트명: 행사, 홍보, 총무, 미디어 등';
COMMENT ON COLUMN director_parts.custom_permissions IS 'JSON 형식의 커스텀 권한 목록';
```

### 3.7 annual_fee_configs (연회비 설정) (🆕)

```sql
CREATE TABLE annual_fee_configs (
    id BIGSERIAL PRIMARY KEY,

    -- 대상 년도
    target_year INTEGER NOT NULL UNIQUE,                 -- 대상 년도 (예: 2025)

    -- 이월 정책 (🆕 핵심 기능)
    carry_over_deadline DATE NOT NULL,                   -- 이월 마감일 (예: 2025-01-15)

    -- 갱신 기간
    renewal_start_date DATE NOT NULL,                    -- 갱신 시작일 (예: 2025-01-01)
    renewal_deadline DATE NOT NULL,                      -- 갱신 마감일 (예: 2025-01-31)

    -- 금액
    enrollment_fee_amount DECIMAL(10,2) NOT NULL DEFAULT 200000,  -- 입회비 (기본 20만원)
    annual_fee_amount DECIMAL(10,2) NOT NULL DEFAULT 200000,      -- 연회비 (기본 20만원)

    -- 설정 관리
    configured_by BIGINT NOT NULL,                       -- 설정한 임원 ID
    configured_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    notes VARCHAR(500),                                  -- 비고 (예: "설 연휴로 마감일 연장")

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes
CREATE INDEX idx_annual_fee_configs_year ON annual_fee_configs(target_year);

-- Comments
COMMENT ON TABLE annual_fee_configs IS '연도별 연회비 설정 (이월 기간 포함)';
COMMENT ON COLUMN annual_fee_configs.target_year IS '대상 년도 (예: 2025)';
COMMENT ON COLUMN annual_fee_configs.carry_over_deadline IS '이월 마감일: 이 날짜까지 가입/납부 시 전년도 연회비로 처리';
COMMENT ON COLUMN annual_fee_configs.renewal_start_date IS '갱신 시작일: 이 날부터 갱신 안내 발송';
COMMENT ON COLUMN annual_fee_configs.renewal_deadline IS '갱신 마감일: 이 날까지 미납 시 준회원 강등';
```

---

## 4. Landing Module Tables / 랜딩 모듈 테이블

### 4.1 club_histories (클럽 역사)

```sql
CREATE TABLE club_histories (
    id BIGSERIAL PRIMARY KEY,
    year INTEGER NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    image_url VARCHAR(500),
    display_order INTEGER NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes
CREATE INDEX idx_club_histories_year ON club_histories(year);
CREATE INDEX idx_club_histories_order ON club_histories(display_order);

-- Comments
COMMENT ON TABLE club_histories IS '클럽 역사 정보';
COMMENT ON COLUMN club_histories.year IS '해당 연도';
COMMENT ON COLUMN club_histories.display_order IS '표시 순서';
```

### 4.2 executives (임원진)

```sql
CREATE TABLE executives (
    id BIGSERIAL PRIMARY KEY,
    term_year INTEGER NOT NULL,
    position VARCHAR(50) NOT NULL,
    name VARCHAR(100) NOT NULL,
    profile_image_url VARCHAR(500),
    introduction TEXT,
    display_order INTEGER NOT NULL,
    is_current BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes
CREATE INDEX idx_executives_term ON executives(term_year);
CREATE INDEX idx_executives_current ON executives(is_current);
CREATE INDEX idx_executives_order ON executives(display_order);

-- Comments
COMMENT ON TABLE executives IS '역대 및 현재 임원진 정보';
COMMENT ON COLUMN executives.term_year IS '기수 (1기, 2기, ...)';
COMMENT ON COLUMN executives.position IS '직책: 회장, 부회장, 총무 등';
COMMENT ON COLUMN executives.is_current IS '현재 임원 여부';
```

### 4.3 events (이벤트/행사)

```sql
CREATE TABLE events (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    event_start_at TIMESTAMP NOT NULL,
    event_end_at TIMESTAMP NOT NULL,
    location VARCHAR(500),
    location_map_url VARCHAR(500),
    max_participants INTEGER,
    current_participants INTEGER DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'UPCOMING',
    cover_image_url VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes
CREATE INDEX idx_events_status ON events(status);
CREATE INDEX idx_events_start ON events(event_start_at);
CREATE INDEX idx_events_end ON events(event_end_at);

-- Comments
COMMENT ON TABLE events IS '클럽 이벤트/행사 정보';
COMMENT ON COLUMN events.status IS '상태: UPCOMING, ONGOING, COMPLETED, CANCELLED';
COMMENT ON COLUMN events.max_participants IS '최대 참가 인원 (NULL이면 무제한)';
```

### 4.4 event_participants (이벤트 참가자)

```sql
CREATE TABLE event_participants (
    id BIGSERIAL PRIMARY KEY,
    event_id BIGINT NOT NULL REFERENCES events(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL,
    registered_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_event_participants UNIQUE (event_id, user_id)
);

-- Indexes
CREATE INDEX idx_event_participants_event ON event_participants(event_id);
CREATE INDEX idx_event_participants_user ON event_participants(user_id);

-- Comments
COMMENT ON TABLE event_participants IS '이벤트 참가자 목록';
```

### 4.5 instagram_posts (인스타그램 포스트)

```sql
CREATE TABLE instagram_posts (
    id BIGSERIAL PRIMARY KEY,
    instagram_post_id VARCHAR(100) NOT NULL UNIQUE,
    caption TEXT,
    media_url VARCHAR(500) NOT NULL,
    media_type VARCHAR(20) NOT NULL,
    permalink VARCHAR(500),
    posted_at TIMESTAMP NOT NULL,
    like_count INTEGER,
    comment_count INTEGER,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes
CREATE INDEX idx_instagram_posts_posted_at ON instagram_posts(posted_at DESC);
CREATE INDEX idx_instagram_posts_instagram_id ON instagram_posts(instagram_post_id);

-- Comments
COMMENT ON TABLE instagram_posts IS '동기화된 인스타그램 포스트';
COMMENT ON COLUMN instagram_posts.instagram_post_id IS 'Instagram 원본 ID';
COMMENT ON COLUMN instagram_posts.media_type IS '미디어 유형: IMAGE, VIDEO, CAROUSEL';
```

---

## 5. Community Module Tables / 커뮤니티 모듈 테이블 (🆕 권한그룹 시스템 추가)

### 5.1 permission_groups (권한 그룹) - 동적 관리 (🆕)

```sql
-- 권한 그룹 (임원진이 동적으로 생성/삭제 가능)
CREATE TABLE permission_groups (
    id BIGSERIAL PRIMARY KEY,

    -- 그룹 정보
    name VARCHAR(50) NOT NULL UNIQUE,                -- 그룹명 (예: 운영진, 정회원 기본, 준회원 제한)
    description VARCHAR(200),

    -- 기본 권한 (JSON 배열)
    default_permissions JSONB NOT NULL DEFAULT '[]', -- 기본 권한 목록: ["READ", "WRITE", "COMMENT", ...]

    -- 관리
    is_system_group BOOLEAN NOT NULL DEFAULT FALSE,  -- 시스템 기본 그룹 (삭제 불가)
    created_by BIGINT,                               -- 생성자 (임원 ID, NULL이면 시스템)
    is_active BOOLEAN NOT NULL DEFAULT TRUE,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes
CREATE INDEX idx_permission_groups_name ON permission_groups(name);
CREATE INDEX idx_permission_groups_active ON permission_groups(is_active);

-- 기본 권한 그룹 데이터
INSERT INTO permission_groups (name, description, default_permissions, is_system_group) VALUES
    ('운영진', '회장, 부회장, 이사 기본 그룹', '["READ", "WRITE", "MOVE", "COMMENT", "DELETE", "HARD_DELETE", "SHARE"]', TRUE),
    ('정회원 기본', '정회원 기본 권한 그룹', '["READ", "WRITE", "COMMENT", "DELETE", "SHARE"]', TRUE),
    ('준회원 제한', '준회원 제한 권한 그룹', '["READ"]', TRUE),
    ('파트너 전용', '파트너사 권한 그룹', '["READ", "WRITE", "COMMENT"]', FALSE);

-- Comments
COMMENT ON TABLE permission_groups IS '권한 그룹 (임원진이 동적으로 관리 가능)';
COMMENT ON COLUMN permission_groups.default_permissions IS 'JSON 배열: READ, WRITE, MOVE, COMMENT, DELETE, HARD_DELETE, SHARE';
COMMENT ON COLUMN permission_groups.is_system_group IS '시스템 그룹 여부 (TRUE면 삭제 불가)';
```

### 5.2 board_permission_mappings (게시판-권한그룹 매핑) (🆕)

```sql
CREATE TABLE board_permission_mappings (
    id BIGSERIAL PRIMARY KEY,
    board_id BIGINT NOT NULL REFERENCES boards(id) ON DELETE CASCADE,
    permission_group_id BIGINT NOT NULL REFERENCES permission_groups(id) ON DELETE CASCADE,

    -- 이 게시판에서의 권한 (그룹 기본 권한과 다를 수 있음)
    permissions JSONB NOT NULL,                      -- ["READ", "WRITE", "COMMENT", ...]

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_board_permission_mappings UNIQUE (board_id, permission_group_id)
);

-- Indexes
CREATE INDEX idx_board_permission_mappings_board ON board_permission_mappings(board_id);
CREATE INDEX idx_board_permission_mappings_group ON board_permission_mappings(permission_group_id);

-- Comments
COMMENT ON TABLE board_permission_mappings IS '게시판별 권한 그룹 매핑';
COMMENT ON COLUMN board_permission_mappings.permissions IS '해당 게시판에서의 권한 (JSON 배열)';
```

### 5.3 user_permission_groups (사용자-권한그룹 매핑) (🆕)

```sql
CREATE TABLE user_permission_groups (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    permission_group_id BIGINT NOT NULL REFERENCES permission_groups(id) ON DELETE CASCADE,

    -- 부여 정보
    assigned_by BIGINT NOT NULL,                     -- 부여한 임원 ID
    assigned_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- 유효 기간 (선택)
    expires_at TIMESTAMP,                            -- 만료일 (NULL이면 무기한)

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_user_permission_groups UNIQUE (user_id, permission_group_id)
);

-- Indexes
CREATE INDEX idx_user_permission_groups_user ON user_permission_groups(user_id);
CREATE INDEX idx_user_permission_groups_group ON user_permission_groups(permission_group_id);
CREATE INDEX idx_user_permission_groups_expires ON user_permission_groups(expires_at)
    WHERE expires_at IS NOT NULL;

-- Comments
COMMENT ON TABLE user_permission_groups IS '사용자별 추가 권한 그룹 (등급 기본 그룹 외 추가)';
COMMENT ON COLUMN user_permission_groups.expires_at IS '만료일 (NULL이면 무기한)';
```

### 5.4 boards (게시판)

```sql
CREATE TABLE boards (
    id BIGSERIAL PRIMARY KEY,
    slug VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    board_type VARCHAR(20) NOT NULL,
    required_grade_to_read VARCHAR(20) NOT NULL DEFAULT 'MEMBER',
    required_grade_to_write VARCHAR(20) NOT NULL DEFAULT 'MEMBER',
    display_order INTEGER NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    allow_comments BOOLEAN NOT NULL DEFAULT TRUE,
    allow_attachments BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes
CREATE INDEX idx_boards_slug ON boards(slug);
CREATE INDEX idx_boards_active ON boards(is_active);
CREATE INDEX idx_boards_order ON boards(display_order);

-- Comments
COMMENT ON TABLE boards IS '동적 게시판 설정';
COMMENT ON COLUMN boards.slug IS 'URL용 고유 식별자';
COMMENT ON COLUMN boards.board_type IS '게시판 유형: GENERAL, NOTICE, GALLERY, QNA';
COMMENT ON COLUMN boards.required_grade_to_read IS '읽기 최소 등급';
COMMENT ON COLUMN boards.required_grade_to_write IS '쓰기 최소 등급';
```

### 5.5 posts (게시글)

```sql
CREATE TABLE posts (
    id BIGSERIAL PRIMARY KEY,
    board_id BIGINT NOT NULL REFERENCES boards(id),
    author_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    view_count INTEGER NOT NULL DEFAULT 0,
    like_count INTEGER NOT NULL DEFAULT 0,
    comment_count INTEGER NOT NULL DEFAULT 0,
    is_pinned BOOLEAN NOT NULL DEFAULT FALSE,
    is_notice BOOLEAN NOT NULL DEFAULT FALSE,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes
CREATE INDEX idx_posts_board ON posts(board_id);
CREATE INDEX idx_posts_author ON posts(author_id);
CREATE INDEX idx_posts_created ON posts(created_at DESC);
CREATE INDEX idx_posts_pinned ON posts(is_pinned, created_at DESC);
CREATE INDEX idx_posts_notice ON posts(is_notice, created_at DESC);
CREATE INDEX idx_posts_deleted ON posts(is_deleted);

-- Composite Index for common queries
CREATE INDEX idx_posts_board_active ON posts(board_id, is_deleted, created_at DESC);

-- Comments
COMMENT ON TABLE posts IS '게시글';
COMMENT ON COLUMN posts.author_id IS '작성자 ID (users 테이블 참조, 느슨한 결합)';
COMMENT ON COLUMN posts.is_pinned IS '상단 고정 여부';
COMMENT ON COLUMN posts.is_notice IS '공지 여부';
COMMENT ON COLUMN posts.is_deleted IS '삭제 여부 (Soft Delete)';
```

### 5.6 comments (댓글)

```sql
CREATE TABLE comments (
    id BIGSERIAL PRIMARY KEY,
    post_id BIGINT NOT NULL REFERENCES posts(id) ON DELETE CASCADE,
    parent_id BIGINT REFERENCES comments(id) ON DELETE CASCADE,
    author_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    like_count INTEGER NOT NULL DEFAULT 0,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes
CREATE INDEX idx_comments_post ON comments(post_id);
CREATE INDEX idx_comments_parent ON comments(parent_id);
CREATE INDEX idx_comments_author ON comments(author_id);
CREATE INDEX idx_comments_created ON comments(created_at);

-- Comments
COMMENT ON TABLE comments IS '댓글 및 대댓글';
COMMENT ON COLUMN comments.parent_id IS '부모 댓글 ID (대댓글인 경우)';
COMMENT ON COLUMN comments.is_deleted IS '삭제 여부 (Soft Delete)';
```

### 5.7 attachments (첨부파일)

```sql
CREATE TABLE attachments (
    id BIGSERIAL PRIMARY KEY,
    post_id BIGINT NOT NULL REFERENCES posts(id) ON DELETE CASCADE,
    original_file_name VARCHAR(255) NOT NULL,
    stored_file_name VARCHAR(255) NOT NULL,
    file_url VARCHAR(500) NOT NULL,
    file_size BIGINT NOT NULL,
    content_type VARCHAR(100) NOT NULL,
    display_order INTEGER NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes
CREATE INDEX idx_attachments_post ON attachments(post_id);

-- Comments
COMMENT ON TABLE attachments IS '게시글 첨부파일';
COMMENT ON COLUMN attachments.stored_file_name IS 'S3/Spaces에 저장된 파일명';
COMMENT ON COLUMN attachments.file_url IS 'CDN URL';
```

### 5.8 post_likes (게시글 좋아요)

```sql
CREATE TABLE post_likes (
    id BIGSERIAL PRIMARY KEY,
    post_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_post_likes UNIQUE (post_id, user_id)
);

-- Indexes
CREATE INDEX idx_post_likes_post ON post_likes(post_id);
CREATE INDEX idx_post_likes_user ON post_likes(user_id);

-- Comments
COMMENT ON TABLE post_likes IS '게시글 좋아요';
```

### 5.9 bookmarks (북마크)

```sql
CREATE TABLE bookmarks (
    id BIGSERIAL PRIMARY KEY,
    post_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_bookmarks UNIQUE (post_id, user_id)
);

-- Indexes
CREATE INDEX idx_bookmarks_user ON bookmarks(user_id);
CREATE INDEX idx_bookmarks_post ON bookmarks(post_id);

-- Comments
COMMENT ON TABLE bookmarks IS '사용자 북마크';
```

---

## 6. Admin Module Tables / 어드민 모듈 테이블

### 6.1 admin_actions (관리자 활동 로그)

```sql
CREATE TABLE admin_actions (
    id BIGSERIAL PRIMARY KEY,
    admin_user_id BIGINT NOT NULL,
    action_type VARCHAR(50) NOT NULL,
    target_entity VARCHAR(100) NOT NULL,
    target_entity_id BIGINT NOT NULL,
    action_detail TEXT,
    ip_address VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes
CREATE INDEX idx_admin_actions_admin ON admin_actions(admin_user_id);
CREATE INDEX idx_admin_actions_type ON admin_actions(action_type);
CREATE INDEX idx_admin_actions_target ON admin_actions(target_entity, target_entity_id);
CREATE INDEX idx_admin_actions_created ON admin_actions(created_at DESC);

-- Comments
COMMENT ON TABLE admin_actions IS '관리자 활동 감사 로그';
COMMENT ON COLUMN admin_actions.action_type IS '작업 유형: USER_GRADE_CHANGE, BOARD_CREATE, POST_DELETE 등';
COMMENT ON COLUMN admin_actions.action_detail IS 'JSON 형식의 상세 정보';
```

### 6.2 dashboard_metrics (대시보드 메트릭)

```sql
CREATE TABLE dashboard_metrics (
    id BIGSERIAL PRIMARY KEY,
    metric_date DATE NOT NULL,
    metric_type VARCHAR(50) NOT NULL,
    metric_value BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_dashboard_metrics UNIQUE (metric_date, metric_type)
);

-- Indexes
CREATE INDEX idx_dashboard_metrics_date ON dashboard_metrics(metric_date);
CREATE INDEX idx_dashboard_metrics_type ON dashboard_metrics(metric_type);

-- Comments
COMMENT ON TABLE dashboard_metrics IS '일별 대시보드 통계';
COMMENT ON COLUMN dashboard_metrics.metric_type IS '메트릭 유형: NEW_USERS, POSTS, COMMENTS, ACTIVE_USERS 등';
```

---

## 7. Chat Module Tables / 채팅 모듈 테이블

### 7.1 chat_rooms (채팅방)

```sql
CREATE TABLE chat_rooms (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100),
    room_type VARCHAR(20) NOT NULL,
    event_id BIGINT,
    last_message_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes
CREATE INDEX idx_chat_rooms_type ON chat_rooms(room_type);
CREATE INDEX idx_chat_rooms_event ON chat_rooms(event_id);
CREATE INDEX idx_chat_rooms_last_message ON chat_rooms(last_message_at DESC);

-- Comments
COMMENT ON TABLE chat_rooms IS '채팅방';
COMMENT ON COLUMN chat_rooms.room_type IS '채팅방 유형: DIRECT, GROUP, EVENT';
COMMENT ON COLUMN chat_rooms.event_id IS 'EVENT 타입인 경우 연결된 이벤트 ID';
```

### 7.2 chat_participants (채팅 참여자)

```sql
CREATE TABLE chat_participants (
    id BIGSERIAL PRIMARY KEY,
    room_id BIGINT NOT NULL REFERENCES chat_rooms(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL,
    last_read_at TIMESTAMP,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    role VARCHAR(20) NOT NULL DEFAULT 'MEMBER',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_chat_participants UNIQUE (room_id, user_id)
);

-- Indexes
CREATE INDEX idx_chat_participants_room ON chat_participants(room_id);
CREATE INDEX idx_chat_participants_user ON chat_participants(user_id);
CREATE INDEX idx_chat_participants_active ON chat_participants(is_active);

-- Comments
COMMENT ON TABLE chat_participants IS '채팅방 참여자';
COMMENT ON COLUMN chat_participants.role IS '참여자 역할: OWNER, ADMIN, MEMBER';
COMMENT ON COLUMN chat_participants.last_read_at IS '마지막으로 읽은 시간 (읽지 않은 메시지 계산용)';
```

### 7.3 chat_messages (채팅 메시지)

```sql
CREATE TABLE chat_messages (
    id BIGSERIAL PRIMARY KEY,
    room_id BIGINT NOT NULL REFERENCES chat_rooms(id) ON DELETE CASCADE,
    sender_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    message_type VARCHAR(20) NOT NULL,
    attachment_url VARCHAR(500),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes
CREATE INDEX idx_chat_messages_room ON chat_messages(room_id);
CREATE INDEX idx_chat_messages_sender ON chat_messages(sender_id);
CREATE INDEX idx_chat_messages_created ON chat_messages(created_at DESC);

-- Composite Index for pagination
CREATE INDEX idx_chat_messages_room_created ON chat_messages(room_id, created_at DESC);

-- Comments
COMMENT ON TABLE chat_messages IS '채팅 메시지';
COMMENT ON COLUMN chat_messages.message_type IS '메시지 유형: TEXT, IMAGE, FILE, SYSTEM';
COMMENT ON COLUMN chat_messages.is_deleted IS '삭제 여부 (Soft Delete)';
```

---

## 8. NaverCafe Module Tables / 네이버 카페 모듈 테이블

### 8.1 cafe_posts (카페 글)

```sql
CREATE TABLE cafe_posts (
    id BIGSERIAL PRIMARY KEY,
    cafe_article_id VARCHAR(100) NOT NULL UNIQUE,
    title VARCHAR(200) NOT NULL,
    content TEXT,
    author_nickname VARCHAR(100),
    posted_at TIMESTAMP NOT NULL,
    view_count INTEGER,
    comment_count INTEGER,
    original_url VARCHAR(500),
    linked_post_id BIGINT,
    sync_direction VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes
CREATE INDEX idx_cafe_posts_article ON cafe_posts(cafe_article_id);
CREATE INDEX idx_cafe_posts_posted ON cafe_posts(posted_at DESC);
CREATE INDEX idx_cafe_posts_linked ON cafe_posts(linked_post_id);

-- Comments
COMMENT ON TABLE cafe_posts IS '네이버 카페 글 동기화';
COMMENT ON COLUMN cafe_posts.cafe_article_id IS '네이버 카페 글 고유 ID';
COMMENT ON COLUMN cafe_posts.linked_post_id IS '연결된 커뮤니티 게시글 ID';
COMMENT ON COLUMN cafe_posts.sync_direction IS '동기화 방향: FROM_CAFE, TO_CAFE, BIDIRECTIONAL';
```

### 8.2 cafe_sync_logs (동기화 로그)

```sql
CREATE TABLE cafe_sync_logs (
    id BIGSERIAL PRIMARY KEY,
    sync_type VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    processed_count INTEGER,
    error_message TEXT,
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes
CREATE INDEX idx_cafe_sync_logs_created ON cafe_sync_logs(created_at DESC);
CREATE INDEX idx_cafe_sync_logs_status ON cafe_sync_logs(status);

-- Comments
COMMENT ON TABLE cafe_sync_logs IS '네이버 카페 동기화 로그';
COMMENT ON COLUMN cafe_sync_logs.sync_type IS '동기화 유형: FETCH, POST, UPDATE';
COMMENT ON COLUMN cafe_sync_logs.status IS '상태: SUCCESS, FAILED, PARTIAL';
```

---

## 9. Notification Module Tables / 알림 모듈 테이블 (🆕)

### 9.1 notification_preferences (알림 설정)

```sql
CREATE TABLE notification_preferences (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,

    -- 채널별 활성화
    email_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    push_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    sms_enabled BOOLEAN NOT NULL DEFAULT FALSE,      -- 기본 비활성화 (비용)

    -- 음소거된 알림 유형 (JSON 배열)
    muted_types JSONB NOT NULL DEFAULT '[]',         -- ["ANNUAL_FEE_RENEWAL_30DAYS", ...]

    -- 조용한 시간대
    quiet_hours_start TIME,                          -- 조용한 시작 시간 (예: 22:00)
    quiet_hours_end TIME,                            -- 조용한 종료 시간 (예: 08:00)

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes
CREATE INDEX idx_notification_preferences_user ON notification_preferences(user_id);

-- Comments
COMMENT ON TABLE notification_preferences IS '사용자별 알림 설정';
COMMENT ON COLUMN notification_preferences.muted_types IS '음소거된 알림 유형 목록 (JSON 배열)';
COMMENT ON COLUMN notification_preferences.sms_enabled IS 'SMS 알림 (비용 발생으로 기본 비활성화)';
```

### 9.2 notification_logs (알림 로그)

```sql
CREATE TABLE notification_logs (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,

    -- 알림 정보
    notification_type VARCHAR(50) NOT NULL,          -- 알림 유형
    channel VARCHAR(20) NOT NULL,                    -- EMAIL, PUSH, SMS
    title VARCHAR(200) NOT NULL,
    body TEXT NOT NULL,

    -- 발송 결과
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',   -- PENDING, SENT, FAILED, CANCELLED
    sent_at TIMESTAMP,
    error_message TEXT,

    -- 읽음 상태 (앱 푸시용)
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    read_at TIMESTAMP,

    -- 관련 엔티티
    related_entity_type VARCHAR(50),                 -- MEMBERSHIP_APPLICATION, PAYMENT, EVENT 등
    related_entity_id BIGINT,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes
CREATE INDEX idx_notification_logs_user ON notification_logs(user_id);
CREATE INDEX idx_notification_logs_type ON notification_logs(notification_type);
CREATE INDEX idx_notification_logs_channel ON notification_logs(channel);
CREATE INDEX idx_notification_logs_status ON notification_logs(status);
CREATE INDEX idx_notification_logs_created ON notification_logs(created_at DESC);
CREATE INDEX idx_notification_logs_unread ON notification_logs(user_id, is_read)
    WHERE is_read = FALSE;

-- Comments
COMMENT ON TABLE notification_logs IS '발송된 알림 로그';
COMMENT ON COLUMN notification_logs.notification_type IS '알림 유형: MEMBERSHIP_*, ANNUAL_FEE_*, EVENT_*, etc.';
COMMENT ON COLUMN notification_logs.channel IS '발송 채널: EMAIL, PUSH, SMS';
COMMENT ON COLUMN notification_logs.status IS '발송 상태: PENDING, SENT, FAILED, CANCELLED';
```

### 9.3 push_tokens (푸시 토큰)

```sql
CREATE TABLE push_tokens (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,

    -- 토큰 정보
    token TEXT NOT NULL,
    platform VARCHAR(20) NOT NULL,                   -- IOS, ANDROID, WEB
    device_id VARCHAR(255),                          -- 기기 고유 ID
    device_name VARCHAR(100),                        -- 기기명 (예: "iPhone 15 Pro")

    -- 상태
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    last_used_at TIMESTAMP,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_push_tokens UNIQUE (user_id, token)
);

-- Indexes
CREATE INDEX idx_push_tokens_user ON push_tokens(user_id);
CREATE INDEX idx_push_tokens_active ON push_tokens(user_id, is_active)
    WHERE is_active = TRUE;

-- Comments
COMMENT ON TABLE push_tokens IS '푸시 알림 토큰 (FCM/APNs)';
COMMENT ON COLUMN push_tokens.platform IS '플랫폼: IOS, ANDROID, WEB';
COMMENT ON COLUMN push_tokens.token IS 'FCM 또는 APNs 토큰';
```

---

## 10. Migration Tables / 마이그레이션 테이블 (🆕)

### 10.1 legacy_members (기존 회원 마이그레이션)

```sql
-- 기존 610명 정회원 마이그레이션용 임시 테이블
CREATE TABLE legacy_members (
    id BIGSERIAL PRIMARY KEY,

    -- 기본 정보
    member_number INTEGER NOT NULL UNIQUE,           -- 정회원 번호
    name VARCHAR(50) NOT NULL,                       -- 이름
    phone_number VARCHAR(20) NOT NULL,               -- 전화번호 (매칭 키)
    email VARCHAR(255),                              -- 이메일 (있는 경우)

    -- 차량 정보 (선택)
    car_model VARCHAR(100),                          -- 차종
    car_number VARCHAR(20),                          -- 차량번호

    -- 기타 정보
    grade_code VARCHAR(30),                          -- 등급 코드
    join_date DATE,                                  -- 가입일
    notes VARCHAR(500),                              -- 비고

    -- 마이그레이션 상태
    is_linked BOOLEAN NOT NULL DEFAULT FALSE,        -- OAuth 연동 완료 여부
    linked_user_id BIGINT,                           -- 연동된 users.id
    linked_at TIMESTAMP,                             -- 연동 완료 시각

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes
CREATE INDEX idx_legacy_members_phone ON legacy_members(phone_number);
CREATE INDEX idx_legacy_members_linked ON legacy_members(is_linked);
CREATE INDEX idx_legacy_members_number ON legacy_members(member_number);

-- Comments
COMMENT ON TABLE legacy_members IS '기존 정회원 마이그레이션 테이블 (임시)';
COMMENT ON COLUMN legacy_members.is_linked IS 'OAuth 연동 완료 여부';
COMMENT ON COLUMN legacy_members.linked_user_id IS '연동된 신규 사용자 ID';

/*
마이그레이션 매칭 로직:
1. OAuth 로그인 시 전화번호로 legacy_members 검색
2. 매칭 성공 → member_number + grade 부여
3. is_linked = TRUE, linked_user_id 업데이트
4. 마이그레이션 완료 후 테이블 DROP (또는 아카이브)
*/
```

---

## 11. Flyway Migration Files / Flyway 마이그레이션 파일

### 11.1 Migration File Structure / 마이그레이션 파일 구조

```
src/main/resources/db/migration/
├── V1__create_user_module_tables.sql         # user_grades, users, oauth_accounts, passkeys, vehicles
├── V2__create_membership_module_tables.sql   # 🆕 신청서, 서류, OCR, 결제, 기간, 이사파트, 연회비설정
├── V3__create_landing_module_tables.sql      # 역사, 임원진, 이벤트, 인스타그램
├── V4__create_community_module_tables.sql    # 권한그룹, 게시판, 게시글, 댓글, 첨부파일
├── V5__create_admin_module_tables.sql        # 관리자 로그, 대시보드 메트릭
├── V6__create_chat_module_tables.sql         # 채팅방, 참여자, 메시지
├── V7__create_navercafe_module_tables.sql    # 카페 글, 동기화 로그
├── V8__create_notification_module_tables.sql # 🆕 알림 설정, 로그, 푸시 토큰
├── V9__create_migration_tables.sql           # 🆕 기존 회원 마이그레이션
├── V10__add_initial_data.sql                 # 기본 등급, 권한 그룹 데이터
└── V11__add_additional_indexes.sql           # 추가 인덱스
```

### 11.2 Sample Migration File / 마이그레이션 파일 예시

```sql
-- V1__create_user_module_tables.sql
-- User Module 테이블 생성
-- 작성일: 2025-XX-XX

-- users 테이블 생성
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    nickname VARCHAR(100),
    profile_image_url VARCHAR(500),
    grade VARCHAR(20) NOT NULL DEFAULT 'MEMBER',
    oauth_provider VARCHAR(20) NOT NULL,
    oauth_provider_id VARCHAR(255) NOT NULL,
    phone_number VARCHAR(20),
    car_model VARCHAR(100),
    car_year VARCHAR(10),
    last_login_at TIMESTAMP,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_users_oauth UNIQUE (oauth_provider, oauth_provider_id)
);

-- 인덱스 생성
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_grade ON users(grade);
```

---

## 12. Entity Relationship Diagram / ERD

```
┌─────────────────────────────────────────────────────────────────────────────────────┐
│                                     USER MODULE                                      │
├─────────────────────────────────────────────────────────────────────────────────────┤
│  ┌─────────────┐                                                                    │
│  │   users     │                                                                    │
│  │─────────────│                                                                    │
│  │ id (PK)     │◄─────────────────────────────────────────────────────────┐        │
│  │ username    │                                                          │        │
│  │ email       │                                                          │        │
│  │ grade       │                                                          │        │
│  │ oauth_*     │                                                          │        │
│  └─────────────┘                                                          │        │
└───────────────────────────────────────────────────────────────────────────┼────────┘
                                                                            │
┌───────────────────────────────────────────────────────────────────────────┼────────┐
│                                   LANDING MODULE                          │        │
├───────────────────────────────────────────────────────────────────────────┼────────┤
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  ┌─────────────────┐   │        │
│  │  histories  │  │ executives  │  │   events    │  │ instagram_posts │   │        │
│  │─────────────│  │─────────────│  │─────────────│  │─────────────────│   │        │
│  │ id (PK)     │  │ id (PK)     │  │ id (PK)     │◄─┤ id (PK)         │   │        │
│  │ year        │  │ term_year   │  │ title       │  │ instagram_id    │   │        │
│  │ title       │  │ position    │  │ start_at    │  │ caption         │   │        │
│  │ description │  │ name        │  │ end_at      │  │ media_url       │   │        │
│  └─────────────┘  └─────────────┘  └──────┬──────┘  └─────────────────┘   │        │
│                                           │                                │        │
│                                    ┌──────▼──────┐                        │        │
│                                    │ event_      │                        │        │
│                                    │ participants│────────────────────────┘        │
│                                    │─────────────│                                 │
│                                    │ event_id(FK)│                                 │
│                                    │ user_id     │                                 │
│                                    └─────────────┘                                 │
└────────────────────────────────────────────────────────────────────────────────────┘

┌────────────────────────────────────────────────────────────────────────────────────┐
│                                  COMMUNITY MODULE                                   │
├────────────────────────────────────────────────────────────────────────────────────┤
│  ┌─────────────┐      ┌─────────────┐      ┌─────────────┐                         │
│  │   boards    │      │    posts    │      │  comments   │                         │
│  │─────────────│      │─────────────│      │─────────────│                         │
│  │ id (PK)     │◄─────┤ board_id(FK)│◄─────┤ post_id(FK) │                         │
│  │ slug        │      │ author_id   │──────┤ parent_id   │◄─┐                      │
│  │ name        │      │ title       │      │ author_id   │  │ (Self Reference)     │
│  │ board_type  │      │ content     │      │ content     │──┘                      │
│  │ grade_read  │      │ view_count  │      └─────────────┘                         │
│  │ grade_write │      │ like_count  │                                              │
│  └─────────────┘      └──────┬──────┘                                              │
│                              │                                                      │
│         ┌────────────────────┼────────────────────┐                                │
│         ▼                    ▼                    ▼                                │
│  ┌─────────────┐      ┌─────────────┐      ┌─────────────┐                         │
│  │ attachments │      │ post_likes  │      │  bookmarks  │                         │
│  │─────────────│      │─────────────│      │─────────────│                         │
│  │ post_id(FK) │      │ post_id     │      │ post_id     │                         │
│  │ file_name   │      │ user_id     │      │ user_id     │                         │
│  │ file_url    │      └─────────────┘      └─────────────┘                         │
│  └─────────────┘                                                                   │
└────────────────────────────────────────────────────────────────────────────────────┘

┌────────────────────────────────────────────────────────────────────────────────────┐
│                                    CHAT MODULE                                      │
├────────────────────────────────────────────────────────────────────────────────────┤
│  ┌─────────────┐      ┌─────────────────┐      ┌─────────────────┐                 │
│  │ chat_rooms  │      │ chat_participants│      │  chat_messages  │                 │
│  │─────────────│      │─────────────────│      │─────────────────│                 │
│  │ id (PK)     │◄─────┤ room_id (FK)    │      │ room_id (FK)    │◄────────────────┤
│  │ name        │      │ user_id         │      │ sender_id       │                 │
│  │ room_type   │      │ role            │      │ content         │                 │
│  │ event_id    │      │ last_read_at    │      │ message_type    │                 │
│  └─────────────┘      └─────────────────┘      └─────────────────┘                 │
└────────────────────────────────────────────────────────────────────────────────────┘

┌────────────────────────────────────────────────────────────────────────────────────┐
│                                  NAVERCAFE MODULE                                   │
├────────────────────────────────────────────────────────────────────────────────────┤
│  ┌─────────────────┐      ┌─────────────────┐                                      │
│  │   cafe_posts    │      │ cafe_sync_logs  │                                      │
│  │─────────────────│      │─────────────────│                                      │
│  │ id (PK)         │      │ id (PK)         │                                      │
│  │ cafe_article_id │      │ sync_type       │                                      │
│  │ title           │      │ status          │                                      │
│  │ linked_post_id  │──────│ processed_count │                                      │
│  │ sync_direction  │      │ error_message   │                                      │
│  └─────────────────┘      └─────────────────┘                                      │
└────────────────────────────────────────────────────────────────────────────────────┘

┌────────────────────────────────────────────────────────────────────────────────────┐
│                                   ADMIN MODULE                                      │
├────────────────────────────────────────────────────────────────────────────────────┤
│  ┌─────────────────────┐      ┌─────────────────────┐                              │
│  │    admin_actions    │      │  dashboard_metrics  │                              │
│  │─────────────────────│      │─────────────────────│                              │
│  │ id (PK)             │      │ id (PK)             │                              │
│  │ admin_user_id       │      │ metric_date         │                              │
│  │ action_type         │      │ metric_type         │                              │
│  │ target_entity       │      │ metric_value        │                              │
│  │ target_entity_id    │      └─────────────────────┘                              │
│  │ action_detail       │                                                           │
│  └─────────────────────┘                                                           │
└────────────────────────────────────────────────────────────────────────────────────┘
```

---

## 13. Data Types Reference / 데이터 타입 참조

### 13.1 Enum Values / Enum 값 (🆕 업데이트)

**User Module**

| Type | Values | Description |
|------|--------|-------------|
| UserGrade (동적) | DB 테이블로 관리 | DEVELOPER, ADVISOR, PRESIDENT, VICE_PRESIDENT, DIRECTOR, REGULAR, ASSOCIATE, PARTNER (기본값) |
| AssociateStatus | PENDING, REVIEWING, EXPIRED, REJECTED | 준회원 상태 |
| ExemptionType | NONE, PERMANENT, ONE_TIME | 연회비 면제 유형 |
| OAuthProvider | GOOGLE, APPLE, NAVER | OAuth 제공자 |
| VehicleOwnershipType | PERSONAL, CORPORATE, LEASE, RENTAL, CORPORATE_LEASE, CORPORATE_RENTAL | 차량 소유 유형 |
| VehicleStatus | ACTIVE, SOLD, GRACE_PERIOD | 차량 상태 |

**Membership Module**

| Type | Values | Description |
|------|--------|-------------|
| ApplicationStatus | DOCUMENT_PENDING, DOCUMENT_SUBMITTED, UNDER_REVIEW, DOCUMENT_APPROVED, DOCUMENT_REJECTED, PAYMENT_PENDING, PAYMENT_CONFIRMED, COMPLETED, CANCELLED | 신청 상태 |
| DocumentType | VEHICLE_REGISTRATION, ID_CARD, BUSINESS_LICENSE, EMPLOYMENT_CERTIFICATE, LEASE_CONTRACT, RENTAL_CONTRACT | 서류 유형 |
| VerificationStatus | PENDING, VERIFIED, REJECTED | 서류 검증 상태 |
| PaymentType | ENROLLMENT_FEE, ANNUAL_FEE | 결제 유형 |
| PaymentStatus | PENDING, CONFIRMED, CANCELLED, REFUNDED | 결제 상태 |
| MembershipStatus | ACTIVE, EXPIRED, CANCELLED | 멤버십 기간 상태 |
| OcrProvider | PADDLE_OCR, TESSERACT, NAVER_CLOVA | OCR 제공자 |

**Community Module**

| Type | Values | Description |
|------|--------|-------------|
| BoardPermission | READ, WRITE, MOVE, COMMENT, DELETE, HARD_DELETE, SHARE | 게시판 권한 |
| BoardType | GENERAL, NOTICE, GALLERY, QNA | 게시판 유형 |

**Landing Module**

| Type | Values | Description |
|------|--------|-------------|
| EventStatus | UPCOMING, ONGOING, COMPLETED, CANCELLED | 이벤트 상태 |
| MediaType | IMAGE, VIDEO, CAROUSEL | 인스타그램 미디어 유형 |

**Chat Module**

| Type | Values | Description |
|------|--------|-------------|
| ChatRoomType | DIRECT, GROUP, EVENT | 채팅방 유형 |
| MessageType | TEXT, IMAGE, FILE, SYSTEM | 메시지 유형 |
| ParticipantRole | OWNER, ADMIN, MEMBER | 참여자 역할 |

**NaverCafe Module**

| Type | Values | Description |
|------|--------|-------------|
| SyncDirection | FROM_CAFE, TO_CAFE, BIDIRECTIONAL | 동기화 방향 |
| SyncType | FETCH, POST, UPDATE | 동기화 유형 |
| SyncStatus | SUCCESS, FAILED, PARTIAL | 동기화 상태 |

**Notification Module**

| Type | Values | Description |
|------|--------|-------------|
| NotificationType | MEMBERSHIP_APPLICATION_RECEIVED, MEMBERSHIP_DOCUMENT_APPROVED, MEMBERSHIP_UPGRADE_COMPLETE, MEMBERSHIP_EXPIRED, ANNUAL_FEE_RENEWAL_30DAYS, ANNUAL_FEE_RENEWAL_7DAYS, etc. | 알림 유형 |
| NotificationChannel | EMAIL, PUSH, SMS | 알림 채널 |
| NotificationStatus | PENDING, SENT, FAILED, CANCELLED | 발송 상태 |
| PushPlatform | IOS, ANDROID, WEB | 푸시 플랫폼 |

**Admin Module**

| Type | Values | Description |
|------|--------|-------------|
| ActionType | USER_GRADE_CHANGE, MEMBERSHIP_APPROVE, MEMBERSHIP_REJECT, PAYMENT_CONFIRM, BOARD_CREATE, POST_DELETE, EXEMPTION_GRANT, etc. | 관리자 활동 유형 |
| MetricType | NEW_USERS, POSTS, COMMENTS, ACTIVE_USERS, MEMBERSHIP_APPLICATIONS, PAYMENTS, etc. | 대시보드 메트릭 유형 |

---

## Document History / 문서 이력

| Version | Date | Author | Description |
|---------|------|--------|-------------|
| 1.0 | 2025-12-30 | Claude | Initial database schema design |
| 1.1 | 2025-12-30 | Claude | 🆕 등급 동적 관리 (user_grades 테이블), Membership 모듈, Notification 모듈, 권한그룹 시스템, 연회비 이월 정책 추가 |
