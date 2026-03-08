package com.example.chat.storage.config.datasource;

/**
 * DataSource 타입 식별자
 * SOURCE: 쓰기 전용 (Primary)
 * REPLICA: 읽기 전용 (Replica)
 */
public enum DataSourceType {
    SOURCE,
    REPLICA
}
