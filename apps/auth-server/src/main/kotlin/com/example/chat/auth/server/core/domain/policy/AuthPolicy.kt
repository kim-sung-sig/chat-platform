package com.example.chat.auth.server.core.domain.policy

import com.example.chat.auth.server.core.domain.AuthLevel
import com.example.chat.auth.server.core.domain.AuthenticationContext
import com.example.chat.auth.server.core.domain.MfaRequirement

/** 인증 정책 */
interface AuthPolicy {
    fun checkMfaRequirement(context: AuthenticationContext, sessionId: String): MfaRequirement
    fun requiredAuthLevel(operation: String): AuthLevel
}
