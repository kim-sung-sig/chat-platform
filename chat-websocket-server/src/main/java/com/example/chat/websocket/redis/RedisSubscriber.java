package com.example.chat.websocket.redis;

import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import com.example.chat.common.dto.ChatMessage;
import com.example.chat.websocket.application.MessageBroadcaster;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisSubscriber implements MessageListener {

	private final MessageBroadcaster messageBroadcaster;
	private final ObjectMapper objectMapper;

	@Override
	public void onMessage(@NonNull Message message, @Nullable byte[] pattern) {
		try {
			ChatMessage chatMessage = objectMapper.readValue(message.getBody(), ChatMessage.class);
			log.debug("Received message from Redis: {}", chatMessage);
			messageBroadcaster.broadcast(chatMessage.getRoomId(), chatMessage);
		} catch (Exception e) {
			log.error("Error while processing redis message", e);
		}
	}

}