
package com.example.chat.auth.server.core.domain.policy

import com.example.chat.auth.server.core.domain.AuthLevel

/**
 * 작업별 필요한 인증 수준
 */
enum class OperationAuthLevel(val requiredLevel: AuthLevel, val description: String) {
    // 조회 작업
    VIEW_PROFILE(AuthLevel.LOW, "프로필 조회"),
    VIEW_MESSAGES(AuthLevel.LOW, "메시지 조회"),

    // 수정 작업
    UPDATE_PROFILE(AuthLevel.MEDIUM, "프로필 수정"),
    CHANGE_PASSWORD(AuthLevel.MEDIUM, "비밀번호 변경"),

    // 민감한 작업
    PAYMENT(AuthLevel.HIGH, "결제"),
    TRANSFER_MONEY(AuthLevel.HIGH, "송금"),
    DELETE_ACCOUNT(AuthLevel.HIGH, "계정 삭제"),

    // 관리자 작업
    ADMIN_ACCESS(AuthLevel.HIGH, "관리자 권한");

    /**
     * 현재 인증 수준으로 이 작업을 수행할 수 있는가?
     */
    fun canPerform(currentLevel: AuthLevel): Boolean {
        return currentLevel.isHigherOrEqual(requiredLevel)
    }

    /**
     * 이 작업을 수행하기 위해 필요한 추가 인증 수준
     */
    fun requiredUpgrade(currentLevel: AuthLevel): AuthLevel? {
        if (canPerform(currentLevel)) {
            return null
        }
        return requiredLevel
    }
}
