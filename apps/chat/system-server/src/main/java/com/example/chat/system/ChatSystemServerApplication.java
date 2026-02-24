package com.example.chat.system;

import com.example.chat.common.logging.annotation.EnableTracingLogging;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@EnableTracingLogging
@SpringBootApplication
public class ChatSystemServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(ChatSystemServerApplication.class, args);
    }
}
