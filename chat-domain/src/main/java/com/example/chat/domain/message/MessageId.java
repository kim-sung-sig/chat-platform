package com.example.chat.domain.message;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.UUID;

/**
 * 메시지 ID (Value Object)
 */
@Getter
@EqualsAndHashCode
@ToString
public class MessageId {
    private final String value;

    private MessageId(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("MessageId cannot be null or blank");
        }
        this.value = value;
    }

    public static MessageId of(String value) {
        return new MessageId(value);
    }

    public static MessageId generate() {
        return new MessageId(UUID.randomUUID().toString());
    }
}
