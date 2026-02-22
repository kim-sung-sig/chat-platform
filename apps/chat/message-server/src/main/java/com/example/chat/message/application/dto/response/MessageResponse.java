package com.example.chat.message.application.dto.response;

import java.time.Instant;

import com.example.chat.domain.message.MessageStatus;
import com.example.chat.domain.message.MessageType;

/**
 * 메시지 응답 DTO
 *
 * 불변 객체로 설계
 */
public record MessageResponse(
        String id, // MessageId → String
        String channelId, // ChannelId
        String senderId, // UserId
        MessageType messageType,
        String content, // 단순화 (text)
        MessageStatus status,
        Instant createdAt,
        Instant sentAt,
        Instant deliveredAt,
        Instant readAt) {
    public MessageResponse(String id, String channelId, String senderId, MessageType messageType, String content,
            MessageStatus status, Instant createdAt, Instant sentAt) {
        this(id, channelId, senderId, messageType, content, status, createdAt, sentAt, null, null);
    }
}
