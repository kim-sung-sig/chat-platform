package com.example.chat.auth.jwt.error;

/**
 * JWT 인증 실패 예외
 */
public class JwtAuthenticationException extends AuthException {

    public JwtAuthenticationException(AuthErrorCode errorCode) {
        super(errorCode);
    }
}
