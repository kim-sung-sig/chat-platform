package com.example.chat.websocket.infrastructure.redis;

import com.example.chat.websocket.application.service.WebSocketBroadcastService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

/**
 * Redis 메시지 구독자
 * chat:room:{roomId} 채널에서 메시지를 수신하여 WebSocket으로 브로드캐스트
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisMessageSubscriber implements MessageListener {

	private final ObjectMapper objectMapper;
	private final WebSocketBroadcastService broadcastService;

	@Override
	public void onMessage(Message message, byte[] pattern) {
		try {
			// Step 1: 메시지 역직렬화
			String messageBody = new String(message.getBody());
			String channel = new String(message.getChannel());

			log.debug("Received Redis message from channel: {}", channel);

			// Step 2: MessageEvent로 역직렬화
			MessageEvent event = deserializeMessage(messageBody);

			// Early return: 역직렬화 실패
			if (event == null) {
				log.warn("Failed to deserialize message from channel: {}", channel);
				return;
			}

			// Step 3: 채팅방 ID 추출
			String roomId = extractRoomIdFromChannel(channel);

			// Early return: roomId 추출 실패
			if (roomId == null) {
				log.warn("Failed to extract roomId from channel: {}", channel);
				return;
			}

			// Step 4: WebSocket 브로드캐스트
			broadcastMessageToRoom(roomId, event);

		} catch (Exception e) {
			log.error("Error processing Redis message", e);
		}
	}

	/**
	 * JSON 메시지 역직렬화
	 */
	private MessageEvent deserializeMessage(String messageBody) {
		try {
			return objectMapper.readValue(messageBody, MessageEvent.class);
		} catch (Exception e) {
			log.error("Failed to deserialize message: {}", messageBody, e);
			return null;
		}
	}

	/**
	 * 채널명에서 roomId 추출
	 * 예: "chat:room:room-123" -> "room-123"
	 */
	private String extractRoomIdFromChannel(String channel) {
		if (channel == null || !channel.startsWith("chat:room:")) {
			return null;
		}

		return channel.substring("chat:room:".length());
	}

	/**
	 * 채팅방에 메시지 브로드캐스트
	 */
	private void broadcastMessageToRoom(String roomId, MessageEvent event) {
		broadcastService.broadcastToRoom(roomId, event);
	}
}
