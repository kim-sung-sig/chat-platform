package com.example.chat.domain.channel;

import java.util.UUID;

/**
 * 채널 ID (Value Object)
 */
public record ChannelId(String value) {
    public ChannelId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("ChannelId cannot be null or blank");
        }
    }

    public static ChannelId of(String value) {
        return new ChannelId(value);
    }

    public static ChannelId generate() {
        return new ChannelId(UUID.randomUUID().toString());
    }

    public String getValue() {
        return value();
    }
}
