package com.example.chat.push.interfaces.kafka;

/**
 * Kafka로부터 수신하는 알림 이벤트
 */
public record NotificationEvent(
        String targetUserId,
        String title,
        String content,
        String pushType) {
}
