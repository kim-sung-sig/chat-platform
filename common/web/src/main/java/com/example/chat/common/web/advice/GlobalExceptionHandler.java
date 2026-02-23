package com.example.chat.common.web.advice;

import com.example.chat.common.core.exception.BaseErrorCode;
import com.example.chat.common.core.exception.BaseException;
import com.example.chat.common.web.response.ErrorResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.List;

/**
 * 글로벌 예외 핸들러
 * 모든 예외를 일관된 형식으로 처리
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // =============================
    // 공통 비즈니스 예외 처리
    // =============================

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ErrorResponse> handleBaseException(BaseException ex) {
        log.error("BaseException occurred: {}", ex.getMessage(), ex);
        return ErrorResponse.of(ex).toResponseEntity();
    }

    // =============================
    // 입력 검증 예외 처리
    // =============================

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        log.error("Validation error occurred: {}", ex.getMessage());
        return handleValidationError(ex.getBindingResult());
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ErrorResponse> handleBindException(BindException ex) {
        log.error("Bind error occurred: {}", ex.getMessage(), ex);
        return handleValidationError(ex.getBindingResult());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(ConstraintViolationException ex) {
        log.error("Constraint violation error occurred: {}", ex.getMessage());
        List<ErrorResponse.FieldError> fieldErrors = ex.getConstraintViolations().stream()
                .map((ConstraintViolation<?> violation) -> new ErrorResponse.FieldError(
                        violation.getPropertyPath().toString(),
                        violation.getInvalidValue() != null ? violation.getInvalidValue().toString() : null,
                        violation.getMessage()))
                .toList();
        return ErrorResponse.of(fieldErrors).toResponseEntity();
    }

    // =============================
    // HTTP 예외 처리
    // =============================

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
        log.error("Internal server error: {}", ex.getMessage(), ex);
        return ErrorResponse.of(BaseErrorCode.INTERNAL_SERVER_ERROR).toResponseEntity();
    }

    // =============================
    // 내부 헬퍼
    // =============================

    private ResponseEntity<ErrorResponse> handleValidationError(BindingResult bindingResult) {
        List<ErrorResponse.FieldError> fieldErrors = bindingResult.getFieldErrors().stream()
                .map(error -> new ErrorResponse.FieldError(
                        error.getField(),
                        error.getRejectedValue() != null ? error.getRejectedValue().toString() : null,
                        error.getDefaultMessage()))
                .toList();
        return ErrorResponse.of(fieldErrors).toResponseEntity();
    }
}
