package com.example.chat.auth.server.core.domain;

import java.util.Collections;
import java.util.Set;

/**
 * 인증 시도의 결과
 * - "로그인 성공/실패"가 아니라 "현재 인증 상태"를 나타냄
 * - MFA가 필요할 수도 있음
 * - 여러 자격증명으로 부분 인증될 수도 있음
 */
public class AuthResult {

    private final boolean authenticated;
    private final AuthLevel authLevel;
    private final Set<CredentialType> completedCredentials;
    private final MfaRequirement mfaRequirement;
    private final String failureReason;

    // Private constructor - use builders
    private AuthResult(boolean authenticated, AuthLevel authLevel,
                      Set<CredentialType> completedCredentials,
                      MfaRequirement mfaRequirement, String failureReason) {
        this.authenticated = authenticated;
        this.authLevel = authLevel;
        this.completedCredentials = Collections.unmodifiableSet(completedCredentials);
        this.mfaRequirement = mfaRequirement;
        this.failureReason = failureReason;
    }

    public boolean isAuthenticated() {
        return authenticated;
    }

    public AuthLevel getAuthLevel() {
        return authLevel;
    }

    public Set<CredentialType> getCompletedCredentials() {
        return completedCredentials;
    }

    public boolean requiresMfa() {
        return mfaRequirement != null && mfaRequirement.isRequired();
    }

    public MfaRequirement getMfaRequirement() {
        return mfaRequirement;
    }

    public String getFailureReason() {
        return failureReason;
    }

    // ====== Builders ======

    public static AuthResult success(AuthLevel authLevel,
                                    Set<CredentialType> completedCredentials) {
        return new AuthResult(true, authLevel, completedCredentials, null, null);
    }

    public static AuthResult successWithMfa(AuthLevel authLevel,
                                           Set<CredentialType> completedCredentials,
                                           MfaRequirement mfaRequirement) {
        return new AuthResult(true, authLevel, completedCredentials, mfaRequirement, null);
    }

    public static AuthResult partialSuccess(AuthLevel authLevel,
                                           Set<CredentialType> completedCredentials,
                                           MfaRequirement mfaRequirement) {
        return new AuthResult(false, authLevel, completedCredentials, mfaRequirement, null);
    }

    public static AuthResult failure(String reason) {
        return new AuthResult(false, null, Collections.emptySet(), null, reason);
    }

    @Override
    public String toString() {
        return "AuthResult{" +
                "authenticated=" + authenticated +
                ", authLevel=" + authLevel +
                ", completedCredentials=" + completedCredentials +
                ", requiresMfa=" + requiresMfa() +
                ", failureReason='" + failureReason + '\'' +
                '}';
    }
}
