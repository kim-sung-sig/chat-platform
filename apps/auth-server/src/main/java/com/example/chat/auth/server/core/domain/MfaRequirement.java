package com.example.chat.auth.server.core.domain;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * MFA 요구사항
 */
public record MfaRequirement(
        boolean required,
        Set<MfaType> requiredMethods,
        Set<MfaType> completedMethods,
        String sessionId) {
    /** 모든 필수 MFA가 완료되었는가? */
    public boolean isComplete() {
        return completedMethods.containsAll(requiredMethods);
    }

    /** 남은 MFA 방식 */
    public Set<MfaType> getRemainingMethods() {
        Set<MfaType> remaining = new HashSet<>(requiredMethods);
        remaining.removeAll(completedMethods);
        return Collections.unmodifiableSet(remaining);
    }

    public static MfaRequirement none(String sessionId) {
        return new MfaRequirement(false, Collections.emptySet(), Collections.emptySet(), sessionId);
    }

    public static MfaRequirement otp(String sessionId) {
        return new MfaRequirement(true, Collections.singleton(MfaType.OTP), Collections.emptySet(), sessionId);
    }

    public static MfaRequirement withCompleted(
            String sessionId,
            Set<MfaType> required,
            Set<MfaType> completed) {
        return new MfaRequirement(true, required, completed, sessionId);
    }
}
