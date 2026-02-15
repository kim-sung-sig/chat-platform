package com.example.chat.message.infrastructure.messaging

import java.time.Instant

/**
 * 메시지 발송 이벤트 DTO
 * Redis Pub/Sub으로 전송되는 이벤트
 *
 * 불변 객체로 설계 (Value Object)
 */
data class MessageSentEvent(
	val messageId: String,     // String (UUID)
	val channelId: String,     // ChannelId
	val senderId: String,      // UserId
	val messageType: String,   // MessageType name
	val content: String,       // 텍스트 내용
	val status: String,        // MessageStatus name
	val sentAt: Instant?
)
