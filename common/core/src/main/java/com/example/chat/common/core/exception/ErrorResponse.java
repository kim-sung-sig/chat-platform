package com.example.chat.common.core.exception;

import lombok.Builder;
import lombok.Getter;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * ?ëŸ¬ ?‘ë‹µ DTO
 * ?´ë¼?´ì–¸?¸ì—ê²??¼ê????•ì‹???ëŸ¬ ?‘ë‹µ ?œê³µ
 */
@Getter
public class ErrorResponse {

	private final String code;
	private final String message;
	private final int status;
	private final LocalDateTime timestamp;
	private final String path;

	@Builder
	private ErrorResponse(String code, String message, int status, LocalDateTime timestamp, String path) {
		this.code = code;
		this.message = message;
		this.status = status;
		this.timestamp = timestamp;
		this.path = path;
	}

	private final List<FieldError> fieldErrors = new ArrayList<>();

	/**
	 * ?„ë“œ ?ëŸ¬ DTO
	 */
	@Getter
	@Builder
	public static class FieldError {
		private final String field;
		private final String rejectedValue;
		private final String message;
	}

	/**
	 * BaseException?¼ë¡œë¶€??ErrorResponse ?ì„±
	 */
	public static ErrorResponse of(BaseException ex, String path) {
		return ErrorResponse.builder()
				.code(ex.getCode())
				.message(ex.getMessage())
				.status(ex.getStatus())
				.timestamp(LocalDateTime.now())
				.path(path)
				.build();
	}

	public static ErrorResponse of(ErrorCode errorCode, String path) {
		return ErrorResponse.builder()
				.code(errorCode.getCode())
				.message(errorCode.getMessage())
				.status(errorCode.getStatus())
				.timestamp(LocalDateTime.now())
				.path(path)
				.build();
	}

	public ErrorResponse withFieldErrors(List<FieldError> fieldErrors) {
		this.fieldErrors.clear();
		this.fieldErrors.addAll(fieldErrors);
		return this;
	}

	/**
	 * ErrorResponseë¥?ResponseEntityë¡?ë³€??
	 */
	public ResponseEntity<ErrorResponse> toResponseEntity() {
		return ResponseEntity
				.status(this.status)
				.body(this);
	}
}
