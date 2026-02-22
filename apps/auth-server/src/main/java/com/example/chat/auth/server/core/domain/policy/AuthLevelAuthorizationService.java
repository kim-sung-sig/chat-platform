package com.example.chat.auth.server.core.domain.policy;

import com.example.chat.auth.server.common.exception.AuthException;
import com.example.chat.auth.server.common.exception.AuthServerErrorCode;
import com.example.chat.auth.server.core.domain.AuthLevel;
import org.springframework.stereotype.Component;

/**
 * AuthLevel 기반 권한 체크 서비스
 */
@Component
public class AuthLevelAuthorizationService {

    /** 특정 작업을 수행할 권한이 있는가? */
    public boolean isAuthorized(AuthLevel currentLevel, OperationAuthLevel operation) {
        return operation.canPerform(currentLevel);
    }

    /** 권한 없을 경우 필요한 인증 수준 반환 */
    public AuthLevel getRequiredLevel(AuthLevel currentLevel, OperationAuthLevel operation) {
        return operation.requiredUpgrade(currentLevel);
    }

    /** 권한 체크 및 에러 발생 */
    public void requireAuthLevel(AuthLevel currentLevel, OperationAuthLevel operation) {
        if (!isAuthorized(currentLevel, operation)) {
            AuthLevel required = operation.getRequiredLevel();
            throw new AuthException(
                    AuthServerErrorCode.INSUFFICIENT_AUTH_LEVEL,
                    new Object[] { operation.getDescription(), required.name(), currentLevel.name() });
        }
    }
}
