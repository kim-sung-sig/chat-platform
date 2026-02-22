package com.example.chat.domain.message;

import java.util.UUID;

/**
 * 메시지 ID (Value Object)
 */
public record MessageId(String value) {
    public MessageId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("MessageId cannot be null or blank");
        }
    }

    public static MessageId of(String value) {
        return new MessageId(value);
    }

    public static MessageId generate() {
        return new MessageId(UUID.randomUUID().toString());
    }

    public String getValue() {
        return value;
    }
}
