package com.example.chat.system.infrastructure.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

/**
 * Test Quartz Configuration
 * Quartz를 비활성화하여 테스트 실행
 */
@TestConfiguration
@Profile("test")
public class TestQuartzConfig {

    @Bean
    @Primary
    public SchedulerFactoryBean schedulerFactoryBean() {
        SchedulerFactoryBean schedulerFactory = new SchedulerFactoryBean();
        schedulerFactory.setAutoStartup(false);
        return schedulerFactory;
    }
}