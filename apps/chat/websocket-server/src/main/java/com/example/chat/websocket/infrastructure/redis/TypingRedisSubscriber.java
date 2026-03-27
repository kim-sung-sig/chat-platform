package com.example.chat.websocket.infrastructure.redis;

import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import com.example.chat.websocket.application.service.WebSocketBroadcastService;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 타이핑 이벤트 Redis 구독자 (websocket-server)
 *
 * 구독 채널: chat:typing:{channelId}
 * 발행자:   ChatWebSocketHandler (FE로부터 수신한 TYPING_START/STOP 이벤트)
 * 역할:     타이핑 이벤트를 수신하여 채팅방 내 모든 접속자에게 WebSocket 브로드캐스트
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TypingRedisSubscriber implements MessageListener {

    private static final String CHANNEL_PREFIX = "chat:typing:";

    private final ObjectMapper objectMapper;
    private final WebSocketBroadcastService broadcastService;

    @Override
    public void onMessage(@NonNull Message message, @Nullable byte[] pattern) {
        try {
            String body = new String(message.getBody());
            String channel = new String(message.getChannel());

            log.debug("Received typing event from Redis channel: {}", channel);

            // Step 1: TypingEvent 역직렬화
            TypingEvent event = objectMapper.readValue(body, TypingEvent.class);
            if (event == null || event.getChannelId() == null) {
                log.warn("Invalid typing event body: {}", body);
                return;
            }

            // Step 2: 채팅방의 모든 접속자에게 타이핑 이벤트 브로드캐스트
            broadcastService.broadcastTypingEvent(event.getChannelId(), event);

        } catch (Exception e) {
            log.error("Error processing typing event from Redis", e);
        }
    }
}
