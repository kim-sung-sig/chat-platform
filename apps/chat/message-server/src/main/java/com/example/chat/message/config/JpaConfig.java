package com.example.chat.message.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * JPA 설정
 * chat-storage 모듈의 Entity, Repository, Adapter, Mapper 스캔 경로를 명시적으로 등록한다.
 */
@Configuration
@ComponentScan(basePackages = {
        "com.example.chat.storage.adapter",
        "com.example.chat.storage.mapper"
})
@EntityScan(basePackages = {
        "com.example.chat.system",
        "com.example.chat.storage.entity"
})
@EnableJpaRepositories(basePackages = {
        "com.example.chat.system",
        "com.example.chat.storage.repository"
})
public class JpaConfig {
}
