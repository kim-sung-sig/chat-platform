package com.example.chat.common.web.advice;

import com.example.chat.common.core.exception.BaseErrorCode;
import com.example.chat.common.core.exception.BaseException;
import com.example.chat.common.core.exception.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.HttpRequestMethodNotSupportedException;
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

	/* =============================
	 *  공통 비지니스 메서드 처리 핸들러
	 * ============================= */

	/**
	 * BaseException 처리
	 */
	@ExceptionHandler(BaseException.class)
	public ResponseEntity<ErrorResponse> handleBaseException(BaseException ex, HttpServletRequest request) {
		log.error("BaseException occurred: {}", ex.getMessage(), ex);
		ErrorResponse errorResponse = ErrorResponse.of(ex, request.getRequestURI());
		return toResponseEntity(errorResponse);
	}

	/* =============================
	 *  입력 검증 관련 처리 헨들러
	 * ============================= */

	/**
	 * Validation 예외 처리
	 */
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex, HttpServletRequest request) {
		log.error("Validation error occurred: {}", ex.getMessage());
		return handleValidationError(ex.getBindingResult(), request);
	}

	/**
	 * BindException 처리
	 */
	@ExceptionHandler(BindException.class)
	public ResponseEntity<ErrorResponse> handleBindException(BindException ex, HttpServletRequest request) {
		log.error("Bind error occurred: {}", ex.getMessage());
		return handleValidationError(ex.getBindingResult(), request);
	}

	/**
	 * ConstraintViolationException 처리
	 */
	@ExceptionHandler(ConstraintViolationException.class)
	public ResponseEntity<ErrorResponse> handleConstraintViolationException(ConstraintViolationException ex, HttpServletRequest request) {
		log.error("Constraint violation error occurred: {}", ex.getMessage());

		List<ErrorResponse.FieldError> fieldErrors =
				convertFieldErrorsFromConstraintViolations(ex);

		return validationErrorResponse(fieldErrors, request);
	}

	private ResponseEntity<ErrorResponse> handleValidationError(BindingResult bindingResult, HttpServletRequest request) {
		List<ErrorResponse.FieldError> fieldErrors =
				convertFieldErrorsFromBindingResult(bindingResult);

		return validationErrorResponse(fieldErrors, request);
	}

	private List<ErrorResponse.FieldError> convertFieldErrorsFromBindingResult(BindingResult bindingResult) {
		return bindingResult.getFieldErrors().stream()
				.map(error -> ErrorResponse.FieldError.builder()
						.field(error.getField())
						.rejectedValue(String.valueOf(error.getRejectedValue()))
						.message(error.getDefaultMessage())
						.build())
				.collect(Collectors.toList());
	}

	private List<ErrorResponse.FieldError> convertFieldErrorsFromConstraintViolations(ConstraintViolationException ex) {
		return ex.getConstraintViolations().stream()
				.map(violation -> ErrorResponse.FieldError.builder()
						.field(violation.getPropertyPath().toString())
						.rejectedValue(String.valueOf(violation.getInvalidValue()))
						.message(violation.getMessage())
						.build())
				.collect(Collectors.toList());
	}

	private ResponseEntity<ErrorResponse> validationErrorResponse(List<ErrorResponse.FieldError> fieldErrors, HttpServletRequest request) {
		var errorResponse = ErrorResponse.of(BaseErrorCode.VALIDATION_ERROR, request.getRequestURI())
				.withFieldErrors(fieldErrors);
		return toResponseEntity(errorResponse);
	}


	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	public ResponseEntity<ErrorResponse> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
		var errorResponse = ErrorResponse.of(BaseErrorCode.METHOD_NOT_ALLOWED, request.getRequestURI());
		return toResponseEntity(errorResponse);
	}

	/**
	 * 그 외 모든 예외 처리
	 */
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleException(Exception ex, HttpServletRequest request) {
		log.error("Unexpected exception occurred: {}", ex.getMessage(), ex);

		ErrorResponse errorResponse = ErrorResponse.builder()
				.code(BaseErrorCode.INTERNAL_SERVER_ERROR.getCode())
				.message(BaseErrorCode.INTERNAL_SERVER_ERROR.getMessage())
				.status(BaseErrorCode.INTERNAL_SERVER_ERROR.getStatus())
				.timestamp(java.time.LocalDateTime.now())
				.path(request.getRequestURI())
				.build();

		return toResponseEntity(errorResponse);
	}

	public ResponseEntity<ErrorResponse> toResponseEntity(ErrorResponse errorResponse) {
		return ResponseEntity
				.status(errorResponse.getStatus())
				.body(errorResponse);
	}
}
