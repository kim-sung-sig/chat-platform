package com.example.chat.system.service;

import com.example.chat.system.base.IntegrationTestBase;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 기본 통합 테스트 - Docker 환경 필요
 */
@Disabled("Requires Docker - run manually")
@DisplayName("기본 통합 테스트")
class BasicIntegrationTest extends IntegrationTestBase {

    @Test
    @DisplayName("Spring Context 로드 - 성공")
    void contextLoads() {
        // Spring Context가 정상적으로 로드되는지 확인
        assertThat(true).isTrue();
    }

    @Test
    @DisplayName("PostgreSQL Container 실행 - 성공")
    void postgresContainerRunning() {
        assertThat(POSTGRES_CONTAINER.isRunning()).isTrue();
        assertThat(POSTGRES_CONTAINER.getDatabaseName()).isEqualTo("chatdb_test");
    }
}
