package com.example.chat.storage.config.datasource;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Replica(Read) DataSource 프로퍼티
 * spring.datasource.replica.* 에 바인딩된다.
 */
@ConfigurationProperties(prefix = "spring.datasource.replica")
public record ReplicaDataSourceProperties(
        String driverClassName,
        String url,
        String username,
        String password
) {
}
