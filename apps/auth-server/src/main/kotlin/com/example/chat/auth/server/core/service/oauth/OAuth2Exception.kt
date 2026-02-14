package com.example.chat.auth.server.core.service.oauth

class OAuth2Exception(val errorCode: OAuth2ErrorCode, val messageDetail: String) :
        RuntimeException(messageDetail) {

    enum class OAuth2ErrorCode {
        CLIENT,
        PROVIDER,
        SERVER
    }
}
