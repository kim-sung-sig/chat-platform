
package com.example.chat.auth.server.core.repository

import com.example.chat.auth.server.core.domain.Principal
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

/**
 * Principal JPA Repository
 */
@Repository
interface PrincipalRepository : JpaRepository<Principal, UUID> {
    /**
     * 식별자(이메일, 사용자명)로 주체 조회
     */
    fun findByIdentifier(identifier: String): Optional<Principal>

    /**
     * 활성 주체인지 확인
     */
    fun existsByIdAndActiveTrue(id: UUID): Boolean
}
