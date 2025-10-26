package com.example.chat.system;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Chat System Server Application
 * 메시지 스케줄링 및 발행 서비스
 */
@SpringBootApplication
@EnableScheduling
public class ChatSystemServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(ChatSystemServerApplication.class, args);
	}

}