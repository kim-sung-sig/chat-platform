-- V10: schedule_rules 테이블 정비
-- 1. message_id NOT NULL → nullable (예약 생성 시점에는 messageId 없음)
-- 2. retry_count 컬럼 추가
-- 3. scheduled_at 인덱스 보완 (channel_id + sender_id 복합 조회 최적화)

ALTER TABLE schedule_rules
    ALTER COLUMN message_id DROP NOT NULL;

ALTER TABLE schedule_rules
    ADD COLUMN IF NOT EXISTS retry_count INTEGER NOT NULL DEFAULT 0;

CREATE INDEX IF NOT EXISTS idx_schedule_channel_sender
    ON schedule_rules (channel_id, sender_id, schedule_status, scheduled_at);
