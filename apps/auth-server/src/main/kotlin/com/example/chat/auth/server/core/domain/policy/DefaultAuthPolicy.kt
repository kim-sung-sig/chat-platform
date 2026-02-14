package com.example.chat.auth.server.core.domain.policy

import com.example.chat.auth.server.core.domain.AuthLevel
import com.example.chat.auth.server.core.domain.AuthenticationContext
import com.example.chat.auth.server.core.domain.MfaRequirement
import com.example.chat.auth.server.core.domain.MfaType
import org.springframework.stereotype.Component

/** 기본 인증 정책 구현 */
@Component
class DefaultAuthPolicy : AuthPolicy {

    override fun checkMfaRequirement(
            context: AuthenticationContext,
            sessionId: String
    ): MfaRequirement {
        if (context.isSuspiciousActivity) {
            return MfaRequirement(true, setOf(MfaType.OTP), emptySet(), sessionId)
        }
        return MfaRequirement.none(sessionId)
    }

    override fun requiredAuthLevel(operation: String): AuthLevel {
        return when (operation) {
            "PAYMENT" -> AuthLevel.HIGH
            "PROFILE_UPDATE" -> AuthLevel.MEDIUM
            else -> AuthLevel.LOW
        }
    }
}
