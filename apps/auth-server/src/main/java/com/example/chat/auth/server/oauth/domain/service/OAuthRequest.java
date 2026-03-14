package com.example.chat.auth.server.oauth.domain.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.validation.constraints.NotBlank;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OAuthRequest(
        @NotBlank SocialType provider,
        @NotBlank String code,
        String state,
        String redirectUri) {

    public OAuthRequest {
    }
}
