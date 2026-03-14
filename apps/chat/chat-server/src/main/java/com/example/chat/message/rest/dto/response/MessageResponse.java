package com.example.chat.message.rest.dto.response;

import java.time.Instant;

import com.example.chat.common.core.enums.MessageStatus;
import com.example.chat.common.core.enums.MessageType;
import com.example.chat.storage.domain.entity.ChatMessageEntity;

/**
 * 메시지 응답 DTO
 *
 * Phase 4: Domain POJO 의존 제거 - fromEntity() 단일 팩토리 메서드 사용
 */
public record MessageResponse(
        String id,
        String channelId,
        String senderId,
        MessageType messageType,
        String content,
        MessageStatus status,
        int unreadCount,
        Instant createdAt,
        Instant sentAt,
        Instant deliveredAt,
        Instant readAt) {

    public static MessageResponse fromEntity(ChatMessageEntity entity) {
        String content = switch (entity.getMessageType()) {
            case TEXT, SYSTEM -> entity.getContentText() != null ? entity.getContentText() : "";
            case IMAGE        -> "[Image] " + entity.getContentFileName();
            case FILE, VIDEO, AUDIO -> "[File] " + entity.getContentFileName();
        };
        return new MessageResponse(
                entity.getId(),
                entity.getChannelId(),
                entity.getSenderId(),
                entity.getMessageType(),
                content,
                entity.getMessageStatus(),
                entity.getUnreadCount(),
                entity.getCreatedAt(),
                entity.getSentAt(),
                entity.getDeliveredAt(),
                entity.getReadAt());
    }
}
