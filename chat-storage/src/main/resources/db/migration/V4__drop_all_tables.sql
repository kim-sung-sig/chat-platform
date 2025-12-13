-- Chat Platform Schema Migration V4
-- Version: 4.0
-- Description: 모든 기존 테이블 제거 (클린 스타트)

-- ========================================
-- Quartz 테이블 제거
-- ========================================
DROP TABLE IF EXISTS QRTZ_FIRED_TRIGGERS CASCADE;
DROP TABLE IF EXISTS QRTZ_PAUSED_TRIGGER_GRPS CASCADE;
DROP TABLE IF EXISTS QRTZ_SCHEDULER_STATE CASCADE;
DROP TABLE IF EXISTS QRTZ_LOCKS CASCADE;
DROP TABLE IF EXISTS QRTZ_SIMPLE_TRIGGERS CASCADE;
DROP TABLE IF EXISTS QRTZ_CRON_TRIGGERS CASCADE;
DROP TABLE IF EXISTS QRTZ_SIMPROP_TRIGGERS CASCADE;
DROP TABLE IF EXISTS QRTZ_BLOB_TRIGGERS CASCADE;
DROP TABLE IF EXISTS QRTZ_TRIGGERS CASCADE;
DROP TABLE IF EXISTS QRTZ_JOB_DETAILS CASCADE;
DROP TABLE IF EXISTS QRTZ_CALENDARS CASCADE;

-- ========================================
-- 애플리케이션 테이블 제거
-- ========================================
DROP TABLE IF EXISTS message_reads CASCADE;
DROP TABLE IF EXISTS ms_message_read CASCADE;
DROP TABLE IF EXISTS schedule_rules CASCADE;
DROP TABLE IF EXISTS chat_messages CASCADE;
DROP TABLE IF EXISTS chat_channel_members CASCADE;
DROP TABLE IF EXISTS chat_channels CASCADE;
DROP TABLE IF EXISTS ms_outbox_event CASCADE;
DROP TABLE IF EXISTS outbox_events CASCADE;
DROP TABLE IF EXISTS users CASCADE;

-- ========================================
-- 구 버전 테이블 제거
-- ========================================
DROP TABLE IF EXISTS ms_message_read CASCADE;
DROP TABLE IF EXISTS ms_message CASCADE;
DROP TABLE IF EXISTS ms_user_chat_room CASCADE;
DROP TABLE IF EXISTS ms_chat_room CASCADE;

-- ========================================
-- 시퀀스 제거
-- ========================================
DROP SEQUENCE IF EXISTS ms_outbox_event_id_seq CASCADE;
