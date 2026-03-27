package com.example.chat.storage.config.datasource;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * 트랜잭션 readOnly 여부에 따라 DataSource 를 동적으로 라우팅한다.
 *
 * readOnly = true  → REPLICA (읽기 전용 복제본)
 * readOnly = false → SOURCE  (쓰기 가능 주 DB)
 *
 * Spring의 {@link TransactionSynchronizationManager#isCurrentTransactionReadOnly()} 를
 * 직접 참조하므로 별도의 AOP 없이 @Transactional(readOnly = true) 만으로 동작한다.
 */
public class TransactionRoutingDataSource extends AbstractRoutingDataSource {

    @Override
    protected Object determineCurrentLookupKey() {
        return TransactionSynchronizationManager.isCurrentTransactionReadOnly()
                ? DataSourceType.REPLICA
                : DataSourceType.SOURCE;
    }
}
