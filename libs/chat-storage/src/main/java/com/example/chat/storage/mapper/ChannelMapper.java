package com.example.chat.storage.mapper;

import com.example.chat.domain.channel.Channel;
import com.example.chat.domain.channel.ChannelId;
import com.example.chat.domain.user.UserId;
import com.example.chat.storage.entity.ChatChannelEntity;
import com.example.chat.storage.entity.ChatChannelMemberEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Channel Domain ↔ ChatChannelEntity 변환
 */
@Component
public class ChannelMapper {

    /**
     * Domain → Entity 변환 (멤버 정보 제외)
     */
    public ChatChannelEntity toEntity(Channel channel) {
        return ChatChannelEntity.builder()
                .id(channel.getId().getValue())
                .name(channel.getName())
                .description(channel.getDescription())
                .channelType(channel.getType())
                .ownerId(channel.getOwnerId().getValue())
                .active(channel.isActive())
                .createdAt(channel.getCreatedAt())
                .updatedAt(channel.getUpdatedAt())
                .build();
    }

    /**
     * Domain의 멤버 정보를 ChatChannelMemberEntity 리스트로 변환
     */
    public List<ChatChannelMemberEntity> toMemberEntities(Channel channel) {
        return channel.getMemberIds().stream()
                .map(userId -> ChatChannelMemberEntity.builder()
                        .channelId(channel.getId().getValue())
                        .userId(userId.getValue())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Entity → Domain 변환
     */
    public Channel toDomain(ChatChannelEntity entity, Set<String> memberIds) {
        Set<UserId> userIds = memberIds.stream()
                .map(UserId::of)
                .collect(Collectors.toSet());

        return Channel.builder()
                .id(ChannelId.of(entity.getId()))
                .name(entity.getName())
                .description(entity.getDescription())
                .type(entity.getChannelType())
                .ownerId(UserId.of(entity.getOwnerId()))
                .memberIds(userIds)
                .active(entity.getActive())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
