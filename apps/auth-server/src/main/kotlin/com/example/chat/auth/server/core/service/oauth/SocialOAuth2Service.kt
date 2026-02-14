
package com.example.chat.auth.server.core.service.oauth

interface SocialOAuth2Service {
    val socialType: SocialType
    fun getUserInfo(oauthRequest: OAuthRequest): OAuth2Data
    fun getAccessToken(oauthRequest: OAuthRequest): String
    fun getUserInfo(accessToken: String): OAuth2Data
}
