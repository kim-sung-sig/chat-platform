package com.example.chat.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum BaseErrorCode implements ErrorCode {

	// === 0000: 서버 내부 ===
	INTERNAL_SERVER_ERROR("E0000", "Internal server error."),
	SERVICE_UNAVAILABLE("E0001", "The service is currently unavailable."),
	TIMEOUT("E0002", "Request timed out."),
	UNKNOWN_ERROR("E0999", "An unknown error occurred."),

	// === 1000: 인증/인가 ===
	UNAUTHORIZED("E1000", "You are not authorized to access this resource."),
	FORBIDDEN("E1001", "You do not have access to this resource."),
	AUTHENTICATION_FAILED("E1002", "Authentication failed."),

	// === 2000: 요청/검증 ===
	METHOD_NOT_ALLOWED("E2001", "The HTTP method is not allowed."),
	UNSUPPORTED_MEDIA_TYPE("E2002", "The content type is not supported."),
	INVALID_REQUEST("E2003", "The request is invalid."),
	;

	private final String code;
	private final String message;

}