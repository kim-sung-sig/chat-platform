
package com.example.chat.auth.server.core.domain.credential

import com.example.chat.auth.server.core.domain.AuthLevel
import com.example.chat.auth.server.core.domain.Credential
import com.example.chat.auth.server.core.domain.CredentialType

/**
 * OTP 자격증명
 * - SMS, Email, Authenticator App으로 전달되는 일회용 코드
 */
class OtpCredential(
    val code: String,
    val deliveryMethod: String
) : Credential(CredentialType.OTP, false) {
    override fun minAuthLevel(): AuthLevel = AuthLevel.LOW
}
