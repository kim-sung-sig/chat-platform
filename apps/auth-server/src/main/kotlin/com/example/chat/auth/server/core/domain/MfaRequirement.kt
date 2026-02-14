package com.example.chat.auth.server.core.domain

/**
 * MFA 요구사항
 * - "MFA가 필요한가?"를 판단하는 도메인 개념
 * - 어떤 MFA 방식을 요구할지 정의
 * - 이미 완료된 MFA 추적
 */
data class MfaRequirement(
        val isRequired: Boolean,
        val requiredMethods: Set<MfaType>,
        val completedMethods: Set<MfaType>,
        val sessionId: String
) {
    /** 모든 필수 MFA가 완료되었는가? */
    fun isComplete(): Boolean {
        return completedMethods.containsAll(requiredMethods)
    }

    /** 남은 MFA 방식 */
    fun getRemainingMethods(): Set<MfaType> {
        return requiredMethods - completedMethods
    }

    companion object {
        fun none(sessionId: String): MfaRequirement {
            return MfaRequirement(false, emptySet(), emptySet(), sessionId)
        }

        fun otp(sessionId: String): MfaRequirement {
            return MfaRequirement(true, setOf(MfaType.OTP), emptySet(), sessionId)
        }

        fun withCompleted(
                sessionId: String,
                required: Set<MfaType>,
                completed: Set<MfaType>
        ): MfaRequirement {
            return MfaRequirement(true, required, completed, sessionId)
        }
    }
}
