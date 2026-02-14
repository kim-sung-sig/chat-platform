-- Auth Server Database Schema
-- Principal (사용자/서비스 계정)
CREATE TABLE IF NOT EXISTS principals (
    id UUID PRIMARY KEY,
    identifier VARCHAR(255) NOT NULL UNIQUE,
    -- email, username
    type VARCHAR(50) NOT NULL,
    -- USER, SERVICE_ACCOUNT
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_principals_identifier ON principals(identifier);
CREATE INDEX idx_principals_active ON principals(active);
-- Credential (자격증명)
CREATE TABLE IF NOT EXISTS credentials (
    id UUID PRIMARY KEY,
    principal_id UUID NOT NULL,
    credential_type VARCHAR(50) NOT NULL,
    -- PASSWORD, SOCIAL, PASSKEY, OTP
    is_verified BOOLEAN NOT NULL DEFAULT false,
    hashed_password VARCHAR(255),
    -- for PASSWORD
    provider VARCHAR(50),
    -- for SOCIAL
    social_user_id VARCHAR(255),
    -- for SOCIAL
    email VARCHAR(255),
    -- for SOCIAL
    credential_id VARCHAR(255),
    -- for PASSKEY
    public_key TEXT,
    -- for PASSKEY
    authenticator_name VARCHAR(100),
    -- for PASSKEY
    otp_code VARCHAR(10),
    -- for OTP
    delivery_method VARCHAR(50),
    -- for OTP
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_credentials_principal FOREIGN KEY (principal_id) REFERENCES principals(id) ON DELETE CASCADE,
    CONSTRAINT uq_principal_credential_type UNIQUE (principal_id, credential_type)
);
CREATE INDEX idx_credentials_principal_id ON credentials(principal_id);
CREATE INDEX idx_credentials_type ON credentials(credential_type);
-- 테스트 데이터 삽입 (개발용)
-- 비밀번호: "password123" (BCrypt 해시)
INSERT INTO principals (id, identifier, type, active)
VALUES (
        '550e8400-e29b-41d4-a716-446655440001',
        'user@example.com',
        'USER',
        true
    ),
    (
        '550e8400-e29b-41d4-a716-446655440002',
        'admin@example.com',
        'USER',
        true
    ) ON CONFLICT (identifier) DO NOTHING;
-- 비밀번호 자격증명 (BCrypt: password123)
INSERT INTO credentials (
        id,
        principal_id,
        credential_type,
        hashed_password,
        is_verified
    )
VALUES (
        gen_random_uuid(),
        '550e8400-e29b-41d4-a716-446655440001',
        'PASSWORD',
        '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
        true
    ),
    (
        gen_random_uuid(),
        '550e8400-e29b-41d4-a716-446655440002',
        'PASSWORD',
        '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
        true
    ) ON CONFLICT (principal_id, credential_type) DO NOTHING;
COMMENT ON TABLE principals IS '인증 주체 (사용자, 서비스 계정)';
COMMENT ON TABLE credentials IS '자격증명 (비밀번호, OAuth, Passkey, OTP)';