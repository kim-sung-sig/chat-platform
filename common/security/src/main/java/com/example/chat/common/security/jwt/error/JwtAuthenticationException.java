package com.example.chat.common.security.jwt.error;

public class JwtAuthenticationException extends AuthException {

	public JwtAuthenticationException(AuthErrorCode errorCode) {
		super(errorCode);
	}
}
