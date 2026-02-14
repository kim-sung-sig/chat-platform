package com.example.chat.auth.server.core.repository

import com.example.chat.auth.server.core.domain.Credential
import com.example.chat.auth.server.infrastructure.persistence.CredentialEntity
import com.example.chat.auth.server.infrastructure.persistence.JpaCredentialRepository
import java.util.*
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
class CredentialRepositoryImpl(private val jpaRepository: JpaCredentialRepository) :
        CredentialRepository {
    override fun findByPrincipalId(
            principalId: UUID,
            credentialType: String
    ): Optional<Credential> {
        return jpaRepository.findByPrincipalIdAndType(principalId, credentialType).map {
            it.toDomain()
        }
    }

    @Transactional
    override fun save(principalId: UUID, credential: Credential) {
        // 기존 타입이 있으면 업데이트를 위해 먼저 조회하거나,
        // 혹은 비즈니스 로직상 하나만 존재해야 한다면 기존꺼 삭제 후 저장 등 정책 필요
        // 여기서는 간단하게 principalId + type 으로 조회해서 있으면 ID를 맞춰서 save 함으로써 update 유도
        val existing = jpaRepository.findByPrincipalIdAndType(principalId, credential.type.name)

        val entity = CredentialEntity.fromDomain(principalId, credential)

        // existing이 있으면 ID를 복사해서 업데이트 수행
        if (existing.isPresent) {
            // ID가 val이라 새로 생성해야 함. 혹은 entity 구조를 updatable하게 변경 필요.
            // 일단은 삭제 후 저장 방식으로 단순화 (또는 merge)
            jpaRepository.delete(existing.get())
        }
        jpaRepository.save(entity)
    }

    @Transactional
    override fun delete(principalId: UUID, credentialType: String) {
        jpaRepository.deleteByPrincipalIdAndType(principalId, credentialType)
    }

    override fun hasCredential(principalId: UUID, credentialType: String): Boolean {
        return jpaRepository.findByPrincipalIdAndType(principalId, credentialType).isPresent
    }
}
