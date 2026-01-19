package com.example.chat.auth.server.application.service;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.example.chat.auth.server.core.domain.AuthResult;
import com.example.chat.auth.server.core.domain.AuthenticationContext;
import com.example.chat.auth.server.core.domain.Credential;
import com.example.chat.auth.server.core.domain.MfaRequirement;
import com.example.chat.auth.server.core.domain.Principal;
import com.example.chat.auth.server.core.domain.Token;
import com.example.chat.auth.server.core.domain.policy.AuthPolicy;
import com.example.chat.auth.server.core.repository.CredentialRepository;
import com.example.chat.auth.server.core.repository.PrincipalRepository;
import com.example.chat.auth.server.core.service.CredentialAuthenticationEngine;
import com.example.chat.auth.server.core.service.TokenService;

/**
 * 인증 Application Service
 * - 모든 로그인 방식(ID/PW, OAuth, Passkey, SSO)을 통합 처리
 * - 도메인 개념(Principal, Credential, AuthResult, AuthPolicy)만 사용
 * - JWT 토큰 발급
 *
 * 책임:
 * 1. Principal 로드
 * 2. Credential 검증
 * 3. 정책에 따른 MFA 체크
 * 4. JWT 토큰 발급 (AuthLevel에 따라 차등)
 */
@Service
public class AuthenticationApplicationService {

    private final PrincipalRepository principalRepository;
    private final CredentialRepository credentialRepository;
    private final CredentialAuthenticationEngine authenticationEngine;
    private final AuthPolicy authPolicy;
    private final TokenService tokenService;

    public AuthenticationApplicationService(PrincipalRepository principalRepository,
                                          CredentialRepository credentialRepository,
                                          CredentialAuthenticationEngine authenticationEngine,
                                          AuthPolicy authPolicy,
                                          TokenService tokenService) {
        this.principalRepository = principalRepository;
        this.credentialRepository = credentialRepository;
        this.authenticationEngine = authenticationEngine;
        this.authPolicy = authPolicy;
        this.tokenService = tokenService;
    }

    /**
     * 인증 실행 결과
     */
    public static class AuthenticationResult {
        private final AuthResult authResult;
        private final Token token;

        public AuthenticationResult(AuthResult authResult, Token token) {
            this.authResult = authResult;
            this.token = token;
        }

        public AuthResult getAuthResult() {
            return authResult;
        }

        public Token getToken() {
            return token;
        }

        public boolean requiresMfa() {
            return authResult.requiresMfa();
        }
    }

    /**
     * 인증 실행
     * @param identifier 사용자 식별자 (이메일, 사용자명 등)
     * @param credentialType 자격증명 타입
     * @param providedCredential 제공된 자격증명
     * @param context 인증 컨텍스트 (IP, Device, 시간대 등)
     * @return 인증 결과 + JWT 토큰
     */
    public AuthenticationResult authenticate(String identifier,
                                            String credentialType,
                                            Credential providedCredential,
                                            AuthenticationContext context) {

        // 1️⃣ Principal 로드
        Optional<Principal> principalOpt = principalRepository.findByIdentifier(identifier);
        if (principalOpt.isEmpty()) {
            return new AuthenticationResult(
                    AuthResult.failure("User not found"),
                    null
            );
        }

        Principal principal = principalOpt.get();

        // 활성 계정 확인
        if (!principal.isActive()) {
            return new AuthenticationResult(
                    AuthResult.failure("Account is inactive"),
                    null
            );
        }

        // 2️⃣ 저장된 자격증명 검색
        Optional<Credential> storedCredentialOpt = credentialRepository
                .findByPrincipalId(principal.getId(), credentialType);

        if (storedCredentialOpt.isEmpty()) {
            return new AuthenticationResult(
                    AuthResult.failure("No credentials found for this authentication method"),
                    null
            );
        }

        Credential storedCredential = storedCredentialOpt.get();

        // 3️⃣ 자격증명 검증 (적절한 서비스로 라우팅)
        AuthResult authResult = authenticationEngine.authenticate(
                storedCredential,
                providedCredential,
                context
        );

        if (!authResult.isAuthenticated()) {
            return new AuthenticationResult(authResult, null);
        }

        // 4️⃣ 정책에 따라 MFA 필요 여부 확인
        String mfaSessionId = UUID.randomUUID().toString();
        MfaRequirement mfaRequirement = authPolicy.checkMfaRequirement(context, mfaSessionId);

        if (mfaRequirement.isRequired()) {
            // MFA가 필요 → MFA_PENDING 토큰 발급
            Token mfaToken = tokenService.createMfaPendingToken(
                    principal.getId(),
                    principal.getIdentifier(),
                    authResult.getAuthLevel(),
                    mfaSessionId
            );

            AuthResult partialResult = AuthResult.partialSuccess(
                    authResult.getAuthLevel(),
                    authResult.getCompletedCredentials(),
                    mfaRequirement
            );

            return new AuthenticationResult(partialResult, mfaToken);
        }

        // 5️⃣ 최종 성공 → FULL_ACCESS 토큰 발급
        // AuthLevel에 따라 TTL이 차등 적용됨
        Token fullAccessToken = tokenService.createFullAccessToken(
                principal.getId(),
                principal.getIdentifier(),
                authResult.getAuthLevel()
        );

        return new AuthenticationResult(authResult, fullAccessToken);
    }
}
