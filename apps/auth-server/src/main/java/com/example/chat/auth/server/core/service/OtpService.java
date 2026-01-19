package com.example.chat.auth.server.core.service;

import java.util.HashSet;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.example.chat.auth.server.core.domain.AuthLevel;
import com.example.chat.auth.server.core.domain.AuthResult;
import com.example.chat.auth.server.core.domain.AuthenticationContext;
import com.example.chat.auth.server.core.domain.CredentialType;
import com.example.chat.auth.server.core.domain.credential.OtpCredential;

/**
 * OTP 인증 서비스
 * - OTP 생성, 전송, 검증
 * - MFA의 핵심 구성요소
 * - SMS, Email, Authenticator App 지원
 */
@Service
public class OtpService {

    /**
     * OTP 검증
     */
    public AuthResult verifyOtp(OtpCredential providedOtp,
                               OtpCredential storedOtp,
                               AuthenticationContext context) {
        // 실제 구현: OTP 검증 (시간 기반, 유효성, 일회용 여부)
        if (!providedOtp.getCode().equals(storedOtp.getCode())) {
            return AuthResult.failure("Invalid OTP");
        }

        Set<CredentialType> completed = new HashSet<>();
        completed.add(CredentialType.OTP);

        // OTP 검증 성공 → MEDIUM 수준으로 격상
        return AuthResult.success(AuthLevel.MEDIUM, completed);
    }

    /**
     * OTP 생성
     */
    public OtpCredential generateOtp(String deliveryMethod) {
        // TODO: 실제 OTP 생성 로직
        String code = generateRandomCode();
        return new OtpCredential(code, deliveryMethod);
    }

    /**
     * OTP 전송
     */
    public void sendOtp(String phoneNumber, String email, OtpCredential otp) {
        switch (otp.getDeliveryMethod()) {
            case "SMS":
                sendSmS(phoneNumber, otp.getCode());
                break;
            case "EMAIL":
                sendEmail(email, otp.getCode());
                break;
            case "APP":
                // Authenticator app - 사용자가 직접 확인
                break;
        }
    }

    private void sendSmS(String phoneNumber, String code) {
        // TODO: SMS 서비스 통합
    }

    private void sendEmail(String email, String code) {
        // TODO: 이메일 서비스 통합
    }

    private String generateRandomCode() {
        // TODO: 6자리 또는 8자리 난수 생성
        return String.format("%06d", (int)(Math.random() * 1000000));
    }
}
