package com.example.chat.system.application.dto.response;

import java.time.Instant;

import com.example.chat.domain.channel.metadata.ChannelMetadata;

/**
 * 채팅방 메타데이터 Response DTO
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
    public static ChannelMetadataResponse from(ChannelMetadata metadata) {
        return new ChannelMetadataResponse(
                metadata.getId().value(),
                metadata.getChannelId().value(),
                metadata.getUserId().value(),
                metadata.isNotificationEnabled(),
                metadata.isFavorite(),
                metadata.isPinned(),
                metadata.getLastReadMessageId() != null ? metadata.getLastReadMessageId().value() : null,
                metadata.getLastReadAt(),
                metadata.getUnreadCount(),
                metadata.getLastActivityAt(),
                metadata.getCreatedAt(),
                metadata.getUpdatedAt());
    }
}
