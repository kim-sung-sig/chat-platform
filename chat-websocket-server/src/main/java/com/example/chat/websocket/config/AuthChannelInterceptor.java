package com.example.chat.websocket.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthChannelInterceptor implements ChannelInterceptor {

	@Override
	public Message<?> preSend(
			@NonNull Message<?> message,
			@NonNull MessageChannel channel
	) {
		StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
		if (accessor == null) {
			throw new UnsupportedOperationException("StompHeaderAccessor is null");
		}

		StompCommand command = accessor.getCommand();
		if (command == null) {
			throw new UnsupportedOperationException("StompCommand is null");
		}

		switch (command) {
			case CONNECT -> checkConnect(accessor);
			case SUBSCRIBE -> checkSubscribe(accessor);
			default -> log.debug("STOMP Command: {}, User: {}", command, accessor.getUser());
		}

		return message;
	}

	private void checkConnect(StompHeaderAccessor accessor) {

	}

	private void checkSubscribe(StompHeaderAccessor accessor) {

	}

}