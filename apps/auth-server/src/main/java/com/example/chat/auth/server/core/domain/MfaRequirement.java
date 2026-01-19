package com.example.chat.auth.server.core.domain;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * MFA 요구사항
 * - "MFA가 필요한가?"를 판단하는 도메인 개념
 * - 어떤 MFA 방식을 요구할지 정의
 * - 이미 완료된 MFA 추적
 */
public class MfaRequirement {

    private final boolean required;
    private final Set<MfaType> requiredMethods;
    private final Set<MfaType> completedMethods;
    private final String sessionId;

    public MfaRequirement(boolean required, Set<MfaType> requiredMethods,
                        Set<MfaType> completedMethods, String sessionId) {
        this.required = required;
        this.requiredMethods = Collections.unmodifiableSet(
                Objects.requireNonNull(requiredMethods, "requiredMethods cannot be null"));
        this.completedMethods = Collections.unmodifiableSet(
                Objects.requireNonNull(completedMethods, "completedMethods cannot be null"));
        this.sessionId = Objects.requireNonNull(sessionId, "sessionId cannot be null");
    }

    public boolean isRequired() {
        return required;
    }

    public Set<MfaType> getRequiredMethods() {
        return requiredMethods;
    }

    public Set<MfaType> getCompletedMethods() {
        return completedMethods;
    }

    public String getSessionId() {
        return sessionId;
    }

    /**
     * 모든 필수 MFA가 완료되었는가?
     */
    public boolean isComplete() {
        return completedMethods.containsAll(requiredMethods);
    }

    /**
     * 남은 MFA 방식
     */
    public Set<MfaType> getRemainingMethods() {
        Set<MfaType> remaining = new HashSet<>(requiredMethods);
        remaining.removeAll(completedMethods);
        return remaining;
    }

    public static MfaRequirement none(String sessionId) {
        return new MfaRequirement(false, Collections.emptySet(), Collections.emptySet(), sessionId);
    }

    public static MfaRequirement otp(String sessionId) {
        Set<MfaType> methods = new HashSet<>();
        methods.add(MfaType.OTP);
        return new MfaRequirement(true, methods, Collections.emptySet(), sessionId);
    }

    public static MfaRequirement withCompleted(String sessionId, Set<MfaType> required,
                                               Set<MfaType> completed) {
        return new MfaRequirement(true, required, completed, sessionId);
    }

    @Override
    public String toString() {
        return "MfaRequirement{" +
                "required=" + required +
                ", requiredMethods=" + requiredMethods +
                ", completedMethods=" + completedMethods +
                ", sessionId='" + sessionId + '\'' +
                '}';
    }
}
