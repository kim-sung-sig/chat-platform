package com.example.chat.auth.server.core.domain.policy;

import com.example.chat.auth.server.core.domain.AuthLevel;
import com.example.chat.auth.server.core.domain.AuthenticationContext;
import com.example.chat.auth.server.core.domain.MfaRequirement;
import com.example.chat.auth.server.core.domain.MfaType;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * 기본 인증 정책 구현
 * - 의심스러운 활동이나 특정 채널에서는 MFA 요구
 */
@Component
public class DefaultAuthPolicy implements AuthPolicy {

    @Override
    public MfaRequirement checkMfaRequirement(AuthenticationContext context, String sessionId) {
        // 의심스러운 활동 감지 → MFA 필수
        if (context.isSuspiciousActivity()) {
            Set<MfaType> required = new HashSet<>();
            required.add(MfaType.OTP);
            return new MfaRequirement(true, required, Collections.emptySet(), sessionId);
        }

        // 기본적으로 MFA 불필요 (추후 정책 확장 가능)
        return MfaRequirement.none(sessionId);
    }

    @Override
    public AuthLevel requiredAuthLevel(String operation) {
        // 작업별 필요한 인증 수준 정의
        return switch (operation) {
            case "PAYMENT" -> AuthLevel.HIGH;      // 결제는 높은 수준 필요
            case "PROFILE_UPDATE" -> AuthLevel.MEDIUM;  // 프로필 수정은 중간
            default -> AuthLevel.LOW;               // 일반 조회는 낮은 수준
        };
    }
}
