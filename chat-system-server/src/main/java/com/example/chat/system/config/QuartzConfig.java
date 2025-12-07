package com.example.chat.system.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import javax.sql.DataSource;
import java.util.Properties;

/**
 * Quartz Scheduler 설정
 * JDBC JobStore를 사용하여 클러스터 모드 지원
 */
@Configuration
public class QuartzConfig {

    @Bean
    public SchedulerFactoryBean schedulerFactoryBean(DataSource dataSource) {
        SchedulerFactoryBean factory = new SchedulerFactoryBean();
        factory.setDataSource(dataSource);
        factory.setQuartzProperties(quartzProperties());
        factory.setWaitForJobsToCompleteOnShutdown(true);
        factory.setAutoStartup(true);

        return factory;
    }

    private Properties quartzProperties() {
        Properties properties = new Properties();

        // Scheduler 설정
        properties.setProperty("org.quartz.scheduler.instanceName", "ChatScheduler");
        properties.setProperty("org.quartz.scheduler.instanceId", "AUTO");

        // ThreadPool 설정
        properties.setProperty("org.quartz.threadPool.threadCount", "10");
        properties.setProperty("org.quartz.threadPool.class",
            "org.quartz.simpl.SimpleThreadPool");

        // JobStore 설정 (JDBC)
        properties.setProperty("org.quartz.jobStore.class",
            "org.quartz.impl.jdbcjobstore.JobStoreTX");
        properties.setProperty("org.quartz.jobStore.driverDelegateClass",
            "org.quartz.impl.jdbcjobstore.PostgreSQLDelegate");
        properties.setProperty("org.quartz.jobStore.tablePrefix", "QRTZ_");
        properties.setProperty("org.quartz.jobStore.dataSource", "quartzDataSource");

        // Cluster 설정 (멀티 인스턴스 지원)
        properties.setProperty("org.quartz.jobStore.isClustered", "true");
        properties.setProperty("org.quartz.jobStore.clusterCheckinInterval", "20000");

        return properties;
    }
}
