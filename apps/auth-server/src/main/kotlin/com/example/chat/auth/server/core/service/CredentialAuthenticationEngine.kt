
package com.example.chat.auth.server.core.service

import com.example.chat.auth.server.core.domain.AuthResult
import com.example.chat.auth.server.core.domain.AuthenticationContext
import com.example.chat.auth.server.core.domain.Credential
import com.example.chat.auth.server.core.domain.credential.OtpCredential
import com.example.chat.auth.server.core.domain.credential.PasskeyCredential
import com.example.chat.auth.server.core.domain.credential.PasswordCredential
import com.example.chat.auth.server.core.domain.credential.SocialCredential
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

/**
 * 자격증명 인증 엔진
 */
@Service
class CredentialAuthenticationEngine(
    private val passwordAuthService: PasswordAuthService,
    private val socialAuthService: SocialAuthService,
    private val webAuthnService: WebAuthnService,
    private val otpService: OtpService
) {
    private val log = LoggerFactory.getLogger(CredentialAuthenticationEngine::class.java)

    /**
     * 자격증명 인증
     */
    fun authenticate(
        storedCredential: Credential,
        providedCredential: Credential,
        context: AuthenticationContext
    ): AuthResult {
        return when (providedCredential) {
            is PasswordCredential -> {
                val stored = storedCredential as PasswordCredential
                passwordAuthService.authenticate(stored, providedCredential, context)
            }
            is SocialCredential -> {
                socialAuthService.authenticate(providedCredential, context)
            }
            is PasskeyCredential -> {
                webAuthnService.authenticate(providedCredential, context, "", "", "")
            }
            is OtpCredential -> {
                val stored = storedCredential as OtpCredential
                otpService.verifyOtp(providedCredential, stored, context)
            }
            else -> {
                AuthResult.failure("Unsupported credential type")
            }
        }
    }
}
