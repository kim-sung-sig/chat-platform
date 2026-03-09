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
 * 읽음 처리 완료 이벤트 Redis 구독자 (websocket-server)
 *
 * 구독 채널: chat:read:event:{channelId}
 * 발행자: chat-server (ReadReceiptEventPublisher)
 * 역할: 읽음 이벤트를 수신하여 채팅방 내 모든 접속자에게 WebSocket으로 브로드캐스트
 *
 * 클라이언트는 이 이벤트를 받아 해당 메시지 이하의 readCount/unreadCount UI를 갱신
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReadReceiptRedisSubscriber implements MessageListener {

    private static final String CHANNEL_PREFIX = "chat:read:event:";

    private final ObjectMapper objectMapper;
    private final WebSocketBroadcastService broadcastService;

    @Override
    public void onMessage(@NonNull Message message, @Nullable byte[] pattern) {
        try {
            String body = new String(message.getBody());
            String channel = new String(message.getChannel());

            log.debug("Received read receipt event from channel: {}", channel);

            ReadReceiptEvent event = objectMapper.readValue(body, ReadReceiptEvent.class);
            if (event == null || event.getChannelId() == null) {
                log.warn("Invalid read receipt event body: {}", body);
                return;
            }

            // 채팅방의 모든 접속자에게 읽음 이벤트 브로드캐스트
            broadcastService.broadcastReadReceipt(event.getChannelId(), event);

        } catch (Exception e) {
            log.error("Error processing read receipt event", e);
        }
    }
}
