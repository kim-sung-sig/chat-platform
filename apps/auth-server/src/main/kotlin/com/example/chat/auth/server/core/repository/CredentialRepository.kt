package com.example.chat.auth.server.core.repository

import com.example.chat.auth.server.core.domain.Credential
import java.util.*

/** Credential 저장소 */
interface CredentialRepository {
    fun findByPrincipalId(principalId: UUID, credentialType: String): Optional<Credential>

    fun save(principalId: UUID, credential: Credential)

    fun delete(principalId: UUID, credentialType: String)

    fun hasCredential(principalId: UUID, credentialType: String): Boolean
}
