package com.example.chat.auth.server.core.domain.policy;

import com.example.chat.auth.server.core.domain.AuthLevel;
import com.example.chat.auth.server.core.domain.AuthenticationContext;
import com.example.chat.auth.server.core.domain.MfaRequirement;

/**
 * 인증 정책
 * - "이런 조건이면 MFA가 필요한가?"를 결정
 * - "이런 상황에서 어느 수준의 인증이 필요한가?"를 결정
 *
 * 비즈니스 규칙을 순수 도메인으로 표현
 */
public interface AuthPolicy {

    /**
     * 주어진 컨텍스트에서 MFA가 필요한가?
     */
    MfaRequirement checkMfaRequirement(AuthenticationContext context, String sessionId);

    /**
     * 이 작업에 필요한 최소 인증 수준은?
     */
    AuthLevel requiredAuthLevel(String operation);
}
