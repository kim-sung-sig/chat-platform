package com.example.chat.common.web.advice;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import com.example.chat.common.core.exception.BaseErrorCode;
import com.example.chat.common.core.exception.BaseException;
import com.example.chat.common.web.response.ErrorResponse;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;

/**
 * 글로벌 예외 핸들러
 * 모든 예외를 일관된 형식으로 처리
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	/*
	 * =============================
	 * 공통 비지니스 메서드 처리 핸들러
	 * =============================
	 */

	/**
	 * BaseException 처리
	 */
	@ExceptionHandler(BaseException.class)
	public ResponseEntity<ErrorResponse> handleBaseException(BaseException ex) {
		log.error("BaseException occurred: {}", ex.getMessage(), ex);
		return ErrorResponse.of(ex).toResponseEntity();
	}

	/*
	 * =============================
	 * 입력 검증 관련 처리 헨들러
	 * =============================
	 */

	/**
	 * Validation 예외 처리
	 */
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
		log.error("Validation error occurred: {}", ex.getMessage());
		return handleValidationError(ex.getBindingResult());
	}

	/**
	 * BindException 처리
	 */
	@ExceptionHandler(BindException.class)
	public ResponseEntity<ErrorResponse> handleBindException(BindException ex) {
		log.error("Bind error occurred: {}", ex.getMessage());
		return handleValidationError(ex.getBindingResult());
	}

	/**
	 * ConstraintViolationException 처리
	 */
	@ExceptionHandler(ConstraintViolationException.class)
	public ResponseEntity<ErrorResponse> handleConstraintViolationException(ConstraintViolationException ex) {
		log.error("Constraint violation error occurred: {}", ex.getMessage());

		List<ErrorResponse.FieldError> fieldErrors = convertFieldErrorsFromConstraintViolations(ex);

		return validationErrorResponse(fieldErrors);
	}

	private ResponseEntity<ErrorResponse> handleValidationError(BindingResult bindingResult) {
		List<ErrorResponse.FieldError> fieldErrors = convertFieldErrorsFromBindingResult(bindingResult);

		return validationErrorResponse(fieldErrors);
	}

	private List<ErrorResponse.FieldError> convertFieldErrorsFromBindingResult(BindingResult bindingResult) {
		return bindingResult.getFieldErrors().stream()
				.map(error -> new ErrorResponse.FieldError(
						error.getField(),
						String.valueOf(error.getRejectedValue()),
						error.getDefaultMessage()))
				.collect(Collectors.toList());
	}

	private List<ErrorResponse.FieldError> convertFieldErrorsFromConstraintViolations(ConstraintViolationException ex) {
		return ex.getConstraintViolations().stream()
				.map(violation -> new ErrorResponse.FieldError(
						violation.getPropertyPath().toString(),
						String.valueOf(violation.getInvalidValue()),
						violation.getMessage()))
				.collect(Collectors.toList());
	}

	private ResponseEntity<ErrorResponse> validationErrorResponse(List<ErrorResponse.FieldError> fieldErrors) {
		return ErrorResponse.of(fieldErrors).toResponseEntity();
	}

	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	public ResponseEntity<ErrorResponse> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
		return ErrorResponse.of(BaseErrorCode.METHOD_NOT_ALLOWED).toResponseEntity();
	}

	@ExceptionHandler(HttpMediaTypeNotSupportedException.class)
	public ResponseEntity<ErrorResponse> handleMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex) {
		log.error("Unsupported media type: {}", ex.getContentType(), ex);
		return ErrorResponse.of(BaseErrorCode.UNSUPPORTED_MEDIA_TYPE).toResponseEntity();
	}

	@ExceptionHandler(NoHandlerFoundException.class)
	public ResponseEntity<ErrorResponse> handleNotFound(NoHandlerFoundException ex) {
		log.error("No handler found: {} {}", ex.getHttpMethod(), ex.getRequestURL(), ex);
		return ErrorResponse.of(BaseErrorCode.NOT_FOUND).toResponseEntity();
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleException(Exception ex) {
		log.error("Unexpected exception occurred: {}", ex.getMessage(), ex);
		return ErrorResponse.of(BaseErrorCode.INTERNAL_SERVER_ERROR).toResponseEntity();
	}
}
