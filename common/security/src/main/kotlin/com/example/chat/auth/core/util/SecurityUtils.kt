
package com.example.chat.auth.core.util

import com.example.chat.auth.core.model.AuthenticatedUser
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import java.util.*

/**
 * Security 유틸리티
 */
object SecurityUtils {
    /**
     * 현재 인증된 사용자 정보 조회
     */
    @JvmStatic
    fun getCurrentUser(): Optional<AuthenticatedUser> {
        val authentication = SecurityContextHolder.getContext().authentication ?: return Optional.empty()
        val principal = authentication.principal
        return if (principal is Jwt) {
            Optional.ofNullable(AuthenticatedUser.from(principal))
        } else {
            Optional.empty()
        }
    }

    /**
     * 현재 사용자 ID 조회
     */
    @JvmStatic
    fun getCurrentUserId(): Optional<String> {
        return getCurrentUser().map { it.userId }
    }

    /**
     * 현재 사용자가 특정 역할을 가지고 있는지 확인
     */
    @JvmStatic
    fun hasRole(role: String): Boolean {
        return getCurrentUser()
            .map { it.hasRole(role) }
            .orElse(false)
    }

    /**
     * 현재 사용자가 관리자인지 확인
     */
    @JvmStatic
    fun isAdmin(): Boolean {
        return getCurrentUser()
            .map { it.isAdmin() }
            .orElse(false)
    }
}
