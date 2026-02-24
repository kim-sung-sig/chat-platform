package com.example.chat.message;

import com.example.chat.common.logging.annotation.EnableTracingLogging;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Chat Message Server Application
 *
 * 메시지 발송 및 처리 서버
 * - HTTP API를 통한 메시지 수신
 * - Storage를 통한 메시지 저장
 * - Redis Pub/Sub을 통한 WebSocket 서버 전달
 * - Kafka를 통한 Push Service 전달
 */
@EnableTracingLogging
@SpringBootApplication(scanBasePackages = "com.example.chat")
public class ChatMessageServerApplication {
    private static final Logger log = LoggerFactory.getLogger(ChatMessageServerApplication.class);

    public static void main(String[] args) {
        log.info("Starting ChatMessageServerApplication...");
        SpringApplication.run(ChatMessageServerApplication.class, args);
        log.info("ChatMessageServerApplication started successfully");
    }
}
