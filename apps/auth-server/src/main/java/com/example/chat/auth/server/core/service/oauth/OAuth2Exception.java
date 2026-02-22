package com.example.chat.auth.server.core.service.oauth;

import lombok.Getter;

@Getter
public class OAuth2Exception extends RuntimeException {
    private final OAuth2ErrorCode errorCode;
    private final String messageDetail;

    public OAuth2Exception(OAuth2ErrorCode errorCode, String messageDetail) {
        super(messageDetail);
        this.errorCode = errorCode;
        this.messageDetail = messageDetail;
    }

    public enum OAuth2ErrorCode {
        CLIENT,
        PROVIDER,
        SERVER
    }
}
