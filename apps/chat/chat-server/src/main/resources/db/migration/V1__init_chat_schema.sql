CREATE TABLE ms_chat_room
(
    id         BIGSERIAL PRIMARY KEY,
    room_type  varchar(50)              not null,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE TABLE ms_user_chat_room
(
    id           BIGSERIAL PRIMARY KEY,
    chat_room_id BIGINT                   NOT NULL,
    user_id      BIGINT                   NOT NULL,
    room_name    VARCHAR(255),
    joined_at    TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_chat_room FOREIGN KEY (chat_room_id) REFERENCES ms_chat_room (id),
    CONSTRAINT uq_user_chat_room UNIQUE (user_id, chat_room_id)
);

CREATE TABLE ms_message
(
    id         BIGSERIAL PRIMARY KEY,
    room_id    BIGINT                   NOT NULL,
    sender_id  BIGINT                   NOT NULL,
    type       VARCHAR(50)              NOT NULL,
    details    JSONB,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_message_chat_room FOREIGN KEY (room_id) REFERENCES ms_chat_room (id)
);

CREATE TABLE ms_message_read
(
    id         BIGSERIAL PRIMARY KEY,
    message_id BIGINT                   NOT NULL,
    user_id    BIGINT                   NOT NULL,
    read_at    TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_message_read_message FOREIGN KEY (message_id) REFERENCES ms_message (id),
    CONSTRAINT uq_user_message_read UNIQUE (user_id, message_id)
);

CREATE INDEX idx_user_chat_room_chat_room_id ON ms_user_chat_room (chat_room_id);
CREATE INDEX idx_message_room_id_created_at ON ms_message (room_id, created_at DESC);
CREATE INDEX idx_message_read_user_id ON ms_message_read (user_id);
CREATE INDEX idx_message_read_message_id ON ms_message_read (message_id);

