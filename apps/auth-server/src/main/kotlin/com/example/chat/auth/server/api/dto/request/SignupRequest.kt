package com.example.chat.auth.server.api.dto.request

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

data class SignupRequest(
	@field:NotBlank
	@field:Email
	val email: String,

	@field:NotBlank
	val password: String,

	@field:NotBlank
	val nickname: String
)
