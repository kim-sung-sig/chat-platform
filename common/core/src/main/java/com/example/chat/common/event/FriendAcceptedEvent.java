package com.example.chat.common.event;

import java.time.Instant;

/**
 * 친구 수락 이벤트
 */
public record FriendAcceptedEvent(
        String userId,
        String friendId,
        Instant occurredAt) {
}
