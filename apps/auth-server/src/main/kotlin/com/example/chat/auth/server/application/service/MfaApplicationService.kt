
package com.example.chat.auth.server.application.service

import com.example.chat.auth.server.core.domain.*
import com.example.chat.auth.server.core.domain.credential.Device
import com.example.chat.auth.server.core.service.OtpService
import com.example.chat.auth.server.core.service.TokenService
import org.springframework.stereotype.Service
import java.util.*

/**
 * MFA Application Service
 */
@Service
class MfaApplicationService(
    private val otpService: OtpService,
    private val tokenService: TokenService
) {

    /**
     * MFA 완료 결과
     */
    data class MfaCompletionResult(
        val authResult: AuthResult,
        val token: Token? = null
    )

    /**
     * MFA 완료 처리
     */
    fun completeMfa(
        mfaToken: String,
        mfaSessionId: String,
        mfaMethod: String,
        code: String,
        device: Device
    ): MfaCompletionResult {
        // 1️⃣ MFA 토큰 검증
        return try {
            tokenService.verify(mfaToken)

            // 2️⃣ OTP 검증 (임시 구현)
            if ("123456" != code) {
                return MfaCompletionResult(AuthResult.failure("Invalid OTP code"))
            }

            // 3️⃣ MFA 성공 → AuthLevel 격상
            val upgradedLevel = AuthLevel.MEDIUM

            // 4️⃣ 최종 FULL_ACCESS 토큰 발급
            // TODO: Principal 정보를 MFA 토큰에서 추출
            val principalId = UUID.randomUUID() // 임시
            val identifier = "user@example.com" // 임시

            val fullAccessToken = tokenService.createFullAccessToken(
                principalId,
                identifier,
                upgradedLevel,
                device
            )

            // 5️⃣ 성공 결과 반환
            val finalResult = AuthResult.success(
                upgradedLevel,
                setOf(CredentialType.PASSWORD, CredentialType.OTP)
            )

            MfaCompletionResult(finalResult, fullAccessToken)
        } catch (e: Exception) {
            MfaCompletionResult(AuthResult.failure("Invalid or expired MFA token"))
        }
    }
}
