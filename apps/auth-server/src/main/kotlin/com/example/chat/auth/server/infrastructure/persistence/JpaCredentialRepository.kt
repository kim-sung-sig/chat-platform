package com.example.chat.auth.server.infrastructure.persistence

import java.util.*
import org.springframework.data.jpa.repository.JpaRepository

interface JpaCredentialRepository : JpaRepository<CredentialEntity, UUID> {
    fun findByPrincipalIdAndType(principalId: UUID, type: String): Optional<CredentialEntity>
    fun deleteByPrincipalIdAndType(principalId: UUID, type: String)
}
