package com.example.chat.websocket.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import com.example.chat.websocket.presentation.handler.ChatWebSocketHandler;

import lombok.RequiredArgsConstructor;

/**
 * WebSocket 설정
 */
@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

	private final @NonNull ChatWebSocketHandler chatWebSocketHandler;

	@Override
	public void registerWebSocketHandlers(@NonNull WebSocketHandlerRegistry registry) {
		registry.addHandler(chatWebSocketHandler, "/ws/chat")
				.setAllowedOrigins("*"); // CORS 설정 (운영 환경에서는 명시적으로 지정)
	}
}
