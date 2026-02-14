package com.example.chat.auth.server.core.domain

/**
 * 인증 시도의 결과
 * - "로그인 성공/실패"가 아니라 "현재 인증 상태"를 나타냄
 * - MFA가 필요할 수도 있음
 * - 여러 자격증명으로 부분 인증될 수도 있음
 */
data class AuthResult
private constructor(
        val isAuthenticated: Boolean,
        val authLevel: AuthLevel?,
        val completedCredentials: Set<CredentialType>,
        val mfaRequirement: MfaRequirement?,
        val failureReason: String?
) {
    fun requiresMfa(): Boolean {
        return mfaRequirement?.isRequired ?: false
    }

    companion object {
        fun success(authLevel: AuthLevel, completedCredentials: Set<CredentialType>): AuthResult {
            return AuthResult(true, authLevel, completedCredentials, null, null)
        }

        fun successWithMfa(
                authLevel: AuthLevel,
                completedCredentials: Set<CredentialType>,
                mfaRequirement: MfaRequirement
        ): AuthResult {
            return AuthResult(true, authLevel, completedCredentials, mfaRequirement, null)
        }

        fun partialSuccess(
                authLevel: AuthLevel,
                completedCredentials: Set<CredentialType>,
                mfaRequirement: MfaRequirement
        ): AuthResult {
            return AuthResult(false, authLevel, completedCredentials, mfaRequirement, null)
        }

        fun failure(reason: String): AuthResult {
            return AuthResult(false, null, emptySet(), null, reason)
        }
    }
}
