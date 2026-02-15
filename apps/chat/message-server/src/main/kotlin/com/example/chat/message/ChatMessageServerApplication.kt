package com.example.chat.message

import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

/**
 * Chat Message Server Application
 *
 * 메시지 발송 전담 서버
 * - HTTP API를 통한 메시지 수신
 * - Storage를 통한 메시지 저장
 * - Redis Pub/Sub을 통한 WebSocket 서버 전달
 * - Kafka를 통한 Push Service 전달
 */
@SpringBootApplication(scanBasePackages = ["com.example.chat"])
class ChatMessageServerApplication

fun main(args: Array<String>) {
	val log = LoggerFactory.getLogger(ChatMessageServerApplication::class.java)
	log.info("Starting ChatMessageServerApplication...")
	runApplication<ChatMessageServerApplication>(*args)
	log.info("ChatMessageServerApplication started successfully")
}
