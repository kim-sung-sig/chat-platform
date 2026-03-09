package com.example.chat.websocket.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.lang.NonNull;

import com.example.chat.websocket.infrastructure.redis.ReadReceiptRedisSubscriber;
import com.example.chat.websocket.infrastructure.redis.RedisMessageSubscriber;

import lombok.RequiredArgsConstructor;

/**
 * Redis 구독 설정
 */
@Configuration
@RequiredArgsConstructor
public class RedisConfig {

	private final @NonNull RedisMessageSubscriber redisMessageSubscriber;
	private final @NonNull ReadReceiptRedisSubscriber readReceiptRedisSubscriber;

	/**
	 * Redis 메시지 리스너 컨테이너
	 * - chat:room:* : 신규 메시지 브로드캐스트
	 * - chat:read:event:* : 읽음 처리 완료 이벤트 브로드캐스트
	 */
	@Bean
	public RedisMessageListenerContainer redisMessageListenerContainer(
			@NonNull RedisConnectionFactory connectionFactory) {
		RedisMessageListenerContainer container = new RedisMessageListenerContainer();
		container.setConnectionFactory(connectionFactory);

		// 신규 메시지 수신 (chat-server 발행)
		container.addMessageListener(
				messageListenerAdapter(),
				new PatternTopic("chat:room:*"));

		// 읽음 이벤트 수신 (chat-server 발행, 읽음 처리 완료 후)
		container.addMessageListener(
				readReceiptListenerAdapter(),
				new PatternTopic("chat:read:event:*"));

		return container;
	}

	@Bean
	public @NonNull MessageListenerAdapter messageListenerAdapter() {
		return new MessageListenerAdapter(redisMessageSubscriber);
	}

	@Bean
	public @NonNull MessageListenerAdapter readReceiptListenerAdapter() {
		return new MessageListenerAdapter(readReceiptRedisSubscriber);
	}
}
