
package com.example.chat.auth.server.core.service

import com.example.chat.auth.server.core.domain.AuthLevel
import com.example.chat.auth.server.core.domain.AuthResult
import com.example.chat.auth.server.core.domain.AuthenticationContext
import com.example.chat.auth.server.core.domain.CredentialType
import com.example.chat.auth.server.core.domain.credential.SocialCredential
import com.example.chat.auth.server.core.service.oauth.SocialOAuth2Service
import com.example.chat.auth.server.core.service.oauth.SocialType
import org.springframework.stereotype.Service

/**
 * 소셜(OAuth) 인증 서비스
 */
@Service
class SocialAuthService(socialServicesList: List<SocialOAuth2Service>) {

    private val socialServices: Map<SocialType, SocialOAuth2Service> = socialServicesList.associateBy { it.socialType }

    /**
     * 소셜 인증 처리
     */
    fun authenticate(
        credential: SocialCredential,
        context: AuthenticationContext
    ): AuthResult {
        val socialType = try {
            SocialType.valueOf(credential.provider.uppercase())
        } catch (e: IllegalArgumentException) {
            return AuthResult.failure("Unsupported social provider: ${credential.provider}")
        }

        val socialOAuth2Service = socialServices[socialType]
            ?: return AuthResult.failure("Social service not configured for: $socialType")

        return try {
            socialOAuth2Service.getUserInfo(credential.socialUserId)
            AuthResult.success(AuthLevel.LOW, setOf(CredentialType.SOCIAL))
        } catch (e: Exception) {
            AuthResult.failure("Invalid social token: ${e.message}")
        }
    }
}
