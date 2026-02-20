package com.example.chat.auth.server.api.dto.response

import java.util.UUID

data class SignupResponse(
    val principalId: UUID,
    val identifier: String,
    val message: String = "회원가입이 완료되었습니다"
)
