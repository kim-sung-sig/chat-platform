package com.example.chat.friendship.rest.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * 친구 요청 Request DTO
 */
public record FriendshipRequest(
        @NotBlank(message = "Friend ID is required") String friendId) {
}
