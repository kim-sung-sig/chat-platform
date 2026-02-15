package com.example.chat.message.application.dto.request

import com.example.chat.domain.message.MessageType
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

/**
 * 메시지 발송 요청 DTO
 *
 * Validation을 통한 조기 에러 표출
 */
data class SendMessageRequest(
	@field:NotBlank(message = "channelId is required")
	val channelId: String?,

	@field:NotNull(message = "messageType is required")
	val messageType: MessageType?,

	@field:NotNull(message = "payload is required")
	val payload: Map<String, Any>?
)
