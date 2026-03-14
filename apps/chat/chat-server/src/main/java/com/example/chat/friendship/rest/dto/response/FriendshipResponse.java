package com.example.chat.friendship.rest.dto.response;

import java.time.Instant;

import com.example.chat.common.core.enums.FriendshipStatus;
import com.example.chat.storage.domain.entity.ChatFriendshipEntity;

/**
 * 친구 관계 Response DTO
 *
 * Phase 4: Domain POJO 의존 제거 - fromEntity() 단일 팩토리 메서드 사용
 */
public record FriendshipResponse(
        String id,
        String userId,
        String friendId,
        FriendshipStatus status,
        String nickname,
        boolean favorite,
        Instant createdAt,
        Instant updatedAt) {

    public static FriendshipResponse fromEntity(ChatFriendshipEntity entity) {
        return new FriendshipResponse(
                entity.getId(),
                entity.getUserId(),
                entity.getFriendId(),
                entity.getStatus(),
                entity.getNickname(),
                entity.isFavorite(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }
}
