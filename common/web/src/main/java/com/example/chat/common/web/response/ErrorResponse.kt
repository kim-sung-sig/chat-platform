package com.example.chat.common.web.response

import com.example.chat.common.core.exception.BaseErrorCode
import com.example.chat.common.core.exception.BaseException
import com.example.chat.common.core.exception.ErrorCode
import org.springframework.http.ResponseEntity

/**
 * 에러 응답 DTO
 * 클라이언트에게 일관된 형식의 에러 응답 제공
 */
data class ErrorResponse(
	val status: Int,
	val code: String,
	val message: String,
	val fieldErrors: List<FieldError> = emptyList()
) {
	fun toResponseEntity(): ResponseEntity<ErrorResponse> = ResponseEntity.status(status).body(this)

	companion object {

		fun of(errorCode: ErrorCode) =
			ErrorResponse(
				status = errorCode.status,
				code = errorCode.code,
				message = errorCode.message
			)

		@JvmStatic
		fun of(ex: BaseException) = of(ex.errorCode)

		fun of(fieldErrors: List<FieldError>): ErrorResponse {
			val errorCode = BaseErrorCode.VALIDATION_ERROR
			return ErrorResponse(
				status = errorCode.status,
				code = errorCode.code,
				message = errorCode.message,
				fieldErrors = fieldErrors
			)
		}

	}

	data class FieldError(
		val field: String,
		val rejectedValue: String?,
		val message: String
	)

}
