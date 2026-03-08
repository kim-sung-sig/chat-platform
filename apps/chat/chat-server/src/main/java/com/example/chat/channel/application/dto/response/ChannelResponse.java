package com.example.chat.channel.application.dto.response;

import com.example.chat.common.core.enums.ChannelType;
import com.example.chat.storage.entity.ChatChannelEntity;

import java.time.Instant;

/**
 * 채널 기본 응답 DTO
 */
public record ChannelResponse(
        String id,
        String name,
        String description,
        ChannelType type,
        String ownerId,
        boolean active,
        Instant createdAt) {

    public static ChannelResponse fromEntity(ChatChannelEntity entity) {
        return new ChannelResponse(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.getChannelType(),
                entity.getOwnerId(),
                entity.isActive(),
                entity.getCreatedAt());
    }
}
