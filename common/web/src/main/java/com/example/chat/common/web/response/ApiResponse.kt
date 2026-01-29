package com.example.chat.common.web.response

import com.fasterxml.jackson.annotation.JsonInclude
import org.springframework.http.ResponseEntity

@JsonInclude(JsonInclude.Include.NON_NULL)
data class ApiResponse<T>(
	val status: Int,
	val message: String?,
	val data: T?
) {
	companion object {
		fun <T> ok(data: T?) =
			ApiResponse<T?>(status = 200, message = "OK", data = data)

		fun ok(): ApiResponse<Void?> =
			ok<Void?>(null)

		fun <T> created(data: T?) =
			ApiResponse<T?>(status = 201, message = "Created", data = data)

		fun created(): ApiResponse<Void?> =
			created<Void?>(null)

		fun noContent() =
			 ApiResponse<Void?>(status = 204, message = "No Content", data = null)
	}

	fun toResponseEntity(): ResponseEntity<ApiResponse<T>> =
		ResponseEntity.status(this.status).body(this)
}
