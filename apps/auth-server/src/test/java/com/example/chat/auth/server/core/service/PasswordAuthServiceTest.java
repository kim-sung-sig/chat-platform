package com.example.chat.auth.server.core.service;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.chat.auth.server.core.domain.AuthResult;
import com.example.chat.auth.server.core.domain.AuthenticationContext;
import com.example.chat.auth.server.core.domain.CredentialType;
import com.example.chat.auth.server.core.domain.credential.PasswordCredential;

@DisplayName("PasswordAuthService 테스트")
class PasswordAuthServiceTest {

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final PasswordAuthService service = new PasswordAuthService(passwordEncoder);

    @Test
    @DisplayName("올바른 비밀번호로 인증 성공")
    void authenticateWithCorrectPassword() {
        // Given
        String plainPassword = "mySecurePassword123!";
        String hashedPassword = passwordEncoder.encode(plainPassword);
        
        PasswordCredential stored = new PasswordCredential(hashedPassword, true);
        PasswordCredential provided = new PasswordCredential(hashedPassword, plainPassword);
        
        AuthenticationContext context = new AuthenticationContext(
                "192.168.1.1",
                "Mozilla/5.0",
                "WEB",
                Instant.now(),
                false
        );

        // When
        AuthResult result = service.authenticate(stored, provided, context);

        // Then
        assertThat(result.isAuthenticated()).isTrue();
        assertThat(result.getAuthLevel().getLevel()).isEqualTo(1);  // LOW
        assertThat(result.getCompletedCredentials())
                .containsExactly(CredentialType.PASSWORD);
    }

    @Test
    @DisplayName("잘못된 비밀번호로 인증 실패")
    void authenticateWithWrongPassword() {
        // Given
        String plainPassword = "mySecurePassword123!";
        String wrongPassword = "wrongPassword";
        String hashedPassword = passwordEncoder.encode(plainPassword);
        
        PasswordCredential stored = new PasswordCredential(hashedPassword, true);
        PasswordCredential provided = new PasswordCredential(hashedPassword, wrongPassword);
        
        AuthenticationContext context = new AuthenticationContext(
                "192.168.1.1",
                "Mozilla/5.0",
                "WEB",
                Instant.now(),
                false
        );

        // When
        AuthResult result = service.authenticate(stored, provided, context);

        // Then
        assertThat(result.isAuthenticated()).isFalse();
        assertThat(result.getFailureReason()).isEqualTo("Invalid password");
    }
}
