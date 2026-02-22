package com.example.chat.system.application.dto.response;

import java.time.Instant;

import com.example.chat.domain.friendship.Friendship;
import com.example.chat.domain.friendship.FriendshipStatus;

/**
 * 친구 관계 Response DTO
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
    public static FriendshipResponse from(Friendship friendship) {
        return new FriendshipResponse(
                friendship.getId().value(),
                friendship.getUserId().value(),
                friendship.getFriendId().value(),
                friendship.getStatus(),
                friendship.getNickname(),
                friendship.isFavorite(),
                friendship.getCreatedAt(),
                friendship.getUpdatedAt());
    }
}
