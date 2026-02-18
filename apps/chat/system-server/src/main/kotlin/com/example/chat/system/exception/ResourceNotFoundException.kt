package com.example.chat.system.exception

/**
 * 리소스를 찾을 수 없을 때 발생하는 예외
 */
class ResourceNotFoundException(message: String) : RuntimeException(message)
