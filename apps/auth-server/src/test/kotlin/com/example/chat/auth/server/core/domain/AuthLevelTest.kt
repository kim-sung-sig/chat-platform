
package com.example.chat.auth.server.core.domain

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("AuthLevel 테스트")
class AuthLevelTest {

    @Test
    @DisplayName("LOW < MEDIUM < HIGH")
    fun levelComparison() {
        assertThat(AuthLevel.LOW.level).isLessThan(AuthLevel.MEDIUM.level)
        assertThat(AuthLevel.MEDIUM.level).isLessThan(AuthLevel.HIGH.level)
    }

    @Test
    @DisplayName("HIGH는 모든 수준보다 높음")
    fun highIsHighestLevel() {
        assertThat(AuthLevel.`HIGH`.isHigherOrEqual(AuthLevel.MEDIUM)).isTrue()
        assertThat(AuthLevel.`HIGH`.isHigherOrEqual(AuthLevel.LOW)).isTrue()
        assertThat(AuthLevel.`HIGH`.isHigherOrEqual(AuthLevel.`HIGH`)).isTrue()
    }

    @Test
    @DisplayName("LOW는 다른 모든 수준보다 낮음")
    fun lowIsLowestLevel() {
        assertThat(AuthLevel.LOW.isLowerOrEqual(AuthLevel.MEDIUM)).isTrue()
        assertThat(AuthLevel.LOW.isLowerOrEqual(AuthLevel.`HIGH`)).isTrue()
        assertThat(AuthLevel.LOW.isLowerOrEqual(AuthLevel.LOW)).isTrue()
    }
}
