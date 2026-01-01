package com.example.chat.message.infrastructure.messaging;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import com.example.chat.domain.message.Message;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 메시지 이벤트 발행자 (Redis Pub/Sub)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MessageEventPublisher {

	private static final String MESSAGE_SENT_CHANNEL_PREFIX = "chat:message:sent:";

	private final RedisTemplate<String, String> redisTemplate;
	private final ObjectMapper objectMapper;

	/**
	 * 메시지 발송 이벤트 발행
	 *
	 * @param message 발송된 메시지
	 */
	public void publishMessageSent(Message message) {
		// Early return: null 체크
		if (message == null) {
			log.warn("Cannot publish null message");
			return;
		}

		// Early return: 메시지 ID 체크
		if (message.getId() == null) {
			log.warn("Cannot publish message without ID");
			return;
		}

		try {
			// Step 1: 메시지 이벤트 DTO 생성
			MessageSentEvent event = createMessageSentEvent(message);

			// Step 2: JSON 직렬화
			String eventJson = serializeEvent(event);

			// Step 3: Redis Pub/Sub 발행 (채널)
			publishToChannel(message.getChannelId().getValue(), eventJson);

			log.info("Message event published: messageId={}, channelId={}",
					message.getId().getValue(), message.getChannelId().getValue());

		} catch (Exception e) {
			log.error("Failed to publish message event: messageId={}", message.getId().getValue(), e);
			throw new RuntimeException("Failed to publish message event", e);
		}
	}

	// ========== Private Helper Methods ==========

	/**
	 * MessageSentEvent 생성
	 */
	private MessageSentEvent createMessageSentEvent(Message message) {
		return MessageSentEvent.builder()
				.messageId(message.getId().getValue())
				.channelId(message.getChannelId().getValue())
				.senderId(message.getSenderId().getValue())
				.messageType(message.getType().name())
				.content(message.getContent().getText())
				.status(message.getStatus().name())
				.sentAt(message.getSentAt())
				.build();
	}

	/**
	 * JSON 직렬화
	 */
	private String serializeEvent(MessageSentEvent event) {
		try {
			return objectMapper.writeValueAsString(event);
		} catch (Exception e) {
			throw new RuntimeException("Failed to serialize message event", e);
		}
	}

	/**
	 * 채널로 발행
	 */
	private void publishToChannel(@NonNull String channelId, @NonNull String eventJson) {
		String channel = MESSAGE_SENT_CHANNEL_PREFIX + channelId;
		redisTemplate.convertAndSend(channel, eventJson);

		log.debug("Published to channel: {}", channel);
	}
}
