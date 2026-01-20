package com.example.chat.auth.server.core.service.oauth;

public interface SocialOAuth2Service {

    SocialType getSocialType();

    OAuth2Data getUserInfo(OAuthRequest oauthRequest) throws OAuth2Exception;

    String getAccessToken(OAuthRequest oauthRequest) throws OAuth2Exception;

    OAuth2Data getUserInfo(String accessToken) throws OAuth2Exception;

}
