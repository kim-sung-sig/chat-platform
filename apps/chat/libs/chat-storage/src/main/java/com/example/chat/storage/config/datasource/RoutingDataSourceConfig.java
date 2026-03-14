package com.example.chat.storage.config.datasource;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;
import javax.sql.DataSource;
import java.util.Map;
/**
 * Source / Replica DataSource 라우팅 설정
 *
 * 활성화 조건: spring.datasource.source.url 이 설정된 경우
 *
 * 흐름:
 * 1. sourceDataSource  - PostgreSQL Source (Write)
 * 2. replicaDataSource - PostgreSQL Replica (Read)
 * 3. routingDataSource - @Transactional readOnly 여부로 라우팅
 * 4. dataSource (@Primary) - LazyConnectionDataSourceProxy 로 감싸
 *    트랜잭션 시작 시점에 실제 커넥션 결정 (readOnly 반영 타이밍 보장)
 *
 * 주의: routingDataSource 는 @Qualifier 로 명시적 주입 - @Primary dataSource 와의 순환 참조 방지
 */
@Configuration
@ConditionalOnProperty(name = "spring.datasource.source.url")
@EnableConfigurationProperties({SourceDataSourceProperties.class, ReplicaDataSourceProperties.class})
@Slf4j
public class RoutingDataSourceConfig {
    @Bean(name = "sourceDataSource")
    public DataSource sourceDataSource(SourceDataSourceProperties props) {
        HikariDataSource ds = new HikariDataSource();
        ds.setDriverClassName(props.driverClassName());
        ds.setJdbcUrl(props.url());
        ds.setUsername(props.username());
        ds.setPassword(props.password());
        ds.setPoolName("source-pool");
        ds.setMaximumPoolSize(10);
        ds.setMinimumIdle(2);
        log.info("[DataSource] Source(Write) registered: {}", props.url());
        return ds;
    }
    @Bean(name = "replicaDataSource")
    public DataSource replicaDataSource(ReplicaDataSourceProperties props) {
        HikariDataSource ds = new HikariDataSource();
        ds.setDriverClassName(props.driverClassName());
        ds.setJdbcUrl(props.url());
        ds.setUsername(props.username());
        ds.setPassword(props.password());
        ds.setPoolName("replica-pool");
        ds.setMaximumPoolSize(10);
        ds.setMinimumIdle(2);
        log.info("[DataSource] Replica(Read) registered: {}", props.url());
        return ds;
    }
    @Bean(name = "routingDataSource")
    public DataSource routingDataSource(
            @Qualifier("sourceDataSource") DataSource sourceDataSource,
            @Qualifier("replicaDataSource") DataSource replicaDataSource) {
        TransactionRoutingDataSource routing = new TransactionRoutingDataSource();
        routing.setTargetDataSources(Map.of(
                DataSourceType.SOURCE,  sourceDataSource,
                DataSourceType.REPLICA, replicaDataSource
        ));
        routing.setDefaultTargetDataSource(sourceDataSource);
        routing.afterPropertiesSet();
        return routing;
    }
    @Bean(name = "dataSource")
    @Primary
    public DataSource dataSource(@Qualifier("routingDataSource") DataSource routingDataSource) {
        return new LazyConnectionDataSourceProxy(routingDataSource);
    }
}
