package com.example.chat.auth.server.application.service

import com.example.chat.auth.server.common.exception.AuthErrorCode
import com.example.chat.auth.server.common.exception.AuthException
import com.example.chat.auth.server.core.domain.*
import com.example.chat.auth.server.core.domain.credential.Device
import com.example.chat.auth.server.core.domain.policy.AuthPolicy
import com.example.chat.auth.server.core.repository.CredentialRepository
import com.example.chat.auth.server.core.repository.PrincipalRepository
import com.example.chat.auth.server.core.repository.RefreshTokenRepository
import com.example.chat.auth.server.core.service.CredentialAuthenticationEngine
import com.example.chat.auth.server.core.service.TokenService
import java.util.*
import org.springframework.stereotype.Service

/** 인증 Application Service */
@Service
class AuthenticationApplicationService(
        private val principalRepository: PrincipalRepository,
        private val credentialRepository: CredentialRepository,
        private val authenticationEngine: CredentialAuthenticationEngine,
        private val authPolicy: AuthPolicy,
        private val tokenService: TokenService,
        private val refreshTokenRepository: RefreshTokenRepository
) {
    /** 인증 실행 결과 */
    data class AuthenticationResult(val authResult: AuthResult, val token: Token? = null) {

        fun requiresMfa(): Boolean = authResult.requiresMfa()
    }

    /** 인증 실행 */
    fun authenticate(
            identifier: String,
            credentialType: String,
            providedCredential: Credential,
            context: AuthenticationContext
    ): AuthenticationResult {
        // 1️⃣ Principal 로드
        val principal =
                principalRepository.findByIdentifier(identifier).orElse(null)
                        ?: throw AuthException(AuthErrorCode.PRINCIPAL_NOT_FOUND)

        // 활성 계정 확인
        if (!principal.active) {
            throw AuthException(AuthErrorCode.PRINCIPAL_INACTIVE)
        }

        // 2️⃣ 저장된 자격증명 검색
        val storedCredential =
                credentialRepository.findByPrincipalId(principal.id, credentialType).orElse(null)
                        ?: throw AuthException(AuthErrorCode.INVALID_CREDENTIALS)

        // 3️⃣ 자격증명 검증
        val authResult =
                authenticationEngine.authenticate(storedCredential, providedCredential, context)

        if (!authResult.isAuthenticated) {
            throw AuthException(AuthErrorCode.INVALID_CREDENTIALS)
        }

        // 4️⃣ 정책에 따라 MFA 필요 여부 확인
        val mfaSessionId = UUID.randomUUID().toString()
        val mfaRequirement = authPolicy.checkMfaRequirement(context, mfaSessionId)

        if (mfaRequirement.isRequired) {
            // MFA가 필요 → MFA_PENDING 토큰 발급
            val mfaToken =
                    tokenService.createMfaPendingToken(
                            principal.id,
                            principal.identifier,
                            authResult.authLevel!!,
                            mfaSessionId
                    )

            val partialResult =
                    AuthResult.partialSuccess(
                            authResult.authLevel,
                            authResult.completedCredentials,
                            mfaRequirement
                    )

            return AuthenticationResult(partialResult, mfaToken)
        }

        // 5️⃣ 최종 성공 → FULL_ACCESS 토큰 발급
        val fullAccessToken =
                tokenService.createFullAccessToken(
                        principal.id,
                        principal.identifier,
                        authResult.authLevel!!,
                        context.getDevice()
                )

        return AuthenticationResult(authResult, fullAccessToken)
    }

    /** 토큰 갱신 */
    fun refreshToken(refreshTokenVal: String, device: Device): AuthenticationResult {
        val newToken = tokenService.rotateRefreshToken(refreshTokenVal, device)
        val authResult = AuthResult.success(newToken.authLevel, emptySet())
        return AuthenticationResult(authResult, newToken)
    }

    /** 로그아웃 */
    fun logout(refreshToken: String?) {
        if (!refreshToken.isNullOrEmpty()) {
            refreshTokenRepository.deleteByTokenValue(refreshToken)
        }
    }
}
