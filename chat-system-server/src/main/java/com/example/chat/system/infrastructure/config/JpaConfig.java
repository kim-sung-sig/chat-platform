package com.example.chat.system.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * JPA 설정
 * JPA Auditing 활성화 (BaseEntity의 생성일시, 수정일시 자동 관리)
 */
@Configuration
@EnableJpaAuditing
public class JpaConfig {
}