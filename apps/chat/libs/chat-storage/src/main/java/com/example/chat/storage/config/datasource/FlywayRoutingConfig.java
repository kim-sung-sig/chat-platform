package com.example.chat.storage.config.datasource;

import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * Flyway 마이그레이션 설정 — Source(Write) DataSource 직접 사용
 *
 * LazyConnectionDataSourceProxy / RoutingDataSource 를 거치지 않고
 * sourceDataSource 에 직접 연결하여 DDL 을 실행한다.
 * Spring Boot 의 FlywayAutoConfiguration 은 spring.flyway.enabled=false 로 비활성화 필요.
 */
@Configuration
@ConditionalOnClass(Flyway.class)
@ConditionalOnProperty(name = "spring.datasource.source.url")
public class FlywayRoutingConfig {

    @Bean(initMethod = "migrate")
    public Flyway flyway(@Qualifier("sourceDataSource") DataSource sourceDataSource) {
        return Flyway.configure()
                .dataSource(sourceDataSource)
                .locations("classpath:db/migration")
                .baselineOnMigrate(true)
                .baselineVersion("0")
                .load();
    }
}
