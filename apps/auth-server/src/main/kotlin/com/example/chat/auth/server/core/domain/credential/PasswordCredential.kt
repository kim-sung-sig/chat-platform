
package com.example.chat.auth.server.core.domain.credential

import com.example.chat.auth.server.core.domain.AuthLevel
import com.example.chat.auth.server.core.domain.Credential
import com.example.chat.auth.server.core.domain.CredentialType

/**
 * 비밀번호 자격증명
 */
class PasswordCredential : Credential {
    val hashedPassword: String?
    val plainPassword: String? // 검증 중에만 임시 보관

    constructor(hashedPassword: String?, verified: Boolean) : super(CredentialType.PASSWORD, verified) {
        this.hashedPassword = hashedPassword
        this.plainPassword = null
    }

    // 검증 중에 사용 (임시)
    constructor(hashedPassword: String?, plainPassword: String?) : super(CredentialType.PASSWORD, false) {
        this.hashedPassword = hashedPassword
        this.plainPassword = plainPassword
    }

    override fun minAuthLevel(): AuthLevel = AuthLevel.LOW
}
