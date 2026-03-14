package com.example.chat.auth.server.auth.domain.policy;

import com.example.chat.auth.server.auth.domain.AuthLevel;
import com.example.chat.auth.server.auth.domain.AuthenticationContext;
import com.example.chat.auth.server.mfa.domain.MfaRequirement;
import com.example.chat.auth.server.mfa.domain.MfaType;
import org.springframework.stereotype.Component;

import java.util.Collections;

/**
 * 기본 인증 정책 구현
 */
@Component
public class DefaultAuthPolicy implements AuthPolicy {
    @Override
    public MfaRequirement checkMfaRequirement(AuthenticationContext context, String sessionId) {
        if (context.suspiciousActivity()) {
            return MfaRequirement.withCompleted(
                    sessionId,
                    Collections.singleton(MfaType.OTP),
                    Collections.emptySet());
        }
        return MfaRequirement.none(sessionId);
    }

    @Override
    public AuthLevel requiredAuthLevel(String operation) {
        return switch (operation) {
            case "PAYMENT" -> AuthLevel.HIGH;
            case "PROFILE_UPDATE" -> AuthLevel.MEDIUM;
            default -> AuthLevel.LOW;
        };
    }
}
