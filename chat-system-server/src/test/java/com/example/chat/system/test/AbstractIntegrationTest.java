package com.example.chat.system.test;

import org.junit.jupiter.api.BeforeAll;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

/**
 * 통합 테스트 베이스 클래스
 *
 * PostgreSQL 및 Redis 컨테이너를 자동으로 시작하고 관리합니다.
 * 모든 통합 테스트는 이 클래스를 상속받아야 합니다.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers
public abstract class AbstractIntegrationTest {

    /**
     * PostgreSQL 컨테이너
     */
    @Container
    protected static final PostgreSQLContainer<?> POSTGRES_CONTAINER =
            new PostgreSQLContainer<>(DockerImageName.parse("postgres:15-alpine"))
                    .withDatabaseName("chat_test")
                    .withUsername("test")
                    .withPassword("test")
                    .withReuse(true);

    /**
     * Redis 컨테이너
     */
    @Container
    protected static final GenericContainer<?> REDIS_CONTAINER =
            new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
                    .withExposedPorts(6379)
                    .withReuse(true);

    /**
     * 컨테이너 시작 확인
     */
    @BeforeAll
    static void beforeAll() {
        POSTGRES_CONTAINER.start();
        REDIS_CONTAINER.start();
    }

    /**
     * Spring Boot 애플리케이션에 컨테이너 설정 주입
     */
    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        // PostgreSQL 설정
        registry.add("spring.datasource.url", POSTGRES_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES_CONTAINER::getUsername);
        registry.add("spring.datasource.password", POSTGRES_CONTAINER::getPassword);

        // Redis 설정
        registry.add("spring.data.redis.host", REDIS_CONTAINER::getHost);
        registry.add("spring.data.redis.port", () -> REDIS_CONTAINER.getMappedPort(6379).toString());

        // JPA 설정 (테스트 환경)
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.jpa.show-sql", () -> "true");

        // Quartz 설정 (메모리 모드)
        registry.add("spring.quartz.job-store-type", () -> "memory");
    }
}
