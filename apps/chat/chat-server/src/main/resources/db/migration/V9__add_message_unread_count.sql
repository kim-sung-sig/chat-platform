-- V9: 메시지별 읽지 않은 멤버 수 컬럼 추가 (Phase 2 - 그룹 채팅 읽음 기능)
--
-- 설계 전략: Last-Read Cursor + Per-Message Unread Counter
--
-- 메시지 발송 시:  unread_count = 채널 멤버 수 - 1 (발신자 제외)
-- 멤버가 읽을 때: chat_channel_metadata.last_read_message_id 갱신 (커서 방식)
--                Kafka Consumer가 해당 커서 이전 메시지들의 unread_count 일괄 감소
--
-- KakaoTalk 스타일: 메시지 옆 숫자 표시 → chat_messages.unread_count 직접 사용
-- 대규모 채널: 단일 컬럼 카운터 방식으로 쓰기 폭발 방지
--              (message_read_receipts 별도 테이블 대비 N배 쓰기 절감)

ALTER TABLE chat_messages
    ADD COLUMN IF NOT EXISTS unread_count INTEGER NOT NULL DEFAULT 0;

-- 읽음 카운터 조회 최적화 인덱스 (채널 내 미읽음 메시지 집계)
CREATE INDEX IF NOT EXISTS idx_chat_message_unread
    ON chat_messages (channel_id, unread_count)
    WHERE unread_count > 0;

COMMENT ON COLUMN chat_messages.unread_count
    IS '채널 멤버 중 이 메시지를 아직 읽지 않은 사람 수. 발송 시 memberCount-1로 초기화, 각 멤버가 읽을 때마다 -1 감소 (Kafka 비동기 처리)';
