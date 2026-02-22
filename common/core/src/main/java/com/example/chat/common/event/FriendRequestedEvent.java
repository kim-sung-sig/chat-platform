package com.example.chat.common.event;

import java.time.Instant;

/**
 * 친구 요청 이벤트
 */
public record FriendRequestedEvent(
        String requesterId,
        String targetId,
        Instant occurredAt) {
}
