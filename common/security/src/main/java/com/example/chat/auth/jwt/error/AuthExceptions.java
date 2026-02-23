package com.example.chat.auth.jwt.error;

import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.server.resource.BearerTokenError;
import org.springframework.security.oauth2.server.resource.BearerTokenErrorCodes;

/**
 * AuthException 생성 팩토리
 */
public final class AuthExceptions {

    private static final String EXPIRED_INDICATOR = "expired";

    private AuthExceptions() {
    }

    public static JwtAuthenticationException of(AuthErrorCode errorCode) {
        return new JwtAuthenticationException(errorCode);
    }

    public static JwtAuthenticationException fromBearerTokenError(BearerTokenError error) {
        String code = error.getErrorCode();
        String desc = error.getDescription();

        if (BearerTokenErrorCodes.INSUFFICIENT_SCOPE.equals(code)) {
            return new JwtAuthenticationException(AuthErrorCode.INSUFFICIENT_SCOPE);
        }

        if (BearerTokenErrorCodes.INVALID_TOKEN.equals(code)) {
            AuthErrorCode authErrorCode = isExpired(desc)
                    ? AuthErrorCode.EXPIRED_TOKEN
                    : AuthErrorCode.INVALID_TOKEN;
            return new JwtAuthenticationException(authErrorCode);
        }

        return new JwtAuthenticationException(AuthErrorCode.INVALID_TOKEN);
    }

    public static JwtAuthenticationException fromOAuth2Exception(OAuth2AuthenticationException ex) {
        if (ex.getError() instanceof BearerTokenError bearerTokenError) {
            return fromBearerTokenError(bearerTokenError);
        }
        return new JwtAuthenticationException(AuthErrorCode.INVALID_TOKEN);
    }

    public static boolean isExpired(String description) {
        return description != null && description.toLowerCase().contains(EXPIRED_INDICATOR);
    }
}
