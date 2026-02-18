package com.example.chat.system.application.dto.request

import jakarta.validation.constraints.NotBlank

/**
 * 친구 요청 Request DTO
 */
data class FriendshipRequest(
	@field:NotBlank(message = "Friend ID is required")
	val friendId: String
)
