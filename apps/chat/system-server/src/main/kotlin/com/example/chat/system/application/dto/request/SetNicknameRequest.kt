package com.example.chat.system.application.dto.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

/**
 * 친구 별칭 설정 Request DTO
 */
data class SetNicknameRequest(
	@field:NotBlank(message = "Nickname is required")
	@field:Size(max = 100, message = "Nickname must be less than 100 characters")
	val nickname: String
)
