package com.example.chat.push;

import com.example.chat.common.logging.annotation.EnableTracingLogging;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@EnableTracingLogging
@SpringBootApplication
public class PushServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(PushServiceApplication.class, args);
    }
}
