package com.example.chat.message.infrastructure.messaging

import com.example.chat.domain.message.Message
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component

/**
 * 메시지 이벤트 발행자 (Redis Pub/Sub)
 *
 * 책임:
 * - 메시지 발송 이벤트를 Redis Pub/Sub 채널로 발행
 * - 이벤트 직렬화 및 채널 라우팅
 */
@Component
class MessageEventPublisher(
	private val redisTemplate: RedisTemplate<String, String>,
	private val objectMapper: ObjectMapper
) {
	private val log = LoggerFactory.getLogger(javaClass)

	/**
	 * 메시지 발송 이벤트 발행
	 *
	 * @param message 발송된 메시지
	 */
	fun publishMessageSent(message: Message) {
		// Early return: null 체크
		val messageId = message.id ?: run {
			log.warn("Cannot publish message without ID")
			return
		}

		try {
			// Step 1: 메시지 이벤트 DTO 생성
			val event = createMessageSentEvent(message)

			// Step 2: JSON 직렬화
			val eventJson = serializeEvent(event)

			// Step 3: Redis Pub/Sub 발행 (채널)
			publishToChannel(message.channelId.value, eventJson)

			log.info(
				"Message event published: messageId={}, channelId={}",
				messageId.value, message.channelId.value
			)
		} catch (e: Exception) {
			log.error("Failed to publish message event: messageId=${messageId.value}", e)
			throw RuntimeException("Failed to publish message event", e)
		}
	}

	// ========== Private Helper Methods ==========

	/**
	 * MessageSentEvent 생성
	 */
	private fun createMessageSentEvent(message: Message): MessageSentEvent {
		return MessageSentEvent(
			messageId = message.id!!.value,
			channelId = message.channelId.value,
			senderId = message.senderId.value,
			messageType = message.type.name,
			content = message.content.text ?: "",
			status = message.status.name,
			sentAt = message.sentAt
		)
	}

	/**
	 * JSON 직렬화
	 */
	private fun serializeEvent(event: MessageSentEvent): String {
		return try {
			objectMapper.writeValueAsString(event)
		} catch (e: Exception) {
			throw RuntimeException("Failed to serialize message event", e)
		}
	}

	/**
	 * 채널로 발행
	 */
	private fun publishToChannel(channelId: String, eventJson: String) {
		val channel = "$MESSAGE_SENT_CHANNEL_PREFIX$channelId"
		redisTemplate.convertAndSend(channel, eventJson)

		log.debug("Published to channel: {}", channel)
	}

	companion object {
		private const val MESSAGE_SENT_CHANNEL_PREFIX = "chat:room:"
	}
}
