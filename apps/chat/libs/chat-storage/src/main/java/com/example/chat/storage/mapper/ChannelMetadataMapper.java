package com.example.chat.storage.mapper;

import org.springframework.stereotype.Component;

import com.example.chat.domain.channel.ChannelId;
import com.example.chat.domain.channel.metadata.ChannelMetadata;
import com.example.chat.domain.channel.metadata.ChannelMetadataId;
import com.example.chat.domain.message.MessageId;
import com.example.chat.domain.user.UserId;
import com.example.chat.storage.entity.ChatChannelMetadataEntity;

/**
 * ChannelMetadata Domain ↔ ChatChannelMetadataEntity 변환
 */
@Component
public class ChannelMetadataMapper {

    /**
     * Domain → Entity 변환
     */
    public ChatChannelMetadataEntity toEntity(ChannelMetadata metadata) {
        return ChatChannelMetadataEntity.builder()
                .id(metadata.getId().value())
                .channelId(metadata.getChannelId().value())
                .userId(metadata.getUserId().value())
                .notificationEnabled(metadata.isNotificationEnabled())
                .favorite(metadata.isFavorite())
                .pinned(metadata.isPinned())
                .lastReadMessageId(
                        metadata.getLastReadMessageId() != null ? metadata.getLastReadMessageId().value() : null)
                .lastReadAt(metadata.getLastReadAt())
                .unreadCount(metadata.getUnreadCount())
                .lastActivityAt(metadata.getLastActivityAt())
                .createdAt(metadata.getCreatedAt())
                .updatedAt(metadata.getUpdatedAt())
                .build();
    }

    /**
     * Entity → Domain 변환
     */
    public ChannelMetadata toDomain(ChatChannelMetadataEntity entity) {
        return ChannelMetadata.fromStorage(
                ChannelMetadataId.of(entity.getId()),
                ChannelId.of(entity.getChannelId()),
                UserId.of(entity.getUserId()),
                entity.getCreatedAt(),
                entity.isNotificationEnabled(),
                entity.isFavorite(),
                entity.isPinned(),
                entity.getLastReadMessageId() != null ? MessageId.of(entity.getLastReadMessageId()) : null,
                entity.getLastReadAt(),
                entity.getUnreadCount(),
                entity.getLastActivityAt() != null ? entity.getLastActivityAt() : entity.getCreatedAt(),
                entity.getUpdatedAt());
    }
}
