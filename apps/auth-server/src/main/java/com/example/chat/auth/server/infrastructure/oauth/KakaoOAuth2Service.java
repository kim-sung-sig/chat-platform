package com.example.chat.auth.server.infrastructure.oauth;

import com.example.chat.auth.server.core.service.oauth.OAuth2Data;
import com.example.chat.auth.server.core.service.oauth.OAuth2Exception;
import com.example.chat.auth.server.core.service.oauth.OAuthRequest;
import com.example.chat.auth.server.core.service.oauth.SocialOAuth2Service;
import com.example.chat.auth.server.core.service.oauth.SocialType;
import com.example.chat.common.core.constants.HeaderConstants;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * 카카오 OAuth2 서비스
 */
@Component
@ConditionalOnProperty(prefix = "oauth.kakao", name = "client-id")
public class KakaoOAuth2Service implements SocialOAuth2Service {
    private static final Logger log = LoggerFactory.getLogger(KakaoOAuth2Service.class);
    private static final String TOKEN_URI = "https://kauth.kakao.com/oauth/token";
    private static final String USER_INFO_URI = "https://kapi.kakao.com/v2/user/me";

    private final String kakaoClientId;
    private final String kakaoClientSecret;
    private final RestClient restClient;

    public KakaoOAuth2Service(
            @Value("${oauth.kakao.client-id}") String kakaoClientId,
            @Value("${oauth.kakao.client-secret}") String kakaoClientSecret) {
        this.kakaoClientId = kakaoClientId;
        this.kakaoClientSecret = kakaoClientSecret;
        this.restClient = RestClient.create();
    }

    @PostConstruct
    public void init() {
        log.debug("KakaoOAuth2Service initialized - clientId: {}", kakaoClientId);
    }

    @Override
    public SocialType getSocialType() {
        return SocialType.KAKAO;
    }

    @Override
    public OAuth2Data getUserInfo(OAuthRequest oauthRequest) {
        String accessToken = getAccessToken(oauthRequest);
        return getUserInfo(accessToken);
    }

    @Override
    public String getAccessToken(OAuthRequest oauthRequest) {
        LinkedMultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", kakaoClientId);
        params.add("client_secret", kakaoClientSecret);
        params.add("code", oauthRequest.code());
        if (oauthRequest.state() != null) {
            params.add("state", oauthRequest.state());
        }

        var response = restClient.post()
                .uri(TOKEN_URI)
                .header(HttpHeaders.CONTENT_TYPE, new MediaType(MediaType.APPLICATION_FORM_URLENCODED, StandardCharsets.UTF_8).toString())
                .body(params)
                .retrieve()
                .onStatus(status -> status.is4xxClientError(), (req, res) -> {
                    log.error("4xx error on Kakao getAccessToken. status: {}", res.getStatusCode());
                    throw new OAuth2Exception(OAuth2Exception.OAuth2ErrorCode.CLIENT, "[Kakao] 400 Client Error");
                })
                .onStatus(status -> status.is5xxServerError(), (req, res) -> {
                    log.error("5xx error on Kakao getAccessToken. status: {}", res.getStatusCode());
                    throw new OAuth2Exception(OAuth2Exception.OAuth2ErrorCode.PROVIDER, "[Kakao] 500 Provider Error");
                })
                .toEntity(new ParameterizedTypeReference<Map<String, Object>>() {});

        Map<String, Object> body = response.getBody();
        if (body == null || !(body.get("access_token") instanceof String token)) {
            throw new OAuth2Exception(OAuth2Exception.OAuth2ErrorCode.SERVER, "[Kakao] access_token not found");
        }
        return token;
    }

    @Override
    public OAuth2Data getUserInfo(String accessToken) {
        var response = restClient.get()
                .uri(USER_INFO_URI)
                .header(HttpHeaders.AUTHORIZATION, HeaderConstants.BEARER_PREFIX + accessToken)
                .header(HttpHeaders.CONTENT_TYPE, new MediaType(MediaType.APPLICATION_FORM_URLENCODED, StandardCharsets.UTF_8).toString())
                .retrieve()
                .onStatus(status -> status.is4xxClientError(), (req, res) -> {
                    log.error("4xx error on Kakao getUserInfo. status: {}", res.getStatusCode());
                    throw new OAuth2Exception(OAuth2Exception.OAuth2ErrorCode.CLIENT, "[Kakao] 400 Client Error");
                })
                .onStatus(status -> status.is5xxServerError(), (req, res) -> {
                    log.error("5xx error on Kakao getUserInfo. status: {}", res.getStatusCode());
                    throw new OAuth2Exception(OAuth2Exception.OAuth2ErrorCode.PROVIDER, "[Kakao] 500 Provider Error");
                })
                .toEntity(new ParameterizedTypeReference<Map<String, Object>>() {});

        Map<String, Object> body = response.getBody();
        if (body == null) {
            throw new OAuth2Exception(OAuth2Exception.OAuth2ErrorCode.SERVER, "[Kakao] response body is null");
        }
        return new KakaoOAuth2Data(body);
    }

    /**
     * 카카오 사용자 정보 DTO
     */
    private record KakaoOAuth2Data(Map<String, Object> attribute) implements OAuth2Data {

        @Override
        public String getProvider() {
            return "kakao";
        }

        @Override
        public String getProviderId() {
            Object id = attribute.get("id");
            return id != null ? id.toString() : "";
        }

        @Override
        public String getEmail() {
            return "";
        }

        @Override
        public String getName() {
            return getNickName();
        }

        @Override
        public String getNickName() {
            Object properties = attribute.get("properties");
            if (properties instanceof Map<?, ?> props) {
                Object nickname = props.get("nickname");
                return nickname != null ? nickname.toString() : "";
            }
            return "";
        }
    }
}
