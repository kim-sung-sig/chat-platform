package com.example.chat.message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.example.chat")
public class ChatMessageServerApplication {

    private static final Logger logger = LoggerFactory.getLogger(ChatMessageServerApplication.class);

    public static void main(String[] args) {
        // TODO: 환경별 설정 확인 (profiles: local/dev/staging/prod)
        logger.info("Starting ChatMessageServerApplication - initializing components and health checks");
        SpringApplication.run(ChatMessageServerApplication.class, args);
        logger.info("ChatMessageServerApplication started");
    }

}