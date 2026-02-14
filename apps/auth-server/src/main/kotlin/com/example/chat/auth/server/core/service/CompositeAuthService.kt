
package com.example.chat.auth.server.core.service

import com.example.chat.auth.server.core.domain.AuthLevel
import com.example.chat.auth.server.core.domain.AuthResult
import org.springframework.stereotype.Service

/**
 * 복합 인증 서비스
 */
@Service
class CompositeAuthService {

    /**
     * 여러 자격증명 결과를 조합하여 최종 인증 수준 결정
     */
    fun combineResults(first: AuthResult, second: AuthResult): AuthResult {
        if (!first.isAuthenticated || !second.isAuthenticated) {
            return AuthResult.failure("One or more authentication methods failed")
        }

        val combined = first.completedCredentials + second.completedCredentials

        val finalLevel = if (first.authLevel!!.level > second.authLevel!!.level) {
            first.authLevel
        } else {
            second.authLevel
        }

        return AuthResult.success(finalLevel, combined)
    }

    /**
     * 인증 수준 업그레이드
     */
    fun upgradeAuthLevel(current: AuthLevel, additional: AuthResult): AuthLevel {
        return if (additional.authLevel!!.level > current.level) {
            additional.authLevel
        } else {
            current
        }
    }
}
