
package com.example.chat.auth.server.infrastructure.oauth

import com.example.chat.auth.server.core.service.oauth.*
import com.example.chat.common.core.constants.HeaderConstants
import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatusCode
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestClient

@Component
@ConditionalOnProperty(prefix = "oauth.naver", name = ["client-id"])
class NaverOAuth2Service(
    @Value("\${oauth.naver.client-id}") private val naverClientId: String,
    @Value("\${oauth.naver.client-secret}") private val naverClientSecret: String
) : SocialOAuth2Service {

    private val log = LoggerFactory.getLogger(NaverOAuth2Service::class.java)
    private val restClient = RestClient.create()

    @PostConstruct
    fun init() {
        log.debug("naverClientId: {}", naverClientId)
        log.debug("naverClientSecret: {}", naverClientSecret)
    }

    override val socialType: SocialType
        get() = SocialType.NAVER

    override fun getUserInfo(oauthRequest: OAuthRequest): OAuth2Data {
        val accessToken = getAccessToken(oauthRequest)
        return getUserInfo(accessToken)
    }

    override fun getAccessToken(oauthRequest: OAuthRequest): String {
        val params = LinkedMultiValueMap<String, String>().apply {
            add("grant_type", "authorization_code")
            add("client_id", naverClientId)
            add("client_secret", naverClientSecret)
            add("code", oauthRequest.code)
            oauthRequest.state?.let { add("state", it) }
        }

        val response = restClient.post()
            .uri("https://nid.naver.com/oauth2.0/token")
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .body(params)
            .retrieve()
            .onStatus(HttpStatusCode::is4xxClientError) { _, res ->
                log.error("4xx error during(get access token) request to Naver. Response status: {}", res.statusCode)
                throw OAuth2Exception(OAuth2Exception.OAuth2ErrorCode.CLIENT, "[Naver getAccessToken 400 Client Error] Failed to get access token")
            }
            .onStatus(HttpStatusCode::is5xxServerError) { _, res ->
                log.error("5xx error during(get access token) request to Naver. Response status: {}", res.statusCode)
                throw OAuth2Exception(OAuth2Exception.OAuth2ErrorCode.PROVIDER, "[Naver getAccessToken 500 Provider Error] Failed to get access token")
            }
            .toEntity(object : ParameterizedTypeReference<Map<String, Any>>() {})

        val body = response.body
        val accessToken = body?.get("access_token") as? String
            ?: throw OAuth2Exception(OAuth2Exception.OAuth2ErrorCode.SERVER, "[Naver getAccessToken 500 Server Error] 응답에 access_token이 없습니다.")

        return accessToken
    }

    override fun getUserInfo(accessToken: String): OAuth2Data {
        val response = restClient.get()
            .uri("https://openapi.naver.com/v1/nid/me")
            .header(HttpHeaders.AUTHORIZATION, HeaderConstants.BEARER_PREFIX + accessToken)
            .retrieve()
            .onStatus(HttpStatusCode::is4xxClientError) { _, res ->
                log.error("4xx error during(get userInfo) request to Naver. Response status: {}", res.statusCode)
                throw OAuth2Exception(OAuth2Exception.OAuth2ErrorCode.CLIENT, "[Naver getUserInfo 400 Client Error] Failed to get userInfo")
            }
            .onStatus(HttpStatusCode::is5xxServerError) { _, res ->
                log.error("5xx error during(get userInfo) request to Naver. Response status: {}", res.statusCode)
                throw OAuth2Exception(OAuth2Exception.OAuth2ErrorCode.PROVIDER, "[Naver getUserInfo 500 Provider Error] Failed to get userInfo")
            }
            .toEntity(object : ParameterizedTypeReference<Map<String, Any>>() {})

        val body = response.body ?: throw OAuth2Exception(OAuth2Exception.OAuth2ErrorCode.SERVER, "[Naver getUserInfo 500 Server Error] 응답이 없습니다.")
        return NaverOAuth2Data(body)
    }
}

class NaverOAuth2Data(attribute: Map<String, Any>) : OAuth2Data {
    private val response: Map<String, Any> = attribute["response"] as Map<String, Any>

    override fun getProvider() = "naver"
    override fun getProviderId() = response["id"]?.toString() ?: ""
    override fun getEmail() = response["email"]?.toString() ?: ""
    override fun getName() = response["name"]?.toString() ?: ""
    override fun getNickName() = response["nickname"]?.toString() ?: ""
}
