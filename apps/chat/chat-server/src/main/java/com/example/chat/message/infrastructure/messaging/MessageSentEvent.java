package com.example.chat.message.infrastructure.messaging;

import java.time.Instant;

/**
 * 메시지 발송 이벤트 DTO
 * Redis Pub/Sub으로 전송되는 이벤트
 *
 * 불변 객체로 설계 (Value Object)
 */
public record MessageSentEvent(
        String eventType,   // 이벤트 타입: MESSAGE | READ_RECEIPT
        String messageId,   // String (UUID)
        String channelId,   // ChannelId
        String senderId,    // UserId
        String messageType, // MessageType name
        String content,     // 텍스트 내용
        String status,      // MessageStatus name
        int unreadCount,    // 차단 멤버 수 - 1 (기본 미읽음 시작값)
        Instant sentAt) {
}
