package com.example.chat.common.core.exception

open class BaseException(
	val errorCode: ErrorCode,
	val args: Array<out Any?>? = null,
	cause: Throwable? = null
) : RuntimeException(errorCode.message, cause)
