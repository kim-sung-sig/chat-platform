package com.example.chat.push.infrastructure.kafka;

/**
 * 푸시 처리 결과 이벤트
 */
public record PushResultEvent(
        Long pushMessageId,
        String targetUserId,
        String status,
        String errorMessage) {

    public PushResultEvent(Long pushMessageId, String targetUserId, String status) {
        this(pushMessageId, targetUserId, status, null);
    }
}
