package com.example.chat.domain.friendship;

import org.springframework.lang.NonNull;

import java.util.UUID;

/**
 * 친구 관계 ID (Value Object)
 */
public record FriendshipId(String value) {
    public FriendshipId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("FriendshipId cannot be blank");
        }
    }

    public static FriendshipId of(String value) {
        return new FriendshipId(value);
    }

    public static FriendshipId generate() {
        return new FriendshipId(UUID.randomUUID().toString());
    }
}
