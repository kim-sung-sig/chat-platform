-- Chat System Server Database Schema
-- PostgreSQL 16

-- 채널 테이블
CREATE TABLE IF NOT EXISTS channels (
    channel_id BIGSERIAL PRIMARY KEY,
    channel_name VARCHAR(100) NOT NULL,
    channel_type VARCHAR(50) NOT NULL,
    description VARCHAR(500),
    is_active BOOLEAN NOT NULL DEFAULT true,
    owner_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_channels_is_active ON channels(is_active);
CREATE INDEX idx_channels_owner_id ON channels(owner_id);
CREATE INDEX idx_channels_channel_type ON channels(channel_type);

COMMENT ON TABLE channels IS '채널 - 메시지를 발행할 권한을 가진 주체';
COMMENT ON COLUMN channels.channel_type IS '채널 타입 (MARKETING, NOTICE, EVENT 등)';

-- 고객 테이블
CREATE TABLE IF NOT EXISTS customers (
    customer_id BIGSERIAL PRIMARY KEY,
    customer_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone_number VARCHAR(20),
    is_active BOOLEAN NOT NULL DEFAULT true,
    is_marketing_agreed BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_customers_email ON customers(email);
CREATE INDEX idx_customers_phone_number ON customers(phone_number);
CREATE INDEX idx_customers_is_active ON customers(is_active);
CREATE INDEX idx_customers_is_marketing_agreed ON customers(is_marketing_agreed);

COMMENT ON TABLE customers IS '고객 - 채널 메시지 수신에 동의한 고객';

-- 채널 구독 테이블
CREATE TABLE IF NOT EXISTS channel_subscriptions (
    subscription_id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    channel_id BIGINT NOT NULL,
    is_subscribed BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_subscription_customer FOREIGN KEY (customer_id) REFERENCES customers(customer_id),
    CONSTRAINT fk_subscription_channel FOREIGN KEY (channel_id) REFERENCES channels(channel_id),
    CONSTRAINT uk_customer_channel UNIQUE (customer_id, channel_id)
);

CREATE INDEX idx_channel_subscriptions_customer_id ON channel_subscriptions(customer_id);
CREATE INDEX idx_channel_subscriptions_channel_id ON channel_subscriptions(channel_id);

COMMENT ON TABLE channel_subscriptions IS '채널 구독 - 고객과 채널의 다대다 관계';

-- 메시지 테이블
CREATE TABLE IF NOT EXISTS messages (
    message_id BIGSERIAL PRIMARY KEY,
    channel_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    message_type VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_by BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_message_channel FOREIGN KEY (channel_id) REFERENCES channels(channel_id)
);

CREATE INDEX idx_messages_channel_id ON messages(channel_id);
CREATE INDEX idx_messages_status ON messages(status);
CREATE INDEX idx_messages_created_by ON messages(created_by);
CREATE INDEX idx_messages_created_at ON messages(created_at);

COMMENT ON TABLE messages IS '메시지 - 채널 담당자가 작성한 발행 콘텐츠';
COMMENT ON COLUMN messages.message_type IS '메시지 타입 (TEXT, IMAGE, MIXED)';
COMMENT ON COLUMN messages.status IS '메시지 상태 (DRAFT, SCHEDULED, PUBLISHED, CANCELLED)';

-- 스케줄 규칙 테이블
CREATE TABLE IF NOT EXISTS schedule_rules (
    schedule_id BIGSERIAL PRIMARY KEY,
    message_id BIGINT NOT NULL,
    schedule_type VARCHAR(20) NOT NULL,
    cron_expression VARCHAR(100),
    execution_time TIMESTAMP,
    next_execution_time TIMESTAMP,
    last_execution_time TIMESTAMP,
    is_active BOOLEAN NOT NULL DEFAULT true,
    job_name VARCHAR(200) UNIQUE,
    job_group VARCHAR(200),
    execution_count INTEGER NOT NULL DEFAULT 0,
    max_execution_count INTEGER,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_schedule_message FOREIGN KEY (message_id) REFERENCES messages(message_id)
);

CREATE INDEX idx_schedule_rules_message_id ON schedule_rules(message_id);
CREATE INDEX idx_schedule_rules_is_active ON schedule_rules(is_active);
CREATE INDEX idx_schedule_rules_next_execution_time ON schedule_rules(next_execution_time);
CREATE INDEX idx_schedule_rules_schedule_type ON schedule_rules(schedule_type);

COMMENT ON TABLE schedule_rules IS '스케줄 규칙 - 주기적/단발성 메시지 발행 규칙';
COMMENT ON COLUMN schedule_rules.schedule_type IS '스케줄 타입 (ONCE, RECURRING)';

-- 메시지 발행 이력 테이블
CREATE TABLE IF NOT EXISTS message_histories (
    history_id BIGSERIAL PRIMARY KEY,
    message_id BIGINT NOT NULL,
    customer_id BIGINT NOT NULL,
    publish_status VARCHAR(20) NOT NULL,
    published_at TIMESTAMP,
    error_message VARCHAR(1000),
    retry_count INTEGER NOT NULL DEFAULT 0,
    schedule_rule_id BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_history_message FOREIGN KEY (message_id) REFERENCES messages(message_id),
    CONSTRAINT fk_history_customer FOREIGN KEY (customer_id) REFERENCES customers(customer_id)
);

CREATE INDEX idx_message_histories_message_id ON message_histories(message_id);
CREATE INDEX idx_message_histories_customer_id ON message_histories(customer_id);
CREATE INDEX idx_message_histories_publish_status ON message_histories(publish_status);
CREATE INDEX idx_message_histories_published_at ON message_histories(published_at);
CREATE INDEX idx_message_histories_schedule_rule_id ON message_histories(schedule_rule_id);
CREATE INDEX idx_message_histories_history_id_desc ON message_histories(history_id DESC);

COMMENT ON TABLE message_histories IS '메시지 발행 이력 - 실제 발행된 메시지 및 발행 상태';
COMMENT ON COLUMN message_histories.publish_status IS '발행 상태 (PENDING, SUCCESS, FAILED, RETRY)';

-- 샘플 데이터 (테스트용)
INSERT INTO channels (channel_name, channel_type, description, is_active, owner_id) VALUES
('마케팅 채널', 'MARKETING', '마케팅 메시지 발송', true, 1),
('공지 채널', 'NOTICE', '중요 공지사항 발송', true, 1),
('이벤트 채널', 'EVENT', '이벤트 메시지 발송', true, 2);

INSERT INTO customers (customer_name, email, phone_number, is_active, is_marketing_agreed) VALUES
('홍길동', 'hong@example.com', '010-1234-5678', true, true),
('김철수', 'kim@example.com', '010-2345-6789', true, true),
('이영희', 'lee@example.com', '010-3456-7890', true, false),
('박민수', 'park@example.com', '010-4567-8901', true, true);

INSERT INTO channel_subscriptions (customer_id, channel_id, is_subscribed) VALUES
(1, 1, true),
(1, 2, true),
(2, 1, true),
(2, 3, true),
(3, 2, true),
(4, 1, true),
(4, 2, true),
(4, 3, true);

COMMIT;