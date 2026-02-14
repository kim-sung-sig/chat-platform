package com.example.chat.auth.server.api.dto.request

import jakarta.validation.constraints.NotBlank

/** MFA 완료 요청 DTO */
data class CompleteMfaRequest(
	@field:NotBlank(message = "mfaToken cannot be blank")
	val mfaToken: String? = null,

	@field:NotBlank(message = "mfaSessionId cannot be blank")
	val mfaSessionId: String? = null,

	@field:NotBlank(message = "mfaMethod cannot be blank")
	val mfaMethod: String? = null,

	@field:NotBlank(message = "otpCode cannot be blank")
	val otpCode: String? = null

)
