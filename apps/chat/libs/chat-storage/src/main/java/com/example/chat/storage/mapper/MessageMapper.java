package com.example.chat.storage.mapper;

import com.example.chat.domain.channel.ChannelId;
import com.example.chat.domain.message.Message;
import com.example.chat.domain.message.MessageContent;
import com.example.chat.domain.message.MessageId;
import com.example.chat.domain.user.UserId;
import com.example.chat.storage.entity.ChatMessageEntity;
import org.springframework.stereotype.Component;

/**
 * Message Domain ↔ ChatMessageEntity 변환
 */
@Component
public class MessageMapper {

    /**
     * Domain → Entity 변환
     */
    public ChatMessageEntity toEntity(Message message) {
        return ChatMessageEntity.builder()
                .id(message.getId().getValue())
                .channelId(message.getChannelId().getValue())
                .senderId(message.getSenderId().getValue())
                .messageType(message.getType())
                .messageStatus(message.getStatus())
                .contentText(message.getContent().getText())
                .contentMediaUrl(message.getContent().getMediaUrl())
                .contentFileName(message.getContent().getFileName())
                .contentFileSize(message.getContent().getFileSize())
                .contentMimeType(message.getContent().getMimeType())
                .createdAt(message.getCreatedAt())
                .sentAt(message.getSentAt())
                .deliveredAt(message.getDeliveredAt())
                .readAt(message.getReadAt())
                .build();
    }

    /**
     * Entity → Domain 변환
     */
    public Message toDomain(ChatMessageEntity entity) {
        MessageContent content = MessageContent.builder()
                .text(entity.getContentText())
                .mediaUrl(entity.getContentMediaUrl())
                .fileName(entity.getContentFileName())
                .fileSize(entity.getContentFileSize())
                .mimeType(entity.getContentMimeType())
                .build();

        return Message.builder()
                .id(MessageId.of(entity.getId()))
                .channelId(ChannelId.of(entity.getChannelId()))
                .senderId(UserId.of(entity.getSenderId()))
                .content(content)
                .type(entity.getMessageType())
                .status(entity.getMessageStatus())
                .createdAt(entity.getCreatedAt())
                .sentAt(entity.getSentAt())
                .deliveredAt(entity.getDeliveredAt())
                .readAt(entity.getReadAt())
                .build();
    }
}
