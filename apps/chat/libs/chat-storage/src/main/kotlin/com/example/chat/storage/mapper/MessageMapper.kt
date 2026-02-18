package com.example.chat.storage.mapper

import com.example.chat.domain.channel.ChannelId
import com.example.chat.domain.message.Message
import com.example.chat.domain.message.MessageContent
import com.example.chat.domain.message.MessageId
import com.example.chat.domain.user.UserId
import com.example.chat.storage.entity.ChatMessageEntity
import org.springframework.stereotype.Component

/**
 * Message Domain ↔ ChatMessageEntity 변환
 */
@Component
class MessageMapper {

    /**
     * Domain → Entity 변환
     */
    fun toEntity(message: Message): ChatMessageEntity {
        return ChatMessageEntity(
            id = message.id.value,
            channelId = message.channelId.value,
            senderId = message.senderId.value,
            messageType = message.type,
            messageStatus = message.status,
            contentText = message.content.text,
            contentMediaUrl = message.content.mediaUrl,
            contentFileName = message.content.fileName,
            contentFileSize = message.content.fileSize,
            contentMimeType = message.content.mimeType,
            createdAt = message.createdAt,
            sentAt = message.sentAt,
            deliveredAt = message.deliveredAt,
            readAt = message.readAt
        )
    }

    /**
     * Entity → Domain 변환
     */
    fun toDomain(entity: ChatMessageEntity): Message {
        val content = MessageContent(
            text = entity.contentText,
            mediaUrl = entity.contentMediaUrl,
            fileName = entity.contentFileName,
            fileSize = entity.contentFileSize,
            mimeType = entity.contentMimeType
        )

        return Message.fromStorage(
            id = MessageId.of(entity.id),
            channelId = ChannelId.of(entity.channelId),
            senderId = UserId.of(entity.senderId),
            content = content,
            type = entity.messageType,
            status = entity.messageStatus,
            createdAt = entity.createdAt,
            sentAt = entity.sentAt,
            deliveredAt = entity.deliveredAt,
            readAt = entity.readAt
        )
    }
}

