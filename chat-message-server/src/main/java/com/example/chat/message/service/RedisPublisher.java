package com.example.chat.message.service;

import com.example.chat.common.dto.ChatMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisPublisher implements MessagePublisher {
	private final StringRedisTemplate redisTemplate;
	private final ObjectMapper objectMapper;

	public void publish(String topic, ChatMessage message) {
		try {
			redisTemplate.convertAndSend(topic, objectMapper.writeValueAsString(message));
		} catch (Exception e) {
			throw new RuntimeException("Redis publish failed", e);
		}
	}
}