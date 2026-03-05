package com.example.chat.auth.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

import com.example.chat.common.logging.annotation.EnableTracingLogging;

@EnableTracingLogging
@EnableDiscoveryClient
@SpringBootApplication(scanBasePackages = "com.example.chat")
public class AuthServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthServerApplication.class, args);
    }
}
