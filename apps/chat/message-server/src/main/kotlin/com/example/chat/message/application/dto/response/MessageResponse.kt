package com.example.chat.message.application.dto.response

import com.example.chat.domain.message.MessageStatus
import com.example.chat.domain.message.MessageType
import java.time.Instant

/**
 * 메시지 응답 DTO
 *
 * 불변 객체로 설계
 */
data class MessageResponse(
	val id: String,              // MessageId → String
	val channelId: String,       // ChannelId
	val senderId: String,        // UserId
	val messageType: MessageType,
	val content: String,         // 단순화 (text)
	val status: MessageStatus,
	val createdAt: Instant?,
	val sentAt: Instant?,
	val deliveredAt: Instant? = null,
	val readAt: Instant? = null
)
