package com.example.chat.push.infrastructure.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.web.client.RestTemplate

@Configuration
@EnableScheduling
@EnableKafka
class PushServiceConfig {
    @Bean
    fun restTemplate(): RestTemplate {
        return RestTemplate()
    }
}
