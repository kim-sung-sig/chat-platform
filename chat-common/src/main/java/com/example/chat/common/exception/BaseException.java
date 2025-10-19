package com.example.chat.common.exception;

import lombok.Getter;

import java.util.Map;

@Getter
public class BaseException extends RuntimeException {

	private final ErrorCode errorCode;
	private final Map<String, Object> details;

	public BaseException(ErrorCode errorCode) {
		super();
		this.errorCode = errorCode;
		this.details = Map.of();
	}

	public BaseException(ErrorCode errorCode, String message) {
		super(message);
		this.errorCode = errorCode;
		this.details = Map.of();
	}

	public BaseException(ErrorCode errorCode, String message, Map<String, Object> details) {
		super(message);
		this.errorCode = errorCode;
		this.details = (details == null) ? Map.of() : Map.copyOf(details);
	}
}