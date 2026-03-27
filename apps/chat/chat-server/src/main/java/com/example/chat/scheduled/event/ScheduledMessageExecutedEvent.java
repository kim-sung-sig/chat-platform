package com.example.chat.scheduled.event;

import java.time.ZonedDateTime;

/**
 * 예약 메시지가 성공적으로 발송되었을 때 발행되는 도메인 이벤트
 */
public record ScheduledMessageExecutedEvent(
        String scheduledMessageId,
        String channelId,
        String senderId,
        ZonedDateTime executedAt,
        ZonedDateTime occurredAt
) {
    public static ScheduledMessageExecutedEvent of(String id, String channelId, String senderId, ZonedDateTime executedAt) {
        return new ScheduledMessageExecutedEvent(id, channelId, senderId, executedAt, ZonedDateTime.now());
    }
}
