
package com.example.chat.auth.jwt.error

import org.springframework.security.oauth2.core.OAuth2AuthenticationException
import org.springframework.security.oauth2.server.resource.BearerTokenError
import org.springframework.security.oauth2.server.resource.BearerTokenErrorCodes

/**
 * AuthException 생성 헬퍼 유틸
 */
object AuthExceptions {
    const val EXPIRED_INDICATOR = "expired"

    fun of(errorCode: AuthErrorCode): JwtAuthenticationException {
        return JwtAuthenticationException(errorCode)
    }

    fun fromBearerTokenError(error: BearerTokenError): JwtAuthenticationException {
        val code = error.errorCode
        val desc = error.description

        if (BearerTokenErrorCodes.INSUFFICIENT_SCOPE == code) {
            return JwtAuthenticationException(AuthErrorCode.INSUFFICIENT_SCOPE)
        }

        if (BearerTokenErrorCodes.INVALID_TOKEN == code) {
            val expired = isExpired(desc)
            return JwtAuthenticationException(
                if (expired) {
                    AuthErrorCode.EXPIRED_TOKEN
                } else {
                    AuthErrorCode.INVALID_TOKEN
                }
            )
        }

        return JwtAuthenticationException(AuthErrorCode.INVALID_TOKEN)
    }

    fun fromOAuth2Exception(ex: OAuth2AuthenticationException): JwtAuthenticationException {
        val error = ex.error
        if (error is BearerTokenError) {
            return fromBearerTokenError(error)
        }
        return JwtAuthenticationException(AuthErrorCode.INVALID_TOKEN)
    }

    fun isExpired(description: String?): Boolean {
        return description != null && description.lowercase().contains(EXPIRED_INDICATOR)
    }
}
