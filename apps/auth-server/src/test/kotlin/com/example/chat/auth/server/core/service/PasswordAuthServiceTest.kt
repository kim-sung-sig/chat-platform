
package com.example.chat.auth.server.core.service

import com.example.chat.auth.server.core.domain.AuthResult
import com.example.chat.auth.server.core.domain.AuthenticationContext
import com.example.chat.auth.server.core.domain.CredentialType
import com.example.chat.auth.server.core.domain.credential.PasswordCredential
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import java.time.Instant

@DisplayName("PasswordAuthService 테스트")
class PasswordAuthServiceTest {

    private val passwordEncoder = BCryptPasswordEncoder()
    private val service = PasswordAuthService(passwordEncoder)

    @Test
    @DisplayName("올바른 비밀번호로 인증 성공")
    fun authenticateWithCorrectPassword() {
        // Given
        val plainPassword = "mySecurePassword123!"
        val hashedPassword = passwordEncoder.encode(plainPassword)
        
        val stored = PasswordCredential(hashedPassword, null)
        val provided = PasswordCredential(hashedPassword, plainPassword)
        
        val context = AuthenticationContext(
            ipAddress = "192.168.1.1",
            userAgent = "Mozilla/5.0",
            channel = "WEB",
            attemptTime = Instant.now(),
            isSuspiciousActivity = false
        )

        // When
        val result = service.authenticate(stored, provided, context)

        // Then
        assertThat(result.isAuthenticated).isTrue()
        assertThat(result.authLevel?.level).isEqualTo(1) // LOW
        assertThat(result.completedCredentials).containsExactly(CredentialType.PASSWORD)
    }

    @Test
    @DisplayName("잘못된 비밀번호로 인증 실패")
    fun authenticateWithWrongPassword() {
        // Given
        val plainPassword = "mySecurePassword123!"
        val wrongPassword = "wrongPassword"
        val hashedPassword = passwordEncoder.encode(plainPassword)
        
        val stored = PasswordCredential(hashedPassword, null)
        val provided = PasswordCredential(hashedPassword, wrongPassword)
        
        val context = AuthenticationContext(
            ipAddress = "192.168.1.1",
            userAgent = "Mozilla/5.0",
            channel = "WEB",
            attemptTime = Instant.now(),
            isSuspiciousActivity = false
        )

        // When
        val result = service.authenticate(stored, provided, context)

        // Then
        assertThat(result.isAuthenticated).isFalse()
        assertThat(result.failureReason).isEqualTo("Invalid password")
    }
}
