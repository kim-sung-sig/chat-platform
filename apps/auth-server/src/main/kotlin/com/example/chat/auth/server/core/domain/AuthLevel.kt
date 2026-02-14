
package com.example.chat.auth.server.core.domain

/**
 * 인증의 신뢰 수준
 * - 도메인의 핵심 개념
 * - "로그인 성공"이 아니라 "어느 수준으로 인증되었는가"가 중요
 *
 * LOW:    비밀번호 (단일 요소)
 * MEDIUM: 비밀번호 + OTP (이중 요소)
 * HIGH:   패스키 또는 WebAuthn (강력한 인증)
 */
enum class AuthLevel(val level: Int, val description: String) {
    LOW(1, "Password only"),
    MEDIUM(2, "Password + OTP"),
    HIGH(3, "Passkey / WebAuthn");

    /**
     * 이 수준이 다른 수준보다 높은가?
     */
    fun isHigherOrEqual(other: AuthLevel): Boolean {
        return this.level >= other.level
    }

    /**
     * 이 수준이 다른 수준보다 낮은가?
     */
    fun isLowerOrEqual(other: AuthLevel): Boolean {
        return this.level <= other.level
    }
}
