package com.example.chat.auth.server.application.service

import com.example.chat.auth.server.common.exception.AuthException
import com.example.chat.auth.server.common.exception.AuthServerErrorCode
import com.example.chat.auth.server.core.domain.AuthLevel
import com.example.chat.auth.server.core.domain.AuthResult
import com.example.chat.auth.server.core.domain.CredentialType
import com.example.chat.auth.server.core.domain.Token
import com.example.chat.auth.server.core.domain.credential.Device
import com.example.chat.auth.server.core.service.OtpService
import com.example.chat.auth.server.core.service.TokenService
import org.springframework.stereotype.Service
import java.util.*

/** MFA Application Service */
@Service
class MfaApplicationService(
        private val otpService: OtpService,
        private val tokenService: TokenService
) {
    /** MFA 완료 결과 */
    data class MfaCompletionResult(val authResult: AuthResult, val token: Token? = null)

    /** MFA 완료 처리 */
    fun completeMfa(
            mfaToken: String,
            mfaSessionId: String,
            mfaMethod: String,
            code: String,
            device: Device
    ): MfaCompletionResult {
        // 1️⃣ MFA 토큰 검증
        val claims = tokenService.verify(mfaToken)

        // 세션 아이디 확인
        val tokenSessionId = claims.getClaim("mfa_session_id") as? String
        if (tokenSessionId != mfaSessionId) {
            throw AuthException(AuthServerErrorCode.MFA_SESSION_EXPIRED)
        }

        // 2️⃣ OTP 검증 (임시 구현)
        if ("123456" != code) {
            throw AuthException(AuthServerErrorCode.INVALID_MFA_CODE)
        }

        // 3️⃣ MFA 성공 → AuthLevel 격상
        val upgradedLevel = AuthLevel.MEDIUM

        // 4️⃣ 최종 FULL_ACCESS 토큰 발급
        val principalId = UUID.fromString(claims.subject)
        val identifier = claims.getClaim("identifier") as String

        val fullAccessToken =
                tokenService.createFullAccessToken(principalId, identifier, upgradedLevel, device)

        // 5️⃣ 성공 결과 반환
        val finalResult =
                AuthResult.success(
                        upgradedLevel,
                        setOf(CredentialType.PASSWORD, CredentialType.OTP)
                )

        return MfaCompletionResult(finalResult, fullAccessToken)
    }
}
