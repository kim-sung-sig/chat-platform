package com.example.chat.auth.server.core.domain.policy;

import org.springframework.stereotype.Component;

import com.example.chat.auth.server.core.domain.AuthLevel;

/**
 * AuthLevel 기반 권한 체크 서비스
 * - role 대신 AuthLevel로 권한 검증
 * - JWT의 auth_level claim으로 판단
 */
@Component
public class AuthLevelAuthorizationService {

    /**
     * 특정 작업을 수행할 권한이 있는가?
     * 
     * @param currentLevel 현재 인증 수준 (JWT에서 추출)
     * @param operation 수행하려는 작업
     * @return true if authorized
     */
    public boolean isAuthorized(AuthLevel currentLevel, OperationAuthLevel operation) {
        return operation.canPerform(currentLevel);
    }

    /**
     * 권한 없을 경우 필요한 인증 수준 반환
     */
    public AuthLevel getRequiredLevel(AuthLevel currentLevel, OperationAuthLevel operation) {
        return operation.requiredUpgrade(currentLevel);
    }

    /**
     * 권한 체크 및 예외 발생
     */
    public void requireAuthLevel(AuthLevel currentLevel, OperationAuthLevel operation) {
        if (!isAuthorized(currentLevel, operation)) {
            AuthLevel required = operation.getRequiredLevel();
            throw new InsufficientAuthLevelException(
                    String.format("작업 '%s'는 %s 수준이 필요합니다 (현재: %s)",
                            operation.getDescription(),
                            required.name(),
                            currentLevel.name())
            );
        }
    }

    /**
     * AuthLevel 부족 예외
     */
    public static class InsufficientAuthLevelException extends RuntimeException {
        public InsufficientAuthLevelException(String message) {
            super(message);
        }
    }
}
