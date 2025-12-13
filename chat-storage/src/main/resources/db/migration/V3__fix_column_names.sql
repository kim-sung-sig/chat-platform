-- Chat Platform Schema Migration V3
-- Version: 3.0
-- Description: Entity와 일치하도록 컬럼명 수정

-- ========================================
-- 1. chat_messages 테이블 컬럼명 수정
-- ========================================

-- status -> message_status
ALTER TABLE chat_messages RENAME COLUMN status TO message_status;

-- text_content -> content_text
ALTER TABLE chat_messages RENAME COLUMN text_content TO content_text;

-- media_url -> content_media_url
ALTER TABLE chat_messages RENAME COLUMN media_url TO content_media_url;

-- file_name -> content_file_name
ALTER TABLE chat_messages RENAME COLUMN file_name TO content_file_name;

-- file_size -> content_file_size
ALTER TABLE chat_messages RENAME COLUMN file_size TO content_file_size;

-- mime_type -> content_mime_type
ALTER TABLE chat_messages RENAME COLUMN mime_type TO content_mime_type;

-- ========================================
-- 2. users 테이블 username 길이 수정
-- ========================================
ALTER TABLE users ALTER COLUMN username TYPE VARCHAR(50);

-- ========================================
-- 3. 인덱스 이름 수정 (Entity와 일치)
-- ========================================

-- users 테이블 인덱스
DROP INDEX IF EXISTS idx_user_username;
DROP INDEX IF EXISTS idx_user_email;
DROP INDEX IF EXISTS idx_user_status;

CREATE INDEX idx_username ON users(username);
CREATE INDEX idx_email ON users(email);
CREATE INDEX idx_status ON users(status);

-- chat_messages 테이블 인덱스
DROP INDEX IF EXISTS idx_message_channel_created;
DROP INDEX IF EXISTS idx_message_sender;
DROP INDEX IF EXISTS idx_message_status;
DROP INDEX IF EXISTS idx_message_type;

CREATE INDEX idx_chat_message_channel_created ON chat_messages(channel_id, created_at DESC);
CREATE INDEX idx_chat_message_sender ON chat_messages(sender_id);
CREATE INDEX idx_message_status ON chat_messages(message_status);
CREATE INDEX idx_message_type ON chat_messages(message_type);

-- ========================================
-- 4. schedule_rules 테이블 수정
-- ========================================

-- message_id FK 제거 (ScheduleRuleEntity에는 FK가 없음)
ALTER TABLE schedule_rules DROP CONSTRAINT IF EXISTS fk_schedule_message;

-- status -> schedule_status
ALTER TABLE schedule_rules RENAME COLUMN status TO schedule_status;

-- 필요한 컬럼 추가 (ScheduleRuleEntity 기준)
ALTER TABLE schedule_rules ADD COLUMN IF NOT EXISTS channel_id VARCHAR(36);
ALTER TABLE schedule_rules ADD COLUMN IF NOT EXISTS sender_id VARCHAR(36);
ALTER TABLE schedule_rules ADD COLUMN IF NOT EXISTS message_text TEXT;
ALTER TABLE schedule_rules ADD COLUMN IF NOT EXISTS message_media_url VARCHAR(500);
ALTER TABLE schedule_rules ADD COLUMN IF NOT EXISTS message_file_name VARCHAR(255);
ALTER TABLE schedule_rules ADD COLUMN IF NOT EXISTS message_file_size BIGINT;
ALTER TABLE schedule_rules ADD COLUMN IF NOT EXISTS message_mime_type VARCHAR(100);

-- executed_count, last_executed_at -> executed_at
ALTER TABLE schedule_rules DROP COLUMN IF EXISTS executed_count;
ALTER TABLE schedule_rules DROP COLUMN IF EXISTS last_executed_at;
ALTER TABLE schedule_rules ADD COLUMN IF NOT EXISTS executed_at TIMESTAMP;

-- 인덱스 수정
DROP INDEX IF EXISTS idx_schedule_status;
DROP INDEX IF EXISTS idx_schedule_type;
DROP INDEX IF EXISTS idx_schedule_scheduled_at;

CREATE INDEX idx_schedule_type_status ON schedule_rules(schedule_type, schedule_status);
CREATE INDEX idx_schedule_scheduled_at ON schedule_rules(scheduled_at);

-- ========================================
-- 5. outbox_events 테이블명 변경 및 구조 수정
-- ========================================

-- 기존 outbox_events 테이블 삭제 후 재생성 (Entity 기준)
DROP TABLE IF EXISTS outbox_events CASCADE;

CREATE TABLE IF NOT EXISTS ms_outbox_event (
    id BIGSERIAL PRIMARY KEY,
    aggregate_id VARCHAR(128) NOT NULL,
    event_type VARCHAR(64) NOT NULL,
    payload TEXT NOT NULL,
    processed BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_outbox_state_created ON ms_outbox_event(processed, created_at);

-- ========================================
-- 6. message_reads 테이블 수정 (MessageReadEntity 기준)
-- ========================================

-- 테이블명 및 컬럼 확인 (이미 정상)
-- message_reads 테이블은 Entity와 일치함
