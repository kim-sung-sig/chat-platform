package com.example.chat.scheduled.event;

import java.time.ZonedDateTime;

/**
 * 예약 메시지가 등록되었을 때 발행되는 도메인 이벤트
 */
public record MessageScheduledEvent(
        String scheduledMessageId,
        String channelId,
        String senderId,
        ZonedDateTime scheduledAt,
        ZonedDateTime occurredAt
) {
    public static MessageScheduledEvent of(String id, String channelId, String senderId, ZonedDateTime scheduledAt) {
        return new MessageScheduledEvent(id, channelId, senderId, scheduledAt, ZonedDateTime.now());
    }
}
