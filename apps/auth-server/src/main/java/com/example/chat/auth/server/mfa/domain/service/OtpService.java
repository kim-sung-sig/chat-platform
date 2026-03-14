package com.example.chat.auth.server.mfa.domain.service;

import com.example.chat.auth.server.shared.exception.AuthException;
import com.example.chat.auth.server.shared.exception.AuthServerErrorCode;
import com.example.chat.auth.server.auth.domain.AuthLevel;
import com.example.chat.auth.server.auth.domain.AuthResult;
import com.example.chat.auth.server.auth.domain.AuthenticationContext;
import com.example.chat.auth.server.auth.domain.CredentialType;
import com.example.chat.auth.server.auth.domain.credential.OtpCredential;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * OTP 인증 서비스
 */
@Service
public class OtpService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final int OTP_TTL_MINUTES = 5;
    private static final int OTP_MAX_ATTEMPTS = 5;
    private static final Map<String, OtpMeta> OTP_META = new ConcurrentHashMap<>();

    private static final class OtpMeta {
        private final Instant expiresAt;
        private final AtomicInteger attempts = new AtomicInteger(0);

        private OtpMeta(Instant expiresAt) {
            this.expiresAt = expiresAt;
        }
    }

    /** OTP 검증 */
    public AuthResult verifyOtp(
            OtpCredential providedOtp,
            OtpCredential storedOtp,
            AuthenticationContext context) {
        String key = buildKey(storedOtp);
        OtpMeta meta = OTP_META.get(key);
        if (meta == null || meta.expiresAt.isBefore(Instant.now())) {
            OTP_META.remove(key);
            throw new AuthException(AuthServerErrorCode.MFA_SESSION_EXPIRED);
        }
        if (meta.attempts.get() >= OTP_MAX_ATTEMPTS) {
            OTP_META.remove(key);
            throw new AuthException(AuthServerErrorCode.ACCESS_DENIED);
        }
        if (!providedOtp.getCode().equals(storedOtp.getCode())) {
            meta.attempts.incrementAndGet();
            throw new AuthException(AuthServerErrorCode.INVALID_MFA_CODE);
        }

        OTP_META.remove(key);
        return AuthResult.success(
                AuthLevel.MEDIUM,
                Collections.singleton(CredentialType.OTP));
    }

    /** OTP 생성 */
    public OtpCredential generateOtp(String deliveryMethod) {
        String code = generateRandomCode();
        Instant expiresAt = Instant.now().plus(OTP_TTL_MINUTES, ChronoUnit.MINUTES);
        OTP_META.put(buildKey(code, deliveryMethod), new OtpMeta(expiresAt));
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
        int code = SECURE_RANDOM.nextInt(1_000_000);
        return String.format("%06d", code);
    }

    private String buildKey(OtpCredential credential) {
        return buildKey(credential.getCode(), credential.getDeliveryMethod());
    }

    private String buildKey(String code, String deliveryMethod) {
        return deliveryMethod + ":" + code;
    }
}
