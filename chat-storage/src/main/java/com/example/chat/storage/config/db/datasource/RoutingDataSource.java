package com.example.chat.storage.config.db.datasource;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Slf4j
public class RoutingDataSource extends AbstractRoutingDataSource {

	@Override
	protected Object determineCurrentLookupKey() {
		boolean readOnly = TransactionSynchronizationManager.isCurrentTransactionReadOnly();
		DataSourceType lookupType = readOnly
				? DataSourceType.REPLICA
				: DataSourceType.SOURCE;

		log.debug("Routing to {} dataSource", lookupType.getKey());
		return lookupType.getKey();
	}
}