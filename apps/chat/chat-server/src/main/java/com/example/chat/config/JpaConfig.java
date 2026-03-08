package com.example.chat.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * JPA 설정 - chat-storage 모듈 스캔
 */
@Configuration
@ComponentScan(basePackages = {
        "com.example.chat.storage.config.datasource"
})
@EntityScan(basePackages = {
        "com.example.chat.storage.entity"
})
@EnableJpaRepositories(basePackages = {
        "com.example.chat.storage.repository"
})
public class JpaConfig {
}
