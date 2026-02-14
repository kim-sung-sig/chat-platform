package com.example.chat.auth.core.model

import org.springframework.security.oauth2.jwt.Jwt

/** 인증된 사용자 정보 */
data class AuthenticatedUser(
        val userId: String,
        val email: String?,
        val roles: List<String> = emptyList()
) {
    companion object {
        /** JWT로부터 AuthenticatedUser 생성 */
        fun from(jwt: Jwt?): AuthenticatedUser? {
            if (jwt == null) return null

            return AuthenticatedUser(
                    userId = jwt.subject,
                    email = jwt.getClaimAsString("email"),
                    roles = jwt.getClaimAsStringList("roles") ?: emptyList()
            )
        }
    }

    /** 특정 역할을 가지고 있는지 확인 */
    fun hasRole(role: String): Boolean {
        return roles.contains(role)
    }

    /** 관리자 여부 확인 */
    fun isAdmin(): Boolean {
        return hasRole("ROLE_ADMIN")
    }
}
