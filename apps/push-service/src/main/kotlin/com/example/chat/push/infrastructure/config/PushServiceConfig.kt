package com.example.chat.push.infrastructure.config

import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.kafka.annotation.EnableKafka

@Configuration
@EnableScheduling
@EnableKafka
class PushServiceConfig
