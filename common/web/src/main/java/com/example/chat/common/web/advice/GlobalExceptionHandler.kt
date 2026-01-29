package com.example.chat.common.web.advice

import com.example.chat.common.core.exception.BaseErrorCode
import com.example.chat.common.core.exception.BaseException
import com.example.chat.common.web.response.ErrorResponse
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.validation.ConstraintViolation
import jakarta.validation.ConstraintViolationException
import org.springframework.http.ResponseEntity
import org.springframework.validation.BindException
import org.springframework.validation.BindingResult
import org.springframework.validation.FieldError
import org.springframework.web.HttpMediaTypeNotSupportedException
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.servlet.NoHandlerFoundException

/**
 * 글로벌 예외 핸들러
 * 모든 예외를 일관된 형식으로 처리
 */
@RestControllerAdvice
class GlobalExceptionHandler {

	private val log = KotlinLogging.logger {}

	/*
	 * =============================
	 * 공통 비지니스 메서드 처리 핸들러
	 * =============================
	 */
	/**
	 * BaseException 처리
	 */
	@ExceptionHandler(BaseException::class)
	fun handleBaseException(ex: BaseException): ResponseEntity<ErrorResponse> =
		ErrorResponse.of(ex).toResponseEntity()
			.also { log.error(ex) { "BaseException occurred: ${ex.message}" } }

	/*
	 * =============================
	 * 입력 검증 관련 처리 헨들러
	 * =============================
	 */
	/**
	 * Validation 예외 처리
	 */
	@ExceptionHandler(MethodArgumentNotValidException::class)
	fun handleMethodArgumentNotValidException(ex: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> =
		handleValidationError(ex.bindingResult)
			.also { log.error { "Validation error occurred: ${ex.message}" } }

	/**
	 * BindException 처리
	 */
	@ExceptionHandler(BindException::class)
	fun handleBindException(ex: BindException): ResponseEntity<ErrorResponse> =
		handleValidationError(ex.bindingResult)
			.also { log.error(ex) { "Bind error occurred: ${ex.message}" } }

	/**
	 * ConstraintViolationException 처리
	 */
	@ExceptionHandler(ConstraintViolationException::class)
	fun handleConstraintViolationException(ex: ConstraintViolationException): ResponseEntity<ErrorResponse> {
		log.error { "Constraint violation error occurred: ${ex.message}"}
		val fieldErrors = convertFieldErrorsFromConstraintViolations(ex)
		return validationErrorResponse(fieldErrors)
	}

	private fun handleValidationError(bindingResult: BindingResult): ResponseEntity<ErrorResponse> {
		val fieldErrors = convertFieldErrorsFromBindingResult(bindingResult)
		return validationErrorResponse(fieldErrors)
	}

	private fun convertFieldErrorsFromBindingResult(bindingResult: BindingResult): List<ErrorResponse.FieldError> =
		bindingResult.fieldErrors
			.map { error: FieldError? ->
				ErrorResponse.FieldError(
					error!!.field,
					error.rejectedValue.toString(),
					error.defaultMessage!!
				)
			}

	private fun convertFieldErrorsFromConstraintViolations(ex: ConstraintViolationException): List<ErrorResponse.FieldError> =
		ex.constraintViolations
			.map { violation: ConstraintViolation<*>? ->
				ErrorResponse.FieldError(
					violation!!.propertyPath.toString(),
					violation.invalidValue.toString(),
					violation.message
				)
			}

	private fun validationErrorResponse(fieldErrors: List<ErrorResponse.FieldError>): ResponseEntity<ErrorResponse> =
		ErrorResponse.of(fieldErrors).toResponseEntity()

	@ExceptionHandler(HttpRequestMethodNotSupportedException::class)
	fun handleMethodNotSupported(ex: HttpRequestMethodNotSupportedException): ResponseEntity<ErrorResponse> =
		ErrorResponse.of(BaseErrorCode.METHOD_NOT_ALLOWED).toResponseEntity()

	@ExceptionHandler(HttpMediaTypeNotSupportedException::class)
	fun handleMediaTypeNotSupported(ex: HttpMediaTypeNotSupportedException): ResponseEntity<ErrorResponse> =
		ErrorResponse.of(BaseErrorCode.UNSUPPORTED_MEDIA_TYPE).toResponseEntity()
			.also { log.error(ex) { "Unsupported media type: ${ex.contentType}" } }

	@ExceptionHandler(NoHandlerFoundException::class)
	fun handleNotFound(ex: NoHandlerFoundException): ResponseEntity<ErrorResponse> =
		ErrorResponse.of(BaseErrorCode.NOT_FOUND).toResponseEntity()
			.also { log.error(ex) { "No handler found: ${ex.httpMethod} ${ex.requestURL}" } }

	@ExceptionHandler(Exception::class)
	fun handleException(ex: Exception): ResponseEntity<ErrorResponse> =
		ErrorResponse.of(BaseErrorCode.INTERNAL_SERVER_ERROR).toResponseEntity()
			.also { log.error(ex) { "Internal server error: ${ex.message}" } }

}
