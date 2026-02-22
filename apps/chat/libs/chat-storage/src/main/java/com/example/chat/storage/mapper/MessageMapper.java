package com.example.chat.storage.mapper;

import org.springframework.stereotype.Component;

import com.example.chat.domain.channel.ChannelId;
import com.example.chat.domain.message.Message;
import com.example.chat.domain.message.MessageContent;
import com.example.chat.domain.message.MessageId;
import com.example.chat.domain.user.UserId;
import com.example.chat.storage.entity.ChatMessageEntity;

/**
 * Message Domain ↔ ChatMessageEntity 변환
 */
@Component
public class MessageMapper {

    /**
     * Domain → Entity 변환
     */
    public ChatMessageEntity toEntity(Message message) {
        ChatMessageEntity.ChatMessageEntityBuilder builder = ChatMessageEntity.builder()
                .id(message.getId().value())
                .channelId(message.getChannelId().value())
                .senderId(message.getSenderId().value())
                .messageType(message.getType())
                .messageStatus(message.getStatus())
                .createdAt(message.getCreatedAt())
                .sentAt(message.getSentAt())
                .deliveredAt(message.getDeliveredAt())
                .readAt(message.getReadAt());

        // MessageContent 매핑 (Sealed Interface 활용)
        MessageContent content = message.getContent();
        if (content instanceof MessageContent.Text t) {
            builder.contentText(t.text());
        } else if (content instanceof MessageContent.Image i) {
            builder.contentMediaUrl(i.mediaUrl())
                    .contentFileName(i.fileName())
                    .contentFileSize(i.fileSize());
        } else if (content instanceof MessageContent.File f) {
            builder.contentMediaUrl(f.mediaUrl())
                    .contentFileName(f.fileName())
                    .contentFileSize(f.fileSize())
                    .contentMimeType(f.mimeType());
        }

        return builder.build();
    }

    /**
     * Entity → Domain 변환
     */
    public Message toDomain(ChatMessageEntity entity) {
        MessageContent content = switch (entity.getMessageType()) {
            case TEXT, SYSTEM -> MessageContent.text(entity.getContentText());
            case IMAGE -> MessageContent.image(entity.getContentMediaUrl(), entity.getContentFileName(),
                    entity.getContentFileSize());
            case FILE, VIDEO, AUDIO -> MessageContent.file(entity.getContentMediaUrl(), entity.getContentFileName(),
                    entity.getContentFileSize(), entity.getContentMimeType());
        };

        return Message.fromStorage(
                MessageId.of(entity.getId()),
                ChannelId.of(entity.getChannelId()),
                UserId.of(entity.getSenderId()),
                content,
                entity.getMessageType(),
                entity.getMessageStatus(),
                entity.getCreatedAt(),
                entity.getSentAt(),
                entity.getDeliveredAt(),
                entity.getReadAt());
    }
}
