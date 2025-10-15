package com.example.chat.storage.config.db.datasource;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.stereotype.Component;

@Setter
@Getter
@Slf4j
@Component
public class MultiDataSourcePropertiesWrapper {

	private final MultiDataSourceProperties multiDataSourceProperties;
	private final DataSourceProperties source;
	private final DataSourceProperties replica;

	public MultiDataSourcePropertiesWrapper(MultiDataSourceProperties multiDataSourceProperties) {
		this.multiDataSourceProperties = multiDataSourceProperties;
		this.source = multiDataSourceProperties.getSource().toDataSourceProperties();
		this.replica = multiDataSourceProperties.getReplica().toDataSourceProperties();
	}

	@PostConstruct
	public void init() {
		log.debug("source url: {}", source.getUrl());
		log.debug("replica url: {}", replica.getUrl());
	}

}