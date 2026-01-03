package com.example.chat.domain.channel;

import java.util.UUID;

import org.springframework.lang.NonNull;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * 채널 ID (Value Object)
 */
@Getter
@EqualsAndHashCode
@ToString
public class ChannelId {

    @NonNull
    private final String value;

    private ChannelId(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("ChannelId cannot be null or blank");
        }
        this.value = value;
    }

    public static ChannelId of(String value) {
        return new ChannelId(value);
    }

    public static ChannelId generate() {
        return new ChannelId(UUID.randomUUID().toString());
    }
}
