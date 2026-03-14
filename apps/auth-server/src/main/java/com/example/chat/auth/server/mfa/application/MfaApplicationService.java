package com.example.chat.auth.server.mfa.application;

import com.example.chat.auth.server.shared.exception.AuthException;
import com.example.chat.auth.server.shared.exception.AuthServerErrorCode;
import com.example.chat.auth.server.auth.domain.AuthLevel;
import com.example.chat.auth.server.auth.domain.AuthResult;
import com.example.chat.auth.server.auth.domain.CredentialType;
import com.example.chat.auth.server.token.domain.Token;
import com.example.chat.auth.server.auth.domain.credential.Device;
import com.example.chat.auth.server.mfa.domain.service.OtpService;
import com.example.chat.auth.server.token.domain.service.TokenService;
import com.nimbusds.jwt.JWTClaimsSet;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.UUID;

/**
 * MFA Application Service
 */
@Service
@RequiredArgsConstructor
public class MfaApplicationService {

    private final OtpService otpService;
    private final TokenService tokenService;

    /** MFA 완료 결과 */
    public record MfaCompletionResult(AuthResult authResult, Token token) {
    }

    /** MFA 완료 처리 */
    public MfaCompletionResult completeMfa(
            String mfaToken,
            String mfaSessionId,
            String mfaMethod,
            String code,
            Device device) {
        // 1️⃣ MFA 토큰 검증
        JWTClaimsSet claims = tokenService.verifyMfaToken(mfaToken);

        // 세션 아이디 확인
        String tokenSessionId = (String) claims.getClaim("mfa_session_id");
        if (tokenSessionId == null || !tokenSessionId.equals(mfaSessionId)) {
            throw new AuthException(AuthServerErrorCode.MFA_SESSION_EXPIRED);
        }

        // 2️⃣ OTP 검증 (임시 구현)
        if (!"123456".equals(code)) {
            throw new AuthException(AuthServerErrorCode.INVALID_MFA_CODE);
        }

        // 3️⃣ MFA 성공 → AuthLevel 격상
        AuthLevel upgradedLevel = AuthLevel.MEDIUM;

        // 4️⃣ 최종 FULL_ACCESS 토큰 발급
        UUID principalId = UUID.fromString(claims.getSubject());
        String identifier = (String) claims.getClaim("identifier");

        Token fullAccessToken = tokenService.createFullAccessToken(principalId, identifier, upgradedLevel, device);

        // 5️⃣ 성공 결과 반환
        AuthResult finalResult = AuthResult.success(
                upgradedLevel,
                Set.of(CredentialType.PASSWORD, CredentialType.OTP));

        return new MfaCompletionResult(finalResult, fullAccessToken);
    }
}
