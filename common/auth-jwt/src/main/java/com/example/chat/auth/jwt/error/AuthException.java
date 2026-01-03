package com.example.chat.auth.jwt.error;

import com.example.chat.common.util.exception.BaseException;

/**
 * 인증/인가 관련 비즈니스 예외의 기본 클래스
 * 프로젝트의 공통 BaseException을 상속하여 ErrorResponse 변환을 일관화합니다.
 */
public abstract class AuthException extends BaseException {

	protected AuthException(AuthErrorCode errorCode) {
		super(errorCode);
	}

}
