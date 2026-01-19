-- Auth Server Database Schema

-- Principal (사용자/서비스 계정)
CREATE TABLE IF NOT EXISTS principals (
    id UUID PRIMARY KEY,
    identifier VARCHAR(255) NOT NULL UNIQUE,  -- email, username
    type VARCHAR(50) NOT NULL,                -- USER, SERVICE_ACCOUNT
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_principals_identifier ON principals(identifier);
CREATE INDEX idx_principals_active ON principals(active);

-- Credential (자격증명)
CREATE TABLE IF NOT EXISTS credentials (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    principal_id UUID NOT NULL,
    type VARCHAR(50) NOT NULL,                -- PASSWORD, SOCIAL, PASSKEY, OTP
    data TEXT NOT NULL,                       -- JSON 형태로 저장
    verified BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_credentials_principal FOREIGN KEY (principal_id) REFERENCES principals(id) ON DELETE CASCADE,
    CONSTRAINT uq_principal_credential_type UNIQUE (principal_id, type)
);

CREATE INDEX idx_credentials_principal_id ON credentials(principal_id);
CREATE INDEX idx_credentials_type ON credentials(type);

-- 테스트 데이터 삽입 (개발용)
-- 비밀번호: "password123" (BCrypt 해시)
INSERT INTO principals (id, identifier, type, active)
VALUES 
    ('550e8400-e29b-41d4-a716-446655440001', 'user@example.com', 'USER', true),
    ('550e8400-e29b-41d4-a716-446655440002', 'admin@example.com', 'USER', true)
ON CONFLICT (identifier) DO NOTHING;

-- 비밀번호 자격증명 (BCrypt: password123)
INSERT INTO credentials (principal_id, type, data, verified)
VALUES 
    ('550e8400-e29b-41d4-a716-446655440001', 'PASSWORD', 
     '{"hashedPassword":"$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy"}', true),
    ('550e8400-e29b-41d4-a716-446655440002', 'PASSWORD', 
     '{"hashedPassword":"$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy"}', true)
ON CONFLICT (principal_id, type) DO NOTHING;

COMMENT ON TABLE principals IS '인증 주체 (사용자, 서비스 계정)';
COMMENT ON TABLE credentials IS '자격증명 (비밀번호, OAuth, Passkey, OTP)';
COMMENT ON COLUMN credentials.data IS 'JSON 형태로 자격증명 상세 정보 저장';
