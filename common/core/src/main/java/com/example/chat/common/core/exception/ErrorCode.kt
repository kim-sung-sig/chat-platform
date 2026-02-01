package com.example.chat.common.core.exception

/**
 * 에러 코드 인터페이스
 */
interface ErrorCode {
	val code: String
	val message: String
	val status: Int
}
