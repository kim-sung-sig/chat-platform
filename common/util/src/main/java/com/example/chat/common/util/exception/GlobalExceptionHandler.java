package com.example.chat.common.util.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 글로벌 예외 핸들러
 * 모든 예외를 일관된 형식으로 처리
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	/**
	 * BaseException 처리
	 */
	@ExceptionHandler(BaseException.class)
	public ResponseEntity<ErrorResponse> handleBaseException(
			BaseException ex,
			HttpServletRequest request) {
		log.error("BaseException occurred: {}", ex.getMessage(), ex);

		ErrorResponse errorResponse = ErrorResponse.of(ex, request.getRequestURI());
		return errorResponse.toResponseEntity();
	}

	/**
	 * Validation 예외 처리
	 */
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(
			MethodArgumentNotValidException ex,
			HttpServletRequest request) {
		log.error("Validation error occurred: {}", ex.getMessage());

		List<ErrorResponse.FieldError> fieldErrors = ex.getBindingResult()
				.getFieldErrors()
				.stream()
				.map(error -> ErrorResponse.FieldError.builder()
						.field(error.getField())
						.rejectedValue(String.valueOf(error.getRejectedValue()))
						.message(error.getDefaultMessage())
						.build())
				.collect(Collectors.toList());

		ErrorResponse errorResponse = ErrorResponse.builder()
				.code(BaseErrorCode.VALIDATION_ERROR.getCode())
				.message(BaseErrorCode.VALIDATION_ERROR.getMessage())
				.status(BaseErrorCode.VALIDATION_ERROR.getStatus())
				.timestamp(java.time.LocalDateTime.now())
				.path(request.getRequestURI())
				.fieldErrors(fieldErrors)
				.build();

		return errorResponse.toResponseEntity();
	}

	/**
	 * BindException 처리
	 */
	@ExceptionHandler(BindException.class)
	public ResponseEntity<ErrorResponse> handleBindException(
			BindException ex,
			HttpServletRequest request) {
		log.error("Bind error occurred: {}", ex.getMessage());

		List<ErrorResponse.FieldError> fieldErrors = ex.getBindingResult()
				.getFieldErrors()
				.stream()
				.map(error -> ErrorResponse.FieldError.builder()
						.field(error.getField())
						.rejectedValue(String.valueOf(error.getRejectedValue()))
						.message(error.getDefaultMessage())
						.build())
				.collect(Collectors.toList());

		ErrorResponse errorResponse = ErrorResponse.builder()
				.code(BaseErrorCode.VALIDATION_ERROR.getCode())
				.message(BaseErrorCode.VALIDATION_ERROR.getMessage())
				.status(BaseErrorCode.VALIDATION_ERROR.getStatus())
				.timestamp(java.time.LocalDateTime.now())
				.path(request.getRequestURI())
				.fieldErrors(fieldErrors)
				.build();

		return errorResponse.toResponseEntity();
	}

	/**
	 * 그 외 모든 예외 처리
	 */
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleException(
			Exception ex,
			HttpServletRequest request) {
		log.error("Unexpected exception occurred: {}", ex.getMessage(), ex);

		ErrorResponse errorResponse = ErrorResponse.builder()
				.code(BaseErrorCode.INTERNAL_SERVER_ERROR.getCode())
				.message(BaseErrorCode.INTERNAL_SERVER_ERROR.getMessage())
				.status(BaseErrorCode.INTERNAL_SERVER_ERROR.getStatus())
				.timestamp(java.time.LocalDateTime.now())
				.path(request.getRequestURI())
				.build();

		return errorResponse.toResponseEntity();
	}
}
