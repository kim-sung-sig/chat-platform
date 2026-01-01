package com.example.chat.websocket.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.lang.NonNull;

import com.example.chat.websocket.infrastructure.redis.RedisMessageSubscriber;

import lombok.RequiredArgsConstructor;

/**
 * Redis 구독 설정
 */
@Configuration
@RequiredArgsConstructor
public class RedisConfig {

	private final @NonNull RedisMessageSubscriber redisMessageSubscriber;

	/**
	 * Redis 메시지 리스너 컨테이너
	 * chat:room:* 패턴의 채널 구독
	 */
	@Bean
	public RedisMessageListenerContainer redisMessageListenerContainer(
			@NonNull RedisConnectionFactory connectionFactory) {
		RedisMessageListenerContainer container = new RedisMessageListenerContainer();
		container.setConnectionFactory(connectionFactory);

		// chat:room:* 패턴 구독
		container.addMessageListener(
				messageListenerAdapter(),
				new PatternTopic("chat:room:*"));

		return container;
	}

	/**
	 * 메시지 리스너 어댑터
	 */
	@Bean
	public @NonNull MessageListenerAdapter messageListenerAdapter() {
		return new MessageListenerAdapter(redisMessageSubscriber);
	}
}
