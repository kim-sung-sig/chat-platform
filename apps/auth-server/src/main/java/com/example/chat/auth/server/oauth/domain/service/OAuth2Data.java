package com.example.chat.auth.server.oauth.domain.service;

public interface OAuth2Data {
    String getProvider();

    String getProviderId();

    String getEmail();

    String getName();

    String getNickName();
}
