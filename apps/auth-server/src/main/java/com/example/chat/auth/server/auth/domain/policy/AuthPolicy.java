package com.example.chat.auth.server.auth.domain.policy;

import com.example.chat.auth.server.auth.domain.AuthLevel;
import com.example.chat.auth.server.auth.domain.AuthenticationContext;
import com.example.chat.auth.server.mfa.domain.MfaRequirement;

/**
 * 인증 정책
 */
public interface AuthPolicy {
    MfaRequirement checkMfaRequirement(AuthenticationContext context, String sessionId);

    AuthLevel requiredAuthLevel(String operation);
}
