package com.example.chat.auth.server.core.service

import com.example.chat.auth.server.common.exception.AuthErrorCode
import com.example.chat.auth.server.common.exception.AuthException
import com.example.chat.auth.server.core.domain.AuthLevel
import com.example.chat.auth.server.core.domain.AuthResult
import com.example.chat.auth.server.core.domain.AuthenticationContext
import com.example.chat.auth.server.core.domain.CredentialType
import com.example.chat.auth.server.core.domain.credential.PasskeyCredential
import java.util.*
import org.springframework.stereotype.Service

/** WebAuthn / Passkey 인증 서비스 */
@Service
class WebAuthnService {
    /** Passkey 검증 */
    fun authenticate(
            credential: PasskeyCredential,
            context: AuthenticationContext,
            challenge: String,
            clientData: String,
            attestationObject: String
    ): AuthResult {
        if (!verifySignature(credential, challenge, clientData, attestationObject)) {
            throw AuthException(AuthErrorCode.INVALID_CREDENTIALS)
        }

        return AuthResult.success(
                authLevel = AuthLevel.HIGH,
                completedCredentials = setOf(CredentialType.PASSKEY)
        )
    }

    /** Challenge 생성 */
    fun generateChallenge(): String {
        return UUID.randomUUID().toString()
    }

    /** 서명 검증 (실제 구현 필요) */
    private fun verifySignature(
            credential: PasskeyCredential,
            challenge: String,
            clientData: String,
            attestationObject: String
    ): Boolean {
        // TODO: WebAuthn 라이브러리로 검증
        return true
    }
}
