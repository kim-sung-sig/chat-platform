package com.example.chat.storage.mapper

import com.example.chat.domain.channel.Channel
import com.example.chat.domain.channel.ChannelId
import com.example.chat.domain.user.UserId
import com.example.chat.storage.entity.ChatChannelEntity
import com.example.chat.storage.entity.ChatChannelMemberEntity
import org.springframework.stereotype.Component

/**
 * Channel Domain ↔ ChatChannelEntity 변환
 */
@Component
class ChannelMapper {

    /**
     * Domain → Entity 변환 (멤버 정보 제외)
     */
    fun toEntity(channel: Channel): ChatChannelEntity {
        return ChatChannelEntity(
            id = channel.id.value,
            name = channel.name,
            description = channel.description,
            channelType = channel.type,
            ownerId = channel.ownerId.value,
            active = channel.active,
            createdAt = channel.createdAt,
            updatedAt = channel.updatedAt
        )
    }

    /**
     * Domain의 멤버 정보를 ChatChannelMemberEntity 리스트로 변환
     */
    fun toMemberEntities(channel: Channel): List<ChatChannelMemberEntity> {
        return channel.memberIds.map { userId ->
            ChatChannelMemberEntity(
                channelId = channel.id.value,
                userId = userId.value
            )
        }
    }

    /**
     * Entity → Domain 변환
     */
    fun toDomain(entity: ChatChannelEntity, memberIds: Set<String>): Channel {
        val userIds = memberIds.map { UserId.of(it) }.toSet()

        return Channel.fromStorage(
            id = ChannelId.of(entity.id),
            name = entity.name,
            description = entity.description,
            type = entity.channelType,
            ownerId = UserId.of(entity.ownerId),
            memberIds = userIds,
            active = entity.active,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )
    }
}

