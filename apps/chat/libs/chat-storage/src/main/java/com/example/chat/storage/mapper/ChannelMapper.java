package com.example.chat.storage.mapper;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.example.chat.domain.channel.Channel;
import com.example.chat.domain.channel.ChannelId;
import com.example.chat.domain.user.UserId;
import com.example.chat.storage.entity.ChatChannelEntity;
import com.example.chat.storage.entity.ChatChannelMemberEntity;

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
                                .id(channel.getId().value())
                                .name(channel.getName())
                                .description(channel.getDescription())
                                .channelType(channel.getType())
                                .ownerId(channel.getOwnerId().value())
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
                                                .channelId(channel.getId().value())
                                                .userId(userId.value())
                                                .joinedAt(java.time.Instant.now())
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

                return Channel.fromStorage(
                                ChannelId.of(entity.getId()),
                                entity.getName(),
                                entity.getDescription(),
                                entity.getChannelType(),
                                UserId.of(entity.getOwnerId()),
                                userIds,
                                entity.isActive(),
                                entity.getCreatedAt(),
                                entity.getUpdatedAt());
        }
}
