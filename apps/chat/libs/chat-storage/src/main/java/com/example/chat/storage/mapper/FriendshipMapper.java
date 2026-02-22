package com.example.chat.storage.mapper;

import org.springframework.stereotype.Component;

import com.example.chat.domain.friendship.Friendship;
import com.example.chat.domain.friendship.FriendshipId;
import com.example.chat.domain.user.UserId;
import com.example.chat.storage.entity.ChatFriendshipEntity;

/**
 * Friendship Domain ↔ ChatFriendshipEntity 변환
 */
@Component
public class FriendshipMapper {

    /**
     * Domain → Entity 변환
     */
    public ChatFriendshipEntity toEntity(Friendship friendship) {
        return ChatFriendshipEntity.builder()
                .id(friendship.getId().value())
                .userId(friendship.getUserId().value())
                .friendId(friendship.getFriendId().value())
                .status(friendship.getStatus())
                .nickname(friendship.getNickname())
                .favorite(friendship.isFavorite())
                .createdAt(friendship.getCreatedAt())
                .updatedAt(friendship.getUpdatedAt())
                .build();
    }

    /**
     * Entity → Domain 변환
     */
    public Friendship toDomain(ChatFriendshipEntity entity) {
        return Friendship.fromStorage(
                FriendshipId.of(entity.getId()),
                UserId.of(entity.getUserId()),
                UserId.of(entity.getFriendId()),
                entity.getCreatedAt(),
                entity.getStatus(),
                entity.getNickname(),
                entity.isFavorite(),
                entity.getUpdatedAt());
    }
}
