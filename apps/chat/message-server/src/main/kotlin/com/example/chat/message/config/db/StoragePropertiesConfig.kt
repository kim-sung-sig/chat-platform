package com.example.chat.message.config.db

import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.PropertySource

/**
 * Storage 모듈의 DB 설정 로드
 */
@Configuration
@PropertySource("classpath:application-db.properties")
class StoragePropertiesConfig
