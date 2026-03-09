package com.example.chat.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.lang.NonNull;

import com.example.chat.channel.infrastructure.redis.ClientReadReceiptSubscriber;

/**
 * chat-server Redis Pub/Sub 구독 설정
 *
 * 구독 채널:
 * - chat:read:{channelId}  : 클라이언트가 WebSocket으로 보낸 읽음 이벤트 수신
 */
@Configuration
public class RedisListenerConfig {

    private final ClientReadReceiptSubscriber clientReadReceiptSubscriber;

    public RedisListenerConfig(ClientReadReceiptSubscriber clientReadReceiptSubscriber) {
        this.clientReadReceiptSubscriber = clientReadReceiptSubscriber;
    }

    @Bean
    public RedisMessageListenerContainer chatServerRedisListenerContainer(
            @NonNull RedisConnectionFactory connectionFactory) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);

        // 클라이언트 읽음 이벤트 구독 (websocket-server → chat-server)
        container.addMessageListener(
                clientReadReceiptListenerAdapter(),
                new PatternTopic("chat:read:*"));

        return container;
    }

    @Bean
    public MessageListenerAdapter clientReadReceiptListenerAdapter() {
        return new MessageListenerAdapter(clientReadReceiptSubscriber);
    }
}
