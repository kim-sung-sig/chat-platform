package com.example.chat;

import com.example.chat.common.logging.annotation.EnableTracingLogging;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Chat Server - message-server + system-server 통합 서버
 * websocket-server 는 WebSocket 연결 수 기반 독립 스케일링을 위해 분리 유지.
 */
@SpringBootApplication
@EnableScheduling
@EnableTracingLogging
public class ChatServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(ChatServerApplication.class, args);
	}
}
