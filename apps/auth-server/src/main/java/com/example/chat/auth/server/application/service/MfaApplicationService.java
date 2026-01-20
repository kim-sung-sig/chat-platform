package com.example.chat.auth.server.application.service;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.example.chat.auth.server.core.domain.AuthLevel;
import com.example.chat.auth.server.core.domain.AuthResult;
import com.example.chat.auth.server.core.domain.Token;
import com.example.chat.auth.server.core.domain.credential.Device;
import com.example.chat.auth.server.core.service.OtpService;
import com.example.chat.auth.server.core.service.TokenService;

/**
 * MFA Application Service
 * - MFA 검증 및 최종 토큰 발급
 */
@Service
public class MfaApplicationService {

    private final OtpService otpService;
    private final TokenService tokenService;

    public MfaApplicationService(OtpService otpService, TokenService tokenService) {
        this.otpService = otpService;
        this.tokenService = tokenService;
    }

    /**
     * MFA 완료 결과
     */
    public static class MfaCompletionResult {
        private final AuthResult authResult;
        private final Token token;

        public MfaCompletionResult(AuthResult authResult, Token token) {
            this.authResult = authResult;
            this.token = token;
        }

        public AuthResult getAuthResult() {
            return authResult;
        }

        public Token getToken() {
            return token;
        }
    }

    /**
     * MFA 완료 처리
     * 
     * @param mfaToken     MFA_PENDING 토큰 (검증용)
     * @param mfaSessionId MFA 세션 ID
     * @param mfaMethod    MFA 방식
     * @param code         OTP 코드
     * @return 최종 인증 결과 + FULL_ACCESS 토큰
     */
    public MfaCompletionResult completeMfa(String mfaToken, String mfaSessionId,
            String mfaMethod, String code, Device device) {
        // 1️⃣ MFA 토큰 검증
        try {
            tokenService.verify(mfaToken);
        } catch (Exception e) {
            return new MfaCompletionResult(
                    AuthResult.failure("Invalid or expired MFA token"),
                    null);
        }

        // 2️⃣ OTP 검증 (실제로는 저장된 OTP와 비교)
        // TODO: MFA 세션에서 저장된 OTP 로드
        // AuthResult otpResult = otpService.verifyOtp(providedOtp, storedOtp, context);

        // 임시 구현
        if (!"123456".equals(code)) {
            return new MfaCompletionResult(
                    AuthResult.failure("Invalid OTP code"),
                    null);
        }

        // 3️⃣ MFA 성공 → AuthLevel 격상
        // LOW → MEDIUM (비밀번호 + OTP)
        AuthLevel upgradedLevel = AuthLevel.MEDIUM;

        // 4️⃣ 최종 FULL_ACCESS 토큰 발급
        // TODO: Principal 정보를 MFA 토큰에서 추출
        UUID principalId = UUID.randomUUID(); // 임시
        String identifier = "user@example.com"; // 임시

        Token fullAccessToken = tokenService.createFullAccessToken(
                principalId,
                identifier,
                upgradedLevel,
                device);

        // 5️⃣ 성공 결과 반환
        Set<String> completedCredentials = new HashSet<>();
        completedCredentials.add("PASSWORD");
        completedCredentials.add("OTP");

        AuthResult finalResult = AuthResult.success(
                upgradedLevel,
                Set.of(com.example.chat.auth.server.core.domain.CredentialType.PASSWORD,
                        com.example.chat.auth.server.core.domain.CredentialType.OTP));

        return new MfaCompletionResult(finalResult, fullAccessToken);
    }
}
