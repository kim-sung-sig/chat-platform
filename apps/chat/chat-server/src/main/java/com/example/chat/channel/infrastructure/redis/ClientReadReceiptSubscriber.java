package com.example.chat.channel.infrastructure.redis;

import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import com.example.chat.channel.application.service.ChannelMetadataApplicationService;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 클라이언트 읽음 이벤트 Redis 구독자 (chat-server)
 *
 * 구독 채널: chat:read:{channelId}
 * 발행자: websocket-server (ChatWebSocketHandler → READ_RECEIPT 수신 시)
 * 역할: 클라이언트가 WebSocket으로 보낸 읽음 이벤트를 DB에 반영 후
 *       ReadReceiptEventPublisher를 통해 chat:read:event:{channelId}로 재발행
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ClientReadReceiptSubscriber implements MessageListener {

    private static final String CHANNEL_PREFIX = "chat:read:";

    private final ObjectMapper objectMapper;
    private final ChannelMetadataApplicationService metadataService;
    private final ReadReceiptEventPublisher readReceiptEventPublisher;

    @Override
    public void onMessage(@NonNull Message message, @Nullable byte[] pattern) {
        try {
            String body = new String(message.getBody());
            String channel = new String(message.getChannel());

            log.debug("Received client read receipt from channel: {}", channel);

            ClientReadEvent event = objectMapper.readValue(body, ClientReadEvent.class);
            if (event == null || event.userId() == null || event.channelId() == null) {
                log.warn("Invalid read receipt event: {}", body);
                return;
            }

            // DB 반영: lastReadMessageId 갱신, unreadCount=0
            metadataService.markAsRead(event.userId(), event.channelId(), event.lastReadMessageId());

            log.info("Read receipt processed via WebSocket: userId={}, channelId={}, messageId={}",
                    event.userId(), event.channelId(), event.lastReadMessageId());

        } catch (Exception e) {
            log.error("Error processing client read receipt", e);
        }
    }

    /**
     * 클라이언트 읽음 이벤트 DTO (websocket-server에서 발행)
     */
    public record ClientReadEvent(
            String type,
            String userId,
            String channelId,
            String lastReadMessageId) {
    }
}
