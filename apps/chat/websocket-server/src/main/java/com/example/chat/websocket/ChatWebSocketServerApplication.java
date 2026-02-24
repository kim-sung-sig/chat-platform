package com.example.chat.websocket;

import com.example.chat.common.logging.annotation.EnableTracingLogging;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@EnableTracingLogging
@SpringBootApplication
@EnableRabbit
public class ChatWebSocketServerApplication {

	private static final Logger log = LoggerFactory.getLogger(ChatWebSocketServerApplication.class);

	public static void main(String[] args) {
		// TODO: 프로파일/환경변수 확인 로직 추가 (local/dev/staging/prod)
		log.info("Starting ChatWebSocketServerApplication - initializing WebSocket endpoints and connectors");
		SpringApplication.run(ChatWebSocketServerApplication.class, args);
		log.info("ChatWebSocketServerApplication started");

		// TODO: 애플리케이션 종료시 graceful shutdown hooks, health check 등록, 리소스 정리 구현
	}

}
