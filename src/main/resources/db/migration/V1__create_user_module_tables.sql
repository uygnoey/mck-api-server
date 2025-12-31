-- V1__create_user_module_tables.sql
-- User Module 테이블 생성
-- 작성일: 2025-12-30

-- ========================================
-- 1. user_grades (사용자 등급) - 동적 관리
-- ========================================
CREATE TABLE user_grades (
    id BIGSERIAL PRIMARY KEY,

    -- 등급 정보
    code VARCHAR(30) NOT NULL UNIQUE,
    name VARCHAR(50) NOT NULL,
    role_name VARCHAR(50) NOT NULL UNIQUE,

    -- 권한 레벨 (높을수록 상위 등급)
    permission_level INTEGER NOT NULL,

    -- 특성 플래그
    is_executive BOOLEAN NOT NULL DEFAULT FALSE,
    is_staff BOOLEAN NOT NULL DEFAULT FALSE,
    is_member BOOLEAN NOT NULL DEFAULT FALSE,
    requires_annual_fee BOOLEAN NOT NULL DEFAULT TRUE,
    is_system_grade BOOLEAN NOT NULL DEFAULT FALSE,

    -- 표시 설정
    display_suffix VARCHAR(20),
    display_order INTEGER NOT NULL,

    -- 관리
    created_by BIGINT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes
CREATE INDEX idx_user_grades_code ON user_grades(code);
CREATE INDEX idx_user_grades_level ON user_grades(permission_level DESC);
CREATE INDEX idx_user_grades_active ON user_grades(is_active);

-- Comments
COMMENT ON TABLE user_grades IS '사용자 등급 (동적 관리 가능)';
COMMENT ON COLUMN user_grades.code IS '등급 코드: DEVELOPER, ADVISOR, PRESIDENT, VICE_PRESIDENT, DIRECTOR, REGULAR, ASSOCIATE, PARTNER 등';
COMMENT ON COLUMN user_grades.permission_level IS '권한 레벨 (높을수록 상위)';
COMMENT ON COLUMN user_grades.is_system_grade IS '시스템 등급 여부 (TRUE면 삭제 불가)';
COMMENT ON COLUMN user_grades.requires_annual_fee IS '연회비 필요 여부 (REGULAR만 TRUE)';

-- 기본 등급 데이터 삽입
INSERT INTO user_grades (code, name, role_name, permission_level, is_executive, is_staff, is_member, requires_annual_fee, is_system_grade, display_suffix, display_order) VALUES
    ('DEVELOPER', '개발자', 'ROLE_DEVELOPER', 10, FALSE, TRUE, FALSE, FALSE, TRUE, NULL, 1),
    ('ADVISOR', '고문', 'ROLE_ADVISOR', 9, FALSE, TRUE, FALSE, FALSE, FALSE, '(고문)', 2),
    ('PRESIDENT', '회장', 'ROLE_PRESIDENT', 8, TRUE, TRUE, FALSE, FALSE, FALSE, '(회장)', 3),
    ('VICE_PRESIDENT', '부회장', 'ROLE_VICE_PRESIDENT', 7, TRUE, TRUE, FALSE, FALSE, FALSE, '(부회장)', 4),
    ('DIRECTOR', '이사', 'ROLE_DIRECTOR', 6, TRUE, TRUE, FALSE, FALSE, FALSE, NULL, 5),
    ('REGULAR', '정회원', 'ROLE_REGULAR', 5, FALSE, FALSE, TRUE, TRUE, FALSE, NULL, 6),
    ('ASSOCIATE', '준회원', 'ROLE_ASSOCIATE', 3, FALSE, FALSE, TRUE, FALSE, TRUE, '(준회원)', 7),
    ('PARTNER', '파트너사', 'ROLE_PARTNER', 2, FALSE, FALSE, FALSE, FALSE, FALSE, '(파트너)', 8);

-- ========================================
-- 2. users (사용자)
-- ========================================
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,

    -- 정회원 관련 필드 (불변)
    member_number INTEGER UNIQUE,
    real_name VARCHAR(50) NOT NULL,

    -- 연락처 정보
    email VARCHAR(255) NOT NULL UNIQUE,
    phone_number VARCHAR(20),
    profile_image_url VARCHAR(500),

    -- 인증 정보 (자체 로그인)
    password VARCHAR(255),
    password_changed_at TIMESTAMP,

    -- 등급 관련
    grade_id BIGINT NOT NULL REFERENCES user_grades(id),
    associate_status VARCHAR(20),
    director_part_id BIGINT,
    partner_company_name VARCHAR(100),

    -- 연회비 면제 관련
    exemption_type VARCHAR(20) NOT NULL DEFAULT 'NONE',
    exemption_reason VARCHAR(200),
    exemption_year INTEGER,

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
CREATE SEQUENCE IF NOT EXISTS member_number_seq START WITH 650;

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

-- ========================================
-- 3. oauth_accounts (OAuth 계정) - 다중 연결 지원
-- ========================================
CREATE TABLE oauth_accounts (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    provider VARCHAR(20) NOT NULL,
    provider_id VARCHAR(255) NOT NULL,
    email VARCHAR(255),
    access_token TEXT,
    refresh_token TEXT,
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

-- ========================================
-- 4. passkey_credentials (Passkey 인증)
-- ========================================
CREATE TABLE passkey_credentials (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    credential_id VARCHAR(500) NOT NULL UNIQUE,
    public_key TEXT NOT NULL,
    sign_counter BIGINT NOT NULL DEFAULT 0,
    transports VARCHAR(255),
    device_name VARCHAR(100),
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

-- Note: member_vehicles 테이블은 V2에서 common_codes와 함께 생성됩니다.
