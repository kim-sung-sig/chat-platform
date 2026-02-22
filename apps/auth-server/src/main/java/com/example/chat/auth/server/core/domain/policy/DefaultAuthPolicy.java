package com.example.chat.auth.server.core.domain.policy;

import com.example.chat.auth.server.core.domain.AuthLevel;
import com.example.chat.auth.server.core.domain.AuthenticationContext;
import com.example.chat.auth.server.core.domain.MfaRequirement;
import com.example.chat.auth.server.core.domain.MfaType;
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
