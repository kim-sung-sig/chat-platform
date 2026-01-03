package com.example.chat.auth.jwt.error;

import com.example.chat.common.util.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AuthErrorCode implements ErrorCode {

	INVALID_TOKEN("AUTH_001", "유효하지 않은 토큰", 401),
	EXPIRED_TOKEN("AUTH_002", "만료된 토큰", 401),
	INSUFFICIENT_SCOPE("AUTH_003", "권한이 부족합니다", 403),
	;

	private final String code;
	private final String message;
	private final int status;

}
