package com.example.chat.auth.server.core.domain

/** 인증 시도의 결과 */
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
            return AuthResult(
                    isAuthenticated = true,
                    authLevel = authLevel,
                    completedCredentials = completedCredentials,
                    mfaRequirement = null,
                    failureReason = null
            )
        }

        fun successWithMfa(
                authLevel: AuthLevel,
                completedCredentials: Set<CredentialType>,
                mfaRequirement: MfaRequirement
        ): AuthResult {
            return AuthResult(
                    isAuthenticated = true,
                    authLevel = authLevel,
                    completedCredentials = completedCredentials,
                    mfaRequirement = mfaRequirement,
                    failureReason = null
            )
        }

        fun partialSuccess(
                authLevel: AuthLevel,
                completedCredentials: Set<CredentialType>,
                mfaRequirement: MfaRequirement
        ): AuthResult {
            return AuthResult(
                    isAuthenticated = false,
                    authLevel = authLevel,
                    completedCredentials = completedCredentials,
                    mfaRequirement = mfaRequirement,
                    failureReason = null
            )
        }

        fun failure(reason: String): AuthResult {
            return AuthResult(
                    isAuthenticated = false,
                    authLevel = null,
                    completedCredentials = emptySet(),
                    mfaRequirement = null,
                    failureReason = reason
            )
        }
    }
}
