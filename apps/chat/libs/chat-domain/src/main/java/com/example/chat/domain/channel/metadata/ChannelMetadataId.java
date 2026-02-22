package com.example.chat.domain.channel.metadata;

import java.util.UUID;

/**
 * 채팅방 메타데이터 ID (Value Object)
 */
public record ChannelMetadataId(String value) {
    public ChannelMetadataId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("ChannelMetadataId cannot be blank");
        }
    }

    public static ChannelMetadataId of(String value) {
        return new ChannelMetadataId(value);
    }

    public static ChannelMetadataId generate() {
        return new ChannelMetadataId(UUID.randomUUID().toString());
    }

    @Override
    public String toString() {
        return value;
    }
}
