package com.example.chat.auth.server.core.service.oauth;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OAuthRequest(
        @NotBlank SocialType provider,
        @NotBlank String code,
        String state,
        String redirectUri) {
    @Builder
    public OAuthRequest {
    }
}
