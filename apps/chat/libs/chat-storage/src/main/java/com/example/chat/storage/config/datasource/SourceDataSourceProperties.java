package com.example.chat.storage.config.datasource;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Source(Write) DataSource 프로퍼티
 * spring.datasource.source.* 에 바인딩된다.
 */
@ConfigurationProperties(prefix = "spring.datasource.source")
public record SourceDataSourceProperties(
        String driverClassName,
        String url,
        String username,
        String password
) {
}
