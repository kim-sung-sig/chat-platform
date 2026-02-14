
package com.example.chat.auth.server.core.domain.policy

import com.example.chat.auth.server.core.domain.AuthLevel
import org.springframework.stereotype.Component

/**
 * AuthLevel 기반 권한 체크 서비스
 */
@Component
class AuthLevelAuthorizationService {

    /**
     * 특정 작업을 수행할 권한이 있는가?
     */
    fun isAuthorized(currentLevel: AuthLevel, operation: OperationAuthLevel): Boolean {
        return operation.canPerform(currentLevel)
    }

    /**
     * 권한 없을 경우 필요한 인증 수준 반환
     */
    fun getRequiredLevel(currentLevel: AuthLevel, operation: OperationAuthLevel): AuthLevel? {
        return operation.requiredUpgrade(currentLevel)
    }

    /**
     * 권한 체크 및 예외 발생
     */
    fun requireAuthLevel(currentLevel: AuthLevel, operation: OperationAuthLevel) {
        if (!isAuthorized(currentLevel, operation)) {
            val required = operation.requiredLevel
            throw InsufficientAuthLevelException(
                "작업 '${operation.description}'는 ${required.name} 수준이 필요합니다 (현재: ${currentLevel.name})"
            )
        }
    }

    class InsufficientAuthLevelException(message: String) : RuntimeException(message)
}
