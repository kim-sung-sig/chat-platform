package com.example.chat.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * JPA 설정 - chat-storage 인라인 이관 후 com.example.chat 전체 스캔
 */
@Configuration
@EntityScan(basePackages = "com.example.chat")
@EnableJpaRepositories(basePackages = "com.example.chat")
public class JpaConfig {
}
