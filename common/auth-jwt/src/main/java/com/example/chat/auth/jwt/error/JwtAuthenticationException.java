package com.example.chat.auth.jwt.error;

public class JwtAuthenticationException extends AuthException {

	public JwtAuthenticationException(AuthErrorCode errorCode) {
		super(errorCode);
	}
}
