-- V2__create_membership_module_tables.sql
-- Membership Module 테이블 생성 (Enum 기반)
-- 작성일: 2025-01-06
-- 기반: DATABASE_SCHEMA.md (최신 스펙)

-- ========================================
-- 1. membership_applications (정회원 신청서)
-- ========================================
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

-- ========================================
-- 2. application_documents (제출 서류)
-- ========================================
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

-- ========================================
-- 3. ocr_results (OCR 추출 결과)
-- ========================================
CREATE TABLE ocr_results (
    id BIGSERIAL PRIMARY KEY,
    document_id BIGINT NOT NULL REFERENCES application_documents(id) ON DELETE CASCADE,

    -- OCR 메타데이터
    ocr_provider VARCHAR(30) NOT NULL,                   -- PADDLE_OCR, TESSERACT, NAVER_CLOVA
    ocr_version VARCHAR(20),
    processing_time_ms INTEGER,
    confidence_score DECIMAL(5,4),                       -- 0.0000 ~ 1.0000
    is_success BOOLEAN NOT NULL DEFAULT TRUE,            -- OCR 처리 성공 여부

    -- 추출된 데이터 (JSON)
    extracted_data JSONB NOT NULL,                       -- 서류별 추출 결과
    raw_text TEXT,                                       -- 원본 추출 텍스트

    -- 대조 결과
    match_result JSONB,                                  -- 신청 정보와 대조 결과
    is_matched BOOLEAN,                                  -- 전체 대조 성공 여부
    mismatch_fields TEXT[],                              -- 불일치 필드 목록

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes
CREATE INDEX idx_ocr_results_document ON ocr_results(document_id);
CREATE INDEX idx_ocr_results_matched ON ocr_results(is_matched);
CREATE INDEX idx_ocr_results_provider ON ocr_results(ocr_provider);

-- Comments
COMMENT ON TABLE ocr_results IS 'OCR 추출 결과';
COMMENT ON COLUMN ocr_results.extracted_data IS 'JSON 형식의 추출 결과 (서류별 다름): 차량등록증 {owner_name, car_number, vin_number}, 신분증 {name, is_masked}, 사업자등록증 {company_name, representative_name}';
COMMENT ON COLUMN ocr_results.match_result IS 'JSON 형식의 대조 결과: {field_name: {expected, actual, matched}}';

-- ========================================
-- 4. payment_records (결제 기록)
-- ========================================
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

-- ========================================
-- 5. membership_periods (멤버십 기간)
-- ========================================
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

-- ========================================
-- 6. director_parts (이사 파트)
-- ========================================
CREATE TABLE director_parts (
    id BIGSERIAL PRIMARY KEY,

    -- 파트 정보
    name VARCHAR(50) NOT NULL UNIQUE,                    -- 파트명 (예: 행사, 홍보, 총무)
    description VARCHAR(200),
    display_order INTEGER NOT NULL DEFAULT 0,            -- 표시 순서

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

-- ========================================
-- 7. annual_fee_configs (연회비 설정)
-- ========================================
CREATE TABLE annual_fee_configs (
    id BIGSERIAL PRIMARY KEY,

    -- 대상 년도
    target_year INTEGER NOT NULL UNIQUE,                 -- 대상 년도 (예: 2025)

    -- 이월 정책
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

-- ========================================
-- 8. member_vehicles (회원 차량)
-- ========================================
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

-- ========================================
-- 초기 연회비 설정 (2025년)
-- ========================================
INSERT INTO annual_fee_configs (
    target_year,
    carry_over_deadline,
    renewal_start_date,
    renewal_deadline,
    enrollment_fee_amount,
    annual_fee_amount,
    configured_by,
    configured_at,
    notes
) VALUES (
    2025,
    '2025-01-15',
    '2025-01-01',
    '2025-01-31',
    200000.00,
    200000.00,
    1,
    CURRENT_TIMESTAMP,
    '초기 설정'
);
