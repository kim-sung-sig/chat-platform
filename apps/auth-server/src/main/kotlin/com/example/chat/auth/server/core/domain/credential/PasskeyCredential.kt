
package com.example.chat.auth.server.core.domain.credential

import com.example.chat.auth.server.core.domain.AuthLevel
import com.example.chat.auth.server.core.domain.Credential
import com.example.chat.auth.server.core.domain.CredentialType

/**
 * Passkey / WebAuthn 자격증명
 */
class PasskeyCredential(
    val credentialId: String,
    val publicKey: String,
    val authenticatorName: String?,
    verified: Boolean
) : Credential(CredentialType.PASSKEY, verified) {
    override fun minAuthLevel(): AuthLevel = AuthLevel.HIGH
}
