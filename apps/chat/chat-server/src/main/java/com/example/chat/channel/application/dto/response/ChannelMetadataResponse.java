package com.example.chat.channel.application.dto.response;

import java.time.Instant;

import com.example.chat.storage.entity.ChatChannelMetadataEntity;

/**
 * 채팅방 메타데이터 Response DTO
 *
 * Phase 4: Domain POJO 의존 제거 - fromEntity() 단일 팩토리 메서드 사용
 */
public record ChannelMetadataResponse(
        String id,
        String channelId,
        String userId,
        boolean notificationEnabled,
        boolean favorite,
        boolean pinned,
        String lastReadMessageId,
        Instant lastReadAt,
        int unreadCount,
        Instant lastActivityAt,
        Instant createdAt,
        Instant updatedAt) {

    public static ChannelMetadataResponse fromEntity(ChatChannelMetadataEntity entity) {
        return new ChannelMetadataResponse(
                entity.getId(),
                entity.getChannelId(),
                entity.getUserId(),
                entity.isNotificationEnabled(),
                entity.isFavorite(),
                entity.isPinned(),
                entity.getLastReadMessageId(),
                entity.getLastReadAt(),
                entity.getUnreadCount(),
                entity.getLastActivityAt(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }
}
