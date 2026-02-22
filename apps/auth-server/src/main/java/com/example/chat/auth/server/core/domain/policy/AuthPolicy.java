package com.example.chat.auth.server.core.domain.policy;

import com.example.chat.auth.server.core.domain.AuthLevel;
import com.example.chat.auth.server.core.domain.AuthenticationContext;
import com.example.chat.auth.server.core.domain.MfaRequirement;

/**
 * 인증 정책
 */
public interface AuthPolicy {
    MfaRequirement checkMfaRequirement(AuthenticationContext context, String sessionId);

    AuthLevel requiredAuthLevel(String operation);
}
