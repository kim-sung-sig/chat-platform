package com.example.chat.auth.server.core.domain;

import java.util.Collections;
import java.util.Set;

/**
 * 인증 시도의 결과
 */
public record AuthResult(
        boolean authenticated,
        AuthLevel authLevel,
        Set<CredentialType> completedCredentials,
        MfaRequirement mfaRequirement,
        String failureReason) {
    public boolean requiresMfa() {
        return mfaRequirement != null && mfaRequirement.required();
    }

    public static AuthResult success(AuthLevel authLevel, Set<CredentialType> completedCredentials) {
        return new AuthResult(true, authLevel, completedCredentials, null, null);
    }

    public static AuthResult successWithMfa(
            AuthLevel authLevel,
            Set<CredentialType> completedCredentials,
            MfaRequirement mfaRequirement) {
        return new AuthResult(true, authLevel, completedCredentials, mfaRequirement, null);
    }

    public static AuthResult partialSuccess(
            AuthLevel authLevel,
            Set<CredentialType> completedCredentials,
            MfaRequirement mfaRequirement) {
        return new AuthResult(false, authLevel, completedCredentials, mfaRequirement, null);
    }

    public static AuthResult failure(String reason) {
        return new AuthResult(false, null, Collections.emptySet(), null, reason);
    }
}
