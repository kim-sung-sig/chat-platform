-- V7: Create friendships table
-- 친구 관계 테이블 생성

CREATE TABLE chat_friendships
(
    id         VARCHAR(36) PRIMARY KEY,
    user_id    VARCHAR(36)              NOT NULL,
    friend_id  VARCHAR(36)              NOT NULL,
    status     VARCHAR(20)              NOT NULL CHECK (status IN ('PENDING', 'ACCEPTED', 'BLOCKED')),
    nickname   VARCHAR(100),
    favorite   BOOLEAN                  NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

-- 인덱스 생성
CREATE INDEX idx_friendship_user_id ON chat_friendships (user_id);
CREATE INDEX idx_friendship_friend_id ON chat_friendships (friend_id);
CREATE INDEX idx_friendship_user_status ON chat_friendships (user_id, status);

-- 유니크 제약조건 (한 쌍의 사용자는 한 번만 친구 관계를 맺을 수 있음)
CREATE UNIQUE INDEX uk_friendship ON chat_friendships (user_id, friend_id);

-- 코멘트 추가
COMMENT
ON TABLE chat_friendships IS '사용자 간 친구 관계 테이블';
COMMENT
ON COLUMN chat_friendships.id IS '친구 관계 ID (UUID)';
COMMENT
ON COLUMN chat_friendships.user_id IS '요청자/소유자 사용자 ID';
COMMENT
ON COLUMN chat_friendships.friend_id IS '친구 사용자 ID';
COMMENT
ON COLUMN chat_friendships.status IS '친구 관계 상태 (PENDING: 대기중, ACCEPTED: 수락됨, BLOCKED: 차단됨)';
COMMENT
ON COLUMN chat_friendships.nickname IS '친구 별칭 (선택)';
COMMENT
ON COLUMN chat_friendships.favorite IS '즐겨찾기 여부';
COMMENT
ON COLUMN chat_friendships.created_at IS '생성 시간';
COMMENT
ON COLUMN chat_friendships.updated_at IS '수정 시간';
