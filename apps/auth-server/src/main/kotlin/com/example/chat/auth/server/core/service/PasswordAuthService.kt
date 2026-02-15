package com.example.chat.auth.server.core.service

import com.example.chat.auth.server.common.exception.AuthException
import com.example.chat.auth.server.common.exception.AuthServerErrorCode
import com.example.chat.auth.server.core.domain.AuthLevel
import com.example.chat.auth.server.core.domain.AuthResult
import com.example.chat.auth.server.core.domain.AuthenticationContext
import com.example.chat.auth.server.core.domain.CredentialType
import com.example.chat.auth.server.core.domain.credential.PasswordCredential
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

/** 비밀번호 인증 서비스 */
@Service
class PasswordAuthService(private val passwordEncoder: PasswordEncoder) {
    /** 비밀번호 검증 */
    fun authenticate(
            storedCredential: PasswordCredential,
            providedCredential: PasswordCredential,
            context: AuthenticationContext
    ): AuthResult {
        val matches =
                passwordEncoder.matches(
                        providedCredential.plainPassword,
                        storedCredential.hashedPassword
                )

        if (!matches) {
            throw AuthException(AuthServerErrorCode.INVALID_CREDENTIALS)
        }

        return AuthResult.success(
                authLevel = AuthLevel.LOW,
                completedCredentials = setOf(CredentialType.PASSWORD)
        )
    }

    /** 비밀번호 해싱 */
    fun hashPassword(plainPassword: String): String {
        return passwordEncoder.encode(plainPassword)
    }
}
