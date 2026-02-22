package com.example.chat.auth.server.core.service.oauth;

public interface OAuth2Data {
    String getProvider();

    String getProviderId();

    String getEmail();

    String getName();

    String getNickName();
}
