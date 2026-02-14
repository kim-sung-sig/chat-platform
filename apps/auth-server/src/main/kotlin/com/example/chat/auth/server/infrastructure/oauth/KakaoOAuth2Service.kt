
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
import java.nio.charset.StandardCharsets

@Component
@ConditionalOnProperty(prefix = "oauth.kakao", name = ["client-id"])
class KakaoOAuth2Service(
    @Value("\${oauth.kakao.client-id}") private val kakaoClientId: String,
    @Value("\${oauth.kakao.client-secret}") private val kakaoClientSecret: String
) : SocialOAuth2Service {

    private val log = LoggerFactory.getLogger(KakaoOAuth2Service::class.java)
    private val restClient = RestClient.create()

    @PostConstruct
    fun init() {
        log.debug("kakaoClientId: {}", kakaoClientId)
        log.debug("kakaoClientSecret: {}", kakaoClientSecret)
    }

    override val socialType: SocialType
        get() = SocialType.KAKAO

    override fun getUserInfo(oauthRequest: OAuthRequest): OAuth2Data {
        val accessToken = getAccessToken(oauthRequest)
        return getUserInfo(accessToken)
    }

    override fun getAccessToken(oauthRequest: OAuthRequest): String {
        val params = LinkedMultiValueMap<String, String>().apply {
            add("grant_type", "authorization_code")
            add("client_id", kakaoClientId)
            add("client_secret", kakaoClientSecret)
            add("code", oauthRequest.code)
            oauthRequest.state?.let { add("state", it) }
        }

        val response = restClient.post()
            .uri("https://kauth.kakao.com/oauth/token")
            .header(HttpHeaders.CONTENT_TYPE, MediaType(MediaType.APPLICATION_FORM_URLENCODED, StandardCharsets.UTF_8).toString())
            .body(params)
            .retrieve()
            .onStatus(HttpStatusCode::is4xxClientError) { _, res ->
                log.error("4xx error during(get access token) request to Kakao. Response status: {}", res.statusCode)
                throw OAuth2Exception(OAuth2Exception.OAuth2ErrorCode.CLIENT, "[Kakao getAccessToken 400 Client Error] Failed to get access token")
            }
            .onStatus(HttpStatusCode::is5xxServerError) { _, res ->
                log.error("5xx error during(get access token) request to Kakao. Response status: {}", res.statusCode)
                throw OAuth2Exception(OAuth2Exception.OAuth2ErrorCode.PROVIDER, "[Kakao getAccessToken 500 Provider Error] Failed to get access token")
            }
            .toEntity(object : ParameterizedTypeReference<Map<String, Any>>() {})

        val body = response.body
        val accessToken = body?.get("access_token") as? String
            ?: throw OAuth2Exception(OAuth2Exception.OAuth2ErrorCode.SERVER, "[Kakao getAccessToken 500 Server Error] 응답에 access_token이 없습니다.")

        return accessToken
    }

    override fun getUserInfo(accessToken: String): OAuth2Data {
        val response = restClient.get()
            .uri("https://kapi.kakao.com/v2/user/me")
            .header(HttpHeaders.AUTHORIZATION, HeaderConstants.BEARER_PREFIX + accessToken)
            .header(HttpHeaders.CONTENT_TYPE, MediaType(MediaType.APPLICATION_FORM_URLENCODED, StandardCharsets.UTF_8).toString())
            .retrieve()
            .onStatus(HttpStatusCode::is4xxClientError) { _, res ->
                log.error("4xx error during(get userInfo) request to Kakao. Response status: {}", res.statusCode)
                throw OAuth2Exception(OAuth2Exception.OAuth2ErrorCode.CLIENT, "[Kakao getUserInfo 400 Client Error] Failed to get userInfo")
            }
            .onStatus(HttpStatusCode::is5xxServerError) { _, res ->
                log.error("5xx error during(get userInfo) request to Kakao. Response status: {}", res.statusCode)
                throw OAuth2Exception(OAuth2Exception.OAuth2ErrorCode.PROVIDER, "[Kakao getUserInfo 500 Provider Error] Failed to get userInfo")
            }
            .toEntity(object : ParameterizedTypeReference<Map<String, Any>>() {})

        val body = response.body ?: throw OAuth2Exception(OAuth2Exception.OAuth2ErrorCode.SERVER, "[Kakao getUserInfo 500 Server Error] 응답이 없습니다.")
        return KakaoOAuth2Data(body)
    }
}

class KakaoOAuth2Data(private val attribute: Map<String, Any>) : OAuth2Data {
    override fun getProvider() = "kakao"
    override fun getProviderId() = attribute["id"]?.toString() ?: ""
    override fun getEmail() = ""
    override fun getName(): String {
        val properties = attribute["properties"] as? Map<*, *>
        return properties?.get("nickname")?.toString() ?: ""
    }
    override fun getNickName(): String {
        val properties = attribute["properties"] as? Map<*, *>
        return properties?.get("nickname")?.toString() ?: ""
    }
}
