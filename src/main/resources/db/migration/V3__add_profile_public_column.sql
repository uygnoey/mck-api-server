-- Add is_profile_public column to users table
-- 사용자 프로필 공개 여부 컬럼 추가

ALTER TABLE users
    ADD COLUMN is_profile_public BOOLEAN NOT NULL DEFAULT TRUE;

COMMENT ON COLUMN users.is_profile_public IS '프로필 공개 여부 (기본값: 공개)';
