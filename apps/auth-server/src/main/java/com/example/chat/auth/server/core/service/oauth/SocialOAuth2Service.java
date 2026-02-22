package com.example.chat.auth.server.core.service.oauth;

public interface SocialOAuth2Service {
    SocialType getSocialType();

    OAuth2Data getUserInfo(OAuthRequest oauthRequest);

    String getAccessToken(OAuthRequest oauthRequest);

    OAuth2Data getUserInfo(String accessToken);
}
