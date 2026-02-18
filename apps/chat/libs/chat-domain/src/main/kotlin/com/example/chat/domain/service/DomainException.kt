package com.example.chat.domain.service

/**
 * 도메인 규칙 위반 예외
 *
 * 비즈니스 규칙이 위반되었을 때 발생하는 예외
 * (예: 권한 없음, 상태 불일치 등)
 */
class DomainException : RuntimeException {
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)
}

