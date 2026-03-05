package com.example.chat.system.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * JPA 설정
 * chat-storage 모듈의 Entity 및 Repository 스캔 경로를 명시적으로 등록한다.
 * - scanBasePackages = "com.example.chat" 이므로 adapter/mapper 는 자동 스캔됨
 * - Entity 및 JPA Repository 는 별도 경로 등록 필요
 */
@Configuration
@EntityScan(basePackages = {
        "com.example.chat.storage.entity"
})
@EnableJpaRepositories(basePackages = {
        "com.example.chat.storage.repository"
})
public class JpaConfig {
}
