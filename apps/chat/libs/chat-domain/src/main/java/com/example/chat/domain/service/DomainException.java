package com.example.chat.domain.service;

/**
 * 도메인 규칙 위반 예외
 *
 * 비즈니스 규칙이 위반되었을 때 발생하는 예외
 * (예: 권한 없음, 상태 불일치 등)
 */
public class DomainException extends RuntimeException {

	public DomainException(String message) {
		super(message);
	}

	public DomainException(String message, Throwable cause) {
		super(message, cause);
	}
}
