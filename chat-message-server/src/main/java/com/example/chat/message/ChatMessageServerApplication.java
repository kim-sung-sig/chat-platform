package com.example.chat.message;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.example.chat")
public class ChatMessageServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(ChatMessageServerApplication.class, args);
	}

}