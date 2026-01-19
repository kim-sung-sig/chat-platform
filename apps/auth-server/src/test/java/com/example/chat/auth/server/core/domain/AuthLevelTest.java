package com.example.chat.auth.server.core.domain;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("AuthLevel 테스트")
class AuthLevelTest {

    @Test
    @DisplayName("LOW < MEDIUM < HIGH")
    void levelComparison() {
        assertThat(AuthLevel.LOW.getLevel()).isLessThan(AuthLevel.MEDIUM.getLevel());
        assertThat(AuthLevel.MEDIUM.getLevel()).isLessThan(AuthLevel.HIGH.getLevel());
    }

    @Test
    @DisplayName("HIGH는 모든 수준보다 높음")
    void highIsHighestLevel() {
        assertThat(AuthLevel.HIGH.isHigherOrEqual(AuthLevel.MEDIUM)).isTrue();
        assertThat(AuthLevel.HIGH.isHigherOrEqual(AuthLevel.LOW)).isTrue();
        assertThat(AuthLevel.HIGH.isHigherOrEqual(AuthLevel.HIGH)).isTrue();
    }

    @Test
    @DisplayName("LOW는 다른 모든 수준보다 낮음")
    void lowIsLowestLevel() {
        assertThat(AuthLevel.LOW.isLowerOrEqual(AuthLevel.MEDIUM)).isTrue();
        assertThat(AuthLevel.LOW.isLowerOrEqual(AuthLevel.HIGH)).isTrue();
        assertThat(AuthLevel.LOW.isLowerOrEqual(AuthLevel.LOW)).isTrue();
    }
}
