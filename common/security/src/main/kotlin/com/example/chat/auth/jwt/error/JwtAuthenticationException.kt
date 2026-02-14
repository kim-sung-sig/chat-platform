package com.example.chat.auth.jwt.error

class JwtAuthenticationException(errorCode: AuthErrorCode) : AuthException(errorCode)
