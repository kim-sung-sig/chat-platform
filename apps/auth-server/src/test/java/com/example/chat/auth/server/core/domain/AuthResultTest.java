package com.example.chat.auth.server.core.domain;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("AuthResult 테스트")
class AuthResultTest {

    @Test
    @DisplayName("성공 결과 생성")
    void successResult() {
        Set<CredentialType> credentials = new HashSet<>();
        credentials.add(CredentialType.PASSWORD);

        AuthResult result = AuthResult.success(AuthLevel.LOW, credentials);

        assertThat(result.isAuthenticated()).isTrue();
        assertThat(result.getAuthLevel()).isEqualTo(AuthLevel.LOW);
        assertThat(result.getCompletedCredentials()).containsExactly(CredentialType.PASSWORD);
        assertThat(result.requiresMfa()).isFalse();
    }

    @Test
    @DisplayName("MFA가 필요한 부분 성공")
    void partialSuccessWithMfa() {
        Set<CredentialType> credentials = new HashSet<>();
        credentials.add(CredentialType.PASSWORD);
        MfaRequirement mfa = MfaRequirement.otp("session123");

        AuthResult result = AuthResult.partialSuccess(AuthLevel.LOW, credentials, mfa);

        assertThat(result.isAuthenticated()).isFalse();  // 완전 성공 아님
        assertThat(result.requiresMfa()).isTrue();
        assertThat(result.getMfaRequirement().getSessionId()).isEqualTo("session123");
    }

    @Test
    @DisplayName("실패 결과")
    void failureResult() {
        AuthResult result = AuthResult.failure("Invalid password");

        assertThat(result.isAuthenticated()).isFalse();
        assertThat(result.getFailureReason()).isEqualTo("Invalid password");
        assertThat(result.getAuthLevel()).isNull();
    }
}
