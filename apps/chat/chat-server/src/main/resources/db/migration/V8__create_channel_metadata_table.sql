-- V8: Create channel_metadata table
-- 채팅방 메타데이터 테이블 생성

CREATE TABLE chat_channel_metadata
(
    id                   VARCHAR(36) PRIMARY KEY,
    channel_id           VARCHAR(36)              NOT NULL,
    user_id              VARCHAR(36)              NOT NULL,
    notification_enabled BOOLEAN                  NOT NULL DEFAULT TRUE,
    favorite             BOOLEAN                  NOT NULL DEFAULT FALSE,
    pinned               BOOLEAN                  NOT NULL DEFAULT FALSE,
    last_read_message_id VARCHAR(36),
    last_read_at         TIMESTAMP WITH TIME ZONE,
    unread_count         INTEGER                  NOT NULL DEFAULT 0,
    last_activity_at     TIMESTAMP WITH TIME ZONE,
    created_at           TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at           TIMESTAMP WITH TIME ZONE NOT NULL
);

-- 인덱스 생성
CREATE INDEX idx_channel_metadata_user_id ON chat_channel_metadata (user_id);
CREATE INDEX idx_channel_metadata_channel_id ON chat_channel_metadata (channel_id);
CREATE INDEX idx_channel_metadata_user_activity ON chat_channel_metadata (user_id, last_activity_at DESC);
CREATE INDEX idx_channel_metadata_user_favorite ON chat_channel_metadata (user_id, favorite);
CREATE INDEX idx_channel_metadata_user_pinned ON chat_channel_metadata (user_id, pinned);

-- 유니크 제약조건 (한 채널에 대한 사용자의 메타데이터는 하나만)
CREATE UNIQUE INDEX uk_channel_metadata_channel_user ON chat_channel_metadata (channel_id, user_id);

-- 외래키 제약조건 (선택사항 - 필요 시 활성화)
-- ALTER TABLE chat_channel_metadata ADD CONSTRAINT fk_metadata_channel
--     FOREIGN KEY (channel_id) REFERENCES chat_channels(id) ON DELETE CASCADE;

-- 코멘트 추가
COMMENT
ON TABLE chat_channel_metadata IS '사용자별 채팅방 메타데이터 (설정, 읽기 상태)';
COMMENT
ON COLUMN chat_channel_metadata.id IS '메타데이터 ID (UUID)';
COMMENT
ON COLUMN chat_channel_metadata.channel_id IS '채널 ID';
COMMENT
ON COLUMN chat_channel_metadata.user_id IS '사용자 ID';
COMMENT
ON COLUMN chat_channel_metadata.notification_enabled IS '알림 활성화 여부';
COMMENT
ON COLUMN chat_channel_metadata.favorite IS '즐겨찾기 여부';
COMMENT
ON COLUMN chat_channel_metadata.pinned IS '상단 고정 여부';
COMMENT
ON COLUMN chat_channel_metadata.last_read_message_id IS '마지막 읽은 메시지 ID';
COMMENT
ON COLUMN chat_channel_metadata.last_read_at IS '마지막 읽은 시간';
COMMENT
ON COLUMN chat_channel_metadata.unread_count IS '읽지 않은 메시지 수';
COMMENT
ON COLUMN chat_channel_metadata.last_activity_at IS '마지막 활동 시간';
COMMENT
ON COLUMN chat_channel_metadata.created_at IS '생성 시간';
COMMENT
ON COLUMN chat_channel_metadata.updated_at IS '수정 시간';
