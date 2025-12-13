-- Chat Platform Schema Migration V6
-- Version: 6.0
-- Description: payload를 JSONB로 변경

-- ========================================
-- 1. ms_outbox_event 테이블 payload 타입 변경
-- ========================================

-- payload를 TEXT에서 JSONB로 변경 (JSON 데이터 저장 및 쿼리 최적화)
ALTER TABLE ms_outbox_event ALTER COLUMN payload TYPE JSONB USING payload::jsonb;
