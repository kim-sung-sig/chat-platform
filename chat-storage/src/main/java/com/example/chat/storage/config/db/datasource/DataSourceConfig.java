package com.example.chat.storage.config.db.datasource;

import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Configuration
@EnableConfigurationProperties(MultiDataSourceProperties.class)
public class DataSourceConfig {

	@Bean
	@Primary
	public DataSource routingDataSource(MultiDataSourcePropertiesWrapper wrapper) throws SQLException {
		HikariDataSource source = wrapper.getSource().initializeDataSourceBuilder()
				.type(HikariDataSource.class)
				.build();

		HikariDataSource replica = wrapper.getReplica().initializeDataSourceBuilder()
				.type(HikariDataSource.class)
				.build();
		RoutingDataSource routingDataSource = new RoutingDataSource();
		Map<Object, Object> targetDataSources = new HashMap<>();
		targetDataSources.put(DataSourceType.SOURCE.getKey(), source);
		targetDataSources.put(DataSourceType.REPLICA.getKey(), replica);
		routingDataSource.setTargetDataSources(targetDataSources);
		routingDataSource.setDefaultTargetDataSource(source);

		try (Connection ignored = replica.getConnection()) {
			log.info("Init ReplicaConnectionPool.");
		}
		return routingDataSource;
	}

}