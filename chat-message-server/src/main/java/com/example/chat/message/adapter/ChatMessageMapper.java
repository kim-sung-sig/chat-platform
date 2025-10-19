package com.example.chat.message.adapter;

import com.example.chat.common.dto.ChatMessage;
import com.example.chat.common.dto.UserId;
import com.example.chat.storage.entity.ChatMessageEntity;

public final class ChatMessageMapper {

    private ChatMessageMapper() {}

    public static ChatMessageEntity toEntity(ChatMessage dto) {
        if (dto == null) return null;
        ChatMessageEntity.ChatMessageEntityBuilder builder = ChatMessageEntity.builder()
                .channelId(dto.getRoomId())
                .senderId(dto.getSenderId() == null ? null : dto.getSenderId().get())
                .content(dto.getContent())
                .createdAt(dto.getSentAt());
        if (dto.getId() != null) builder.id(dto.getId());
        return builder.build();
    }

    public static ChatMessage toDto(ChatMessageEntity entity) {
        if (entity == null) return null;
        return ChatMessage.builder()
                .id(entity.getId())
                .roomId(entity.getChannelId())
                .senderId(entity.getSenderId() == null ? null : UserId.of(entity.getSenderId()))
                .content(entity.getContent())
                .sentAt(entity.getCreatedAt())
                .build();
    }
}