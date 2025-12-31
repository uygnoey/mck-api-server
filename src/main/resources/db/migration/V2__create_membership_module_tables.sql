-- V2__create_membership_module_tables.sql
-- Membership Module 테이블 생성
-- 작성일: 2025-12-31

-- ========================================
-- 1. common_codes (공통 코드) - Enum 대체
-- ========================================
CREATE TABLE common_codes (
    id BIGSERIAL PRIMARY KEY,

    -- 코드 정보
    code_group VARCHAR(50) NOT NULL,
    code VARCHAR(50) NOT NULL,
    name VARCHAR(100) NOT NULL,

    -- 표시 설정
    display_order INTEGER NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,

    -- 부가 정보
    description VARCHAR(500),
    attribute1 VARCHAR(255),
    attribute2 VARCHAR(255),
    attribute3 VARCHAR(255),

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_common_code UNIQUE (code_group, code)
);

-- Indexes
CREATE INDEX idx_common_codes_group ON common_codes(code_group);
CREATE INDEX idx_common_codes_active ON common_codes(code_group, is_active);

-- Comments
COMMENT ON TABLE common_codes IS '공통 코드 테이블 (동적 Enum 관리)';
COMMENT ON COLUMN common_codes.code_group IS '코드 그룹: VEHICLE_OWNERSHIP_TYPE, DOCUMENT_TYPE, VERIFICATION_STATUS, PAYMENT_TYPE, PAYMENT_STATUS, VEHICLE_STATUS, APPLICATION_STATUS';

-- ========================================
-- 2. membership_applications (정회원 신청)
-- ========================================
CREATE TABLE membership_applications (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),

    -- 신청자 정보
    real_name VARCHAR(50) NOT NULL,
    phone_number VARCHAR(20) NOT NULL,

    -- 차량 정보
    car_number VARCHAR(20) NOT NULL,
    vin_number VARCHAR(50) NOT NULL,
    ownership_type_code_id BIGINT NOT NULL REFERENCES common_codes(id),

    -- 신청 상태
    status_code_id BIGINT NOT NULL REFERENCES common_codes(id),

    -- 승인/반려 정보
    rejection_reason VARCHAR(500),
    reviewed_by_admin_id BIGINT REFERENCES users(id),
    reviewed_at TIMESTAMP,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes
CREATE INDEX idx_membership_applications_user ON membership_applications(user_id);
CREATE INDEX idx_membership_applications_status ON membership_applications(status_code_id);
CREATE INDEX idx_membership_applications_vin ON membership_applications(vin_number);

-- Comments
COMMENT ON TABLE membership_applications IS '정회원 가입 신청서';
COMMENT ON COLUMN membership_applications.status_code_id IS '상태: PENDING(심사대기), DOCUMENT_VERIFICATION(서류검증중), PAYMENT_PENDING(결제대기), APPROVED(승인), REJECTED(반려)';

-- ========================================
-- 3. application_documents (신청 서류)
-- ========================================
CREATE TABLE application_documents (
    id BIGSERIAL PRIMARY KEY,
    application_id BIGINT NOT NULL REFERENCES membership_applications(id) ON DELETE CASCADE,

    -- 서류 정보
    document_type_code_id BIGINT NOT NULL REFERENCES common_codes(id),
    file_url VARCHAR(500) NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_size BIGINT NOT NULL,
    content_type VARCHAR(100) NOT NULL,

    -- 검증 상태
    is_verified BOOLEAN NOT NULL DEFAULT FALSE,
    uploaded_at TIMESTAMP NOT NULL,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes
CREATE INDEX idx_application_documents_application ON application_documents(application_id);
CREATE INDEX idx_application_documents_type ON application_documents(document_type_code_id);
CREATE INDEX idx_application_documents_verified ON application_documents(is_verified);

-- Comments
COMMENT ON TABLE application_documents IS '정회원 신청 첨부 서류';
COMMENT ON COLUMN application_documents.document_type_code_id IS '서류 타입: VEHICLE_REGISTRATION(차량등록증), ID_CARD(신분증), BUSINESS_LICENSE(사업자등록증), CORPORATE_SEAL(법인인감증명서)';

-- ========================================
-- 4. ocr_results (OCR 검증 결과)
-- ========================================
CREATE TABLE ocr_results (
    id BIGSERIAL PRIMARY KEY,
    document_id BIGINT NOT NULL REFERENCES application_documents(id) ON DELETE CASCADE,

    -- OCR 처리 결과
    is_success BOOLEAN NOT NULL DEFAULT FALSE,
    confidence_score NUMERIC(3,2),
    extracted_text TEXT,

    -- 검증 결과
    is_verified BOOLEAN NOT NULL DEFAULT FALSE,
    verification_message VARCHAR(500),

    -- 처리 정보
    processed_at TIMESTAMP NOT NULL,
    ocr_engine VARCHAR(50),

    -- 에러 정보
    error_message VARCHAR(1000),

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes
CREATE INDEX idx_ocr_results_document ON ocr_results(document_id);
CREATE INDEX idx_ocr_results_success ON ocr_results(is_success);
CREATE INDEX idx_ocr_results_confidence ON ocr_results(confidence_score);

-- Comments
COMMENT ON TABLE ocr_results IS 'OCR 서류 검증 결과';
COMMENT ON COLUMN ocr_results.extracted_text IS 'OCR로 추출된 텍스트 데이터';

-- ========================================
-- 5. payment_records (결제 기록)
-- ========================================
CREATE TABLE payment_records (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),

    -- 결제 정보
    payment_type_code_id BIGINT NOT NULL REFERENCES common_codes(id),
    amount NUMERIC(10,0) NOT NULL,
    depositor_name VARCHAR(50) NOT NULL,
    deposit_date DATE NOT NULL,

    -- 결제 상태
    payment_status_code_id BIGINT NOT NULL REFERENCES common_codes(id),

    -- 확인 정보
    confirmed_by_admin_id BIGINT REFERENCES users(id),
    confirmed_at TIMESTAMP,

    -- 자동 확인 여부
    auto_confirmed BOOLEAN NOT NULL DEFAULT FALSE,

    -- OpenBanking 자동 확인
    bank_transaction_id VARCHAR(100),

    -- 대상 연도
    target_year INTEGER NOT NULL,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes
CREATE INDEX idx_payment_records_user ON payment_records(user_id);
CREATE INDEX idx_payment_records_status ON payment_records(payment_status_code_id);
CREATE INDEX idx_payment_records_type ON payment_records(payment_type_code_id);
CREATE INDEX idx_payment_records_year ON payment_records(target_year);
CREATE INDEX idx_payment_records_deposit_date ON payment_records(deposit_date);

-- Comments
COMMENT ON TABLE payment_records IS '결제 기록 (가입비, 연회비)';
COMMENT ON COLUMN payment_records.payment_type_code_id IS '결제 타입: ENROLLMENT_FEE(가입비), ANNUAL_FEE(연회비)';
COMMENT ON COLUMN payment_records.payment_status_code_id IS '결제 상태: PENDING(확인대기), CONFIRMED(확인완료), REJECTED(반려)';
COMMENT ON COLUMN payment_records.bank_transaction_id IS 'OpenBanking 거래 ID (자동 확인용)';

-- ========================================
-- 6. membership_periods (멤버십 기간)
-- ========================================
CREATE TABLE membership_periods (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),

    -- 기간 정보
    year INTEGER NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    renewal_deadline DATE NOT NULL,

    -- 상태
    is_active BOOLEAN NOT NULL DEFAULT TRUE,

    -- 결제 연결
    payment_record_id BIGINT REFERENCES payment_records(id),

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_membership_period UNIQUE (user_id, year)
);

-- Indexes
CREATE INDEX idx_membership_periods_user ON membership_periods(user_id);
CREATE INDEX idx_membership_periods_year ON membership_periods(year);
CREATE INDEX idx_membership_periods_active ON membership_periods(user_id, is_active);
CREATE INDEX idx_membership_periods_renewal ON membership_periods(renewal_deadline);

-- Comments
COMMENT ON TABLE membership_periods IS '멤버십 유효 기간 (연도별)';
COMMENT ON COLUMN membership_periods.renewal_deadline IS '갱신 기한 (보통 매년 1월 31일)';

-- ========================================
-- 7. annual_fee_configs (연회비 설정)
-- ========================================
CREATE TABLE annual_fee_configs (
    id BIGSERIAL PRIMARY KEY,

    -- 대상 연도
    target_year INTEGER NOT NULL UNIQUE,

    -- 기한 설정
    carry_over_deadline DATE NOT NULL,
    renewal_start_date DATE NOT NULL,
    renewal_deadline DATE NOT NULL,

    -- 금액
    annual_fee_amount NUMERIC(10,0) NOT NULL,

    -- 설정 정보
    configured_by_admin_id BIGINT NOT NULL,
    configured_at TIMESTAMP NOT NULL,
    notes VARCHAR(500),

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes
CREATE INDEX idx_annual_fee_configs_year ON annual_fee_configs(target_year);

-- Comments
COMMENT ON TABLE annual_fee_configs IS '연회비 설정 (연도별)';
COMMENT ON COLUMN annual_fee_configs.carry_over_deadline IS '전년도 이월 마감일 (보통 1월 말)';
COMMENT ON COLUMN annual_fee_configs.renewal_start_date IS '갱신 시작일 (보통 11월 1일)';
COMMENT ON COLUMN annual_fee_configs.renewal_deadline IS '갱신 마감일 (보통 1월 31일)';

-- ========================================
-- 8. director_parts (이사 파트 마스터)
-- ========================================
CREATE TABLE director_parts (
    id BIGSERIAL PRIMARY KEY,

    -- 파트 정보
    part_name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(500),

    -- 정렬 및 상태
    display_order INTEGER NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,

    -- 생성 정보
    created_by_admin_id BIGINT NOT NULL,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_director_part_name UNIQUE (part_name)
);

-- Indexes
CREATE INDEX idx_director_parts_name ON director_parts(part_name);
CREATE INDEX idx_director_parts_active ON director_parts(is_active);
CREATE INDEX idx_director_parts_order ON director_parts(display_order);

-- Comments
COMMENT ON TABLE director_parts IS '이사 파트 마스터 (총무, 행사, 홍보 등)';
COMMENT ON COLUMN director_parts.part_name IS '파트명 (UNIQUE): 총무, 행사, 홍보, 기술 등';
COMMENT ON COLUMN director_parts.display_order IS '화면 표시 순서';
COMMENT ON COLUMN director_parts.is_active IS '활성 여부 (false면 폐지된 파트)';

-- ========================================
-- 초기 공통 코드 데이터
-- ========================================

-- VEHICLE_OWNERSHIP_TYPE (차량 소유 유형)
INSERT INTO common_codes (code_group, code, name, display_order) VALUES
    ('VEHICLE_OWNERSHIP_TYPE', 'PERSONAL', '개인 소유', 1),
    ('VEHICLE_OWNERSHIP_TYPE', 'CORPORATE', '법인 소유', 2),
    ('VEHICLE_OWNERSHIP_TYPE', 'LEASE', '개인 리스', 3),
    ('VEHICLE_OWNERSHIP_TYPE', 'RENTAL', '개인 렌탈', 4),
    ('VEHICLE_OWNERSHIP_TYPE', 'CORPORATE_LEASE', '법인 리스', 5),
    ('VEHICLE_OWNERSHIP_TYPE', 'CORPORATE_RENTAL', '법인 렌탈', 6);

-- DOCUMENT_TYPE (서류 타입)
INSERT INTO common_codes (code_group, code, name, display_order) VALUES
    ('DOCUMENT_TYPE', 'VEHICLE_REGISTRATION', '차량등록증', 1),
    ('DOCUMENT_TYPE', 'ID_CARD', '신분증', 2),
    ('DOCUMENT_TYPE', 'BUSINESS_LICENSE', '사업자등록증', 3),
    ('DOCUMENT_TYPE', 'CORPORATE_SEAL', '법인인감증명서', 4);


-- APPLICATION_STATUS (신청 상태)
INSERT INTO common_codes (code_group, code, name, display_order) VALUES
    ('APPLICATION_STATUS', 'PENDING', '심사대기', 1),
    ('APPLICATION_STATUS', 'DOCUMENT_VERIFICATION', '서류검증중', 2),
    ('APPLICATION_STATUS', 'PAYMENT_PENDING', '결제대기', 3),
    ('APPLICATION_STATUS', 'APPROVED', '승인완료', 4),
    ('APPLICATION_STATUS', 'REJECTED', '반려', 5);

-- PAYMENT_TYPE (결제 타입)
INSERT INTO common_codes (code_group, code, name, display_order) VALUES
    ('PAYMENT_TYPE', 'ENROLLMENT_FEE', '가입비', 1),
    ('PAYMENT_TYPE', 'ANNUAL_FEE', '연회비', 2);

-- PAYMENT_STATUS (결제 상태)
INSERT INTO common_codes (code_group, code, name, display_order) VALUES
    ('PAYMENT_STATUS', 'PENDING', '확인대기', 1),
    ('PAYMENT_STATUS', 'CONFIRMED', '확인완료', 2),
    ('PAYMENT_STATUS', 'REJECTED', '반려', 3);

-- VEHICLE_STATUS (차량 상태)
INSERT INTO common_codes (code_group, code, name, display_order) VALUES
    ('VEHICLE_STATUS', 'ACTIVE', '활성', 1),
    ('VEHICLE_STATUS', 'GRACE_PERIOD', '유예기간', 2),
    ('VEHICLE_STATUS', 'SOLD', '매각', 3);

-- ========================================
-- 9. member_vehicles (회원 차량) - CommonCode 기반
-- ========================================
CREATE TABLE member_vehicles (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),

    -- 차량 정보
    license_plate VARCHAR(20) NOT NULL,
    vin_number VARCHAR(50) NOT NULL UNIQUE,
    model_name VARCHAR(100) NOT NULL,

    -- CommonCode 참조
    ownership_type_code_id BIGINT NOT NULL REFERENCES common_codes(id),
    status_code_id BIGINT NOT NULL REFERENCES common_codes(id),

    -- 등록/매각 정보
    registered_at DATE NOT NULL DEFAULT CURRENT_DATE,
    sold_at DATE,
    grace_period_end_at DATE,

    -- 대표 차량
    is_primary BOOLEAN NOT NULL DEFAULT FALSE,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes
CREATE INDEX idx_member_vehicles_user ON member_vehicles(user_id);
CREATE INDEX idx_member_vehicles_vin ON member_vehicles(vin_number);
CREATE INDEX idx_member_vehicles_status ON member_vehicles(status_code_id);
CREATE INDEX idx_member_vehicles_primary ON member_vehicles(user_id, is_primary);
CREATE INDEX idx_member_vehicles_license ON member_vehicles(license_plate);

-- Comments
COMMENT ON TABLE member_vehicles IS '회원 차량 정보 (다중 차량 등록 가능)';
COMMENT ON COLUMN member_vehicles.vin_number IS '차대번호 (중복 불가, 이중 등록 방지)';
COMMENT ON COLUMN member_vehicles.license_plate IS '차량번호';
COMMENT ON COLUMN member_vehicles.ownership_type_code_id IS '소유 유형 CommonCode 참조';
COMMENT ON COLUMN member_vehicles.status_code_id IS '차량 상태 CommonCode 참조';
COMMENT ON COLUMN member_vehicles.grace_period_end_at IS '유예 종료일 (매각 후 6개월)';

-- ========================================
-- 초기 연회비 설정 (2025년)
-- ========================================
INSERT INTO annual_fee_configs (target_year, carry_over_deadline, renewal_start_date, renewal_deadline, annual_fee_amount, configured_by_admin_id, configured_at, notes) VALUES
    (2025, '2025-01-31', '2024-11-01', '2025-01-31', 200000, 1, CURRENT_TIMESTAMP, '초기 설정');

-- ========================================
-- Spring Modulith Event Publication 테이블
-- (Spring Modulith 2.0.1 스키마)
-- ========================================
CREATE TABLE event_publication (
    id UUID PRIMARY KEY,
    event_type VARCHAR(512) NOT NULL,
    listener_id VARCHAR(512) NOT NULL,
    publication_date TIMESTAMP NOT NULL,
    serialized_event TEXT NOT NULL,
    completion_date TIMESTAMP,
    status VARCHAR(20),
    completion_attempts INTEGER,
    last_resubmission_date TIMESTAMP
);

-- Spring Modulith 2.0 권장 인덱스
CREATE INDEX idx_event_publication_by_completion_date ON event_publication(completion_date);
CREATE INDEX idx_event_publication_by_listener_id_and_serialized_event ON event_publication(listener_id, serialized_event);

COMMENT ON TABLE event_publication IS 'Spring Modulith 이벤트 발행 추적 테이블 (Transactional Outbox Pattern)';
