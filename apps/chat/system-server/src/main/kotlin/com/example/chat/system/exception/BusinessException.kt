package com.example.chat.system.exception

/**
 * 비즈니스 로직 위반 시 발생하는 예외
 */
class BusinessException(message: String) : RuntimeException(message)
