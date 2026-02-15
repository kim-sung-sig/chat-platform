package com.example.chat.auth.server.core.service

import com.example.chat.auth.server.common.exception.AuthException
import com.example.chat.auth.server.common.exception.AuthServerErrorCode
import com.example.chat.auth.server.core.domain.AuthLevel
import com.example.chat.auth.server.core.domain.AuthResult
import com.example.chat.auth.server.core.domain.AuthenticationContext
import com.example.chat.auth.server.core.domain.CredentialType
import com.example.chat.auth.server.core.domain.credential.OtpCredential
import org.springframework.stereotype.Service

/** OTP 인증 서비스 */
@Service
class OtpService {
    /** OTP 검증 */
    fun verifyOtp(
            providedOtp: OtpCredential,
            storedOtp: OtpCredential,
            context: AuthenticationContext
    ): AuthResult {
        if (providedOtp.code != storedOtp.code) {
            throw AuthException(AuthServerErrorCode.INVALID_MFA_CODE)
        }

        return AuthResult.success(
                authLevel = AuthLevel.MEDIUM,
                completedCredentials = setOf(CredentialType.OTP)
        )
    }

    /** OTP 생성 */
    fun generateOtp(deliveryMethod: String): OtpCredential {
        val code = generateRandomCode()
        return OtpCredential(code, deliveryMethod)
    }

    /** OTP 전송 */
    fun sendOtp(phoneNumber: String?, email: String?, otp: OtpCredential) {
        when (otp.deliveryMethod) {
            "SMS" -> phoneNumber?.let { sendSms(it, otp.code) }
            "EMAIL" -> email?.let { sendEmail(it, otp.code) }
            "APP" -> {
                /* Authenticator app - 사용자가 직접 확인 */
            }
        }
    }

    private fun sendSms(phoneNumber: String, code: String) {
        // TODO: SMS 서비스 통합
    }

    private fun sendEmail(email: String, code: String) {
        // TODO: 이메일 서비스 통합
    }

    private fun generateRandomCode(): String {
        return String.format("%06d", (Math.random() * 1000000).toInt())
    }
}
