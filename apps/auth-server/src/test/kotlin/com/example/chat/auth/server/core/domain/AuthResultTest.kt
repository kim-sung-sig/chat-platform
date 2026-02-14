
package com.example.chat.auth.server.core.domain

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("AuthResult 테스트")
class AuthResultTest {

    @Test
    @DisplayName("성공 결과 생성")
    fun successResult() {
        val credentials = setOf(CredentialType.PASSWORD)
        val result = AuthResult.success(AuthLevel.LOW, credentials)

        assertThat(result.isAuthenticated).isTrue()
        assertThat(result.authLevel).isEqualTo(AuthLevel.LOW)
        assertThat(result.completedCredentials).containsExactly(CredentialType.PASSWORD)
        assertThat(result.requiresMfa()).isFalse()
    }

    @Test
    @DisplayName("MFA가 필요한 부분 성공")
    fun partialSuccessWithMfa() {
        val credentials = setOf(CredentialType.PASSWORD)
        val mfa = MfaRequirement.otp("session123")

        val result = AuthResult.partialSuccess(AuthLevel.LOW, credentials, mfa)

        assertThat(result.isAuthenticated).isFalse()
        assertThat(result.requiresMfa()).isTrue()
        assertThat(result.mfaRequirement?.sessionId).isEqualTo("session123")
    }

    @Test
    @DisplayName("실패 결과")
    fun failureResult() {
        val result = AuthResult.failure("Invalid password")

        assertThat(result.isAuthenticated).isFalse()
        assertThat(result.failureReason).isEqualTo("Invalid password")
        assertThat(result.authLevel).isNull()
    }
}
