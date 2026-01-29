package com.example.chat.common.web.response;

import java.util.List;

import org.springframework.http.ResponseEntity;

import com.example.chat.common.core.exception.BaseErrorCode;
import com.example.chat.common.core.exception.BaseException;
import com.example.chat.common.core.exception.ErrorCode;

/**
 * 에러 응답 DTO
 * 클라이언트에게 일관된 형식의 에러 응답 제공
 */
public record ErrorResponse(
		int status, // HTTP 상태 코드
		String code, // 에러 코드
		String message, // 에러 메시지
		List<FieldError> fieldErrors // 필드 에러 리스트
) {
	public ErrorResponse {
		fieldErrors = fieldErrors == null ? List.of() : List.copyOf(fieldErrors);
	}

	public static ErrorResponse of(BaseException ex) {
		return new ErrorResponse(
				ex.getStatus(),
				ex.getCode(),
				ex.getMessage(),
				List.of());
	}

	public static ErrorResponse of(ErrorCode errorCode) {
		return ErrorResponse.of(errorCode, List.of());
	}

	public static ErrorResponse of(List<FieldError> fieldErrors) {
		ErrorCode errorCode = BaseErrorCode.VALIDATION_ERROR;
		return ErrorResponse.of(errorCode, fieldErrors);
	}

	private static ErrorResponse of(ErrorCode errorCode, List<FieldError> fieldErrors) {
		return new ErrorResponse(
				errorCode.getStatus(),
				errorCode.getCode(),
				errorCode.getMessage(),
				fieldErrors);
	}

	public ResponseEntity<ErrorResponse> toResponseEntity() {
		return ResponseEntity
				.status(status)
				.body(this);
	}

	/**
	 * 필드 에러 DTO
	 */
	public record FieldError(
			String field,
			String rejectedValue,
			String message) {
	}

}
