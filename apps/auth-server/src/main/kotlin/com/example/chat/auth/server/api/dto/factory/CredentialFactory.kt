
package com.example.chat.auth.server.api.dto.factory

import com.example.chat.auth.server.api.dto.request.AuthenticateRequest
import com.example.chat.auth.server.core.domain.Credential
import com.example.chat.auth.server.core.domain.credential.OtpCredential
import com.example.chat.auth.server.core.domain.credential.PasswordCredential
import com.example.chat.auth.server.core.domain.credential.SocialCredential
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Component

/**
 * Credential Factory
 */
@Component
class CredentialFactory {

    private val objectMapper = ObjectMapper()

    fun createFromRequest(request: AuthenticateRequest): Credential {
        val type = request.credentialType ?: throw IllegalArgumentException("credentialType is required")
        val data = request.credentialData ?: throw IllegalArgumentException("credentialData is required")

        return when (type.uppercase()) {
            "PASSWORD" -> createPasswordCredential(data)
            "SOCIAL" -> createSocialCredential(data)
            "OTP" -> createOtpCredential(data)
            "PASSKEY" -> throw UnsupportedOperationException("Passkey not yet implemented")
            else -> throw IllegalArgumentException("Unknown credential type: $type")
        }
    }

    private fun createPasswordCredential(plainPassword: String): Credential {
        return PasswordCredential(null, plainPassword)
    }

    private fun createSocialCredential(jsonData: String): Credential {
        return try {
            val json = objectMapper.readTree(jsonData)
            val provider = json.get("provider").asText()
            val token = json.get("token").asText()
            val email = if (json.has("email")) json.get("email").asText() else null

            val socialUserId = extractUserIdFromToken(provider, token)
            SocialCredential(provider, socialUserId, email, false)
        } catch (e: Exception) {
            throw IllegalArgumentException("Invalid social credential data", e)
        }
    }

    private fun createOtpCredential(jsonData: String): Credential {
        return try {
            val json = objectMapper.readTree(jsonData)
            val code = json.get("code").asText()
            val deliveryMethod = json.get("deliveryMethod").asText()

            OtpCredential(code, deliveryMethod)
        } catch (e: Exception) {
            throw IllegalArgumentException("Invalid OTP credential data", e)
        }
    }

    private fun extractUserIdFromToken(provider: String, token: String): String {
        return "social-user-" + token.substring(0, token.length.coerceAtMost(10))
    }
}
