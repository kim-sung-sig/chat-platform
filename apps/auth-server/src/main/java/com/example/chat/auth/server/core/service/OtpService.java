package com.example.chat.auth.server.core.service;

import com.example.chat.auth.server.common.exception.AuthException;
import com.example.chat.auth.server.common.exception.AuthServerErrorCode;
import com.example.chat.auth.server.core.domain.AuthLevel;
import com.example.chat.auth.server.core.domain.AuthResult;
import com.example.chat.auth.server.core.domain.AuthenticationContext;
import com.example.chat.auth.server.core.domain.CredentialType;
import com.example.chat.auth.server.core.domain.credential.OtpCredential;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * OTP 인증 서비스
 */
@Service
public class OtpService {

    /** OTP 검증 */
    public AuthResult verifyOtp(
            OtpCredential providedOtp,
            OtpCredential storedOtp,
            AuthenticationContext context) {
        if (!providedOtp.getCode().equals(storedOtp.getCode())) {
            throw new AuthException(AuthServerErrorCode.INVALID_MFA_CODE);
        }

        return AuthResult.success(
                AuthLevel.MEDIUM,
                Collections.singleton(CredentialType.OTP));
    }

    /** OTP 생성 */
    public OtpCredential generateOtp(String deliveryMethod) {
        String code = generateRandomCode();
        return new OtpCredential(code, deliveryMethod);
    }

    /** OTP 전송 */
    public void sendOtp(String phoneNumber, String email, OtpCredential otp) {
        switch (otp.getDeliveryMethod()) {
            case "SMS" -> {
                if (phoneNumber != null)
                    sendSms(phoneNumber, otp.getCode());
            }
            case "EMAIL" -> {
                if (email != null)
                    sendEmail(email, otp.getCode());
            }
            case "APP" -> {
                /* Authenticator app - 사용자가 직접 확인 */ }
            default -> {
            }
        }
    }

    private void sendSms(String phoneNumber, String code) {
        // TODO: SMS 서비스 통합
    }

    private void sendEmail(String email, String code) {
        // TODO: 이메일 서비스 통합
    }

    private String generateRandomCode() {
        return String.format("%06d", (int) (Math.random() * 1000000));
    }
}
