
package com.example.chat.auth.server.core.domain.credential

import com.example.chat.auth.server.core.domain.AuthLevel
import com.example.chat.auth.server.core.domain.Credential
import com.example.chat.auth.server.core.domain.CredentialType

/**
 * 소셜 계정 자격증명
 */
class SocialCredential(
    val provider: String,
    val socialUserId: String,
    val email: String?,
    verified: Boolean
) : Credential(CredentialType.SOCIAL, verified) {
    override fun minAuthLevel(): AuthLevel = AuthLevel.LOW
}
