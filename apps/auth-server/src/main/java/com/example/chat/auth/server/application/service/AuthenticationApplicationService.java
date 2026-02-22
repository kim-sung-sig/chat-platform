package com.example.chat.auth.server.application.service;

import com.example.chat.auth.server.common.exception.AuthException;
import com.example.chat.auth.server.common.exception.AuthServerErrorCode;
import com.example.chat.auth.server.core.domain.*;
import com.example.chat.auth.server.core.domain.credential.Device;
import com.example.chat.auth.server.core.domain.policy.AuthPolicy;
import com.example.chat.auth.server.core.repository.CredentialRepository;
import com.example.chat.auth.server.core.repository.PrincipalRepository;
import com.example.chat.auth.server.core.repository.RefreshTokenRepository;
import com.example.chat.auth.server.core.service.CredentialAuthenticationEngine;
import com.example.chat.auth.server.core.service.TokenService;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.UUID;

/**
 * 인증 Application Service
 */
@Service
@RequiredArgsConstructor
public class AuthenticationApplicationService {

    private final PrincipalRepository principalRepository;
    private final CredentialRepository credentialRepository;
    private final CredentialAuthenticationEngine authenticationEngine;
    private final AuthPolicy authPolicy;
    private final TokenService tokenService;
    private final RefreshTokenRepository refreshTokenRepository;

    /** 인증 실행 결과 */
    @Builder
    public record AuthenticationResult(AuthResult authResult, Token token) {
        public boolean requiresMfa() {
            return authResult.requiresMfa();
        }
    }

    /** 인증 실행 */
    public AuthenticationResult authenticate(
            String identifier,
            CredentialType credentialType,
            Credential providedCredential,
            AuthenticationContext context) {
        // 1️⃣ Principal 로드
        Principal principal = principalRepository.findByIdentifier(identifier)
                .orElseThrow(() -> new AuthException(AuthServerErrorCode.PRINCIPAL_NOT_FOUND));

        // 활성 계정 확인
        if (!principal.isActive()) {
            throw new AuthException(AuthServerErrorCode.PRINCIPAL_INACTIVE);
        }

        // 2️⃣ 저장된 자격증명 검색
        Credential storedCredential = credentialRepository.findByPrincipalId(principal.getId(), credentialType)
                .orElseThrow(() -> new AuthException(AuthServerErrorCode.INVALID_CREDENTIALS));

        // 3️⃣ 자격증명 검증
        AuthResult authResult = authenticationEngine.authenticate(storedCredential, providedCredential, context);

        if (!authResult.authenticated()) {
            throw new AuthException(AuthServerErrorCode.INVALID_CREDENTIALS);
        }

        // 4️⃣ 정책에 따라 MFA 필요 여부 확인
        String mfaSessionId = UUID.randomUUID().toString();
        MfaRequirement mfaRequirement = authPolicy.checkMfaRequirement(context, mfaSessionId);

        if (mfaRequirement.required()) {
            // MFA가 필요 → MFA_PENDING 토큰 발급
            Token mfaToken = tokenService.createMfaPendingToken(
                    principal.getId(),
                    principal.getIdentifier(),
                    authResult.authLevel(),
                    mfaSessionId);

            AuthResult partialResult = AuthResult.partialSuccess(
                    authResult.authLevel(),
                    authResult.completedCredentials(),
                    mfaRequirement);

            return new AuthenticationResult(partialResult, mfaToken);
        }

        // 5️⃣ 최종 성공 → FULL_ACCESS 토큰 발급
        Token fullAccessToken = tokenService.createFullAccessToken(
                principal.getId(),
                principal.getIdentifier(),
                authResult.authLevel(),
                context.getDevice());

        return new AuthenticationResult(authResult, fullAccessToken);
    }

    /** 토큰 갱신 */
    public AuthenticationResult refreshToken(String refreshTokenVal, Device device) {
        Token newToken = tokenService.rotateRefreshToken(refreshTokenVal, device);
        AuthResult authResult = AuthResult.success(newToken.authLevel(), Collections.emptySet());
        return new AuthenticationResult(authResult, newToken);
    }

    /** 로그아웃 */
    public void logout(String refreshToken) {
        if (refreshToken != null && !refreshToken.isEmpty()) {
            refreshTokenRepository.deleteByTokenValue(refreshToken);
        }
    }
}
