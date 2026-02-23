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

import java.util.Map;

/**
 * 네이버 OAuth2 서비스
 */
@Component
@ConditionalOnProperty(prefix = "oauth.naver", name = "client-id")
public class NaverOAuth2Service implements SocialOAuth2Service {
    private static final Logger log = LoggerFactory.getLogger(NaverOAuth2Service.class);
    private static final String TOKEN_URI = "https://nid.naver.com/oauth2.0/token";
    private static final String USER_INFO_URI = "https://openapi.naver.com/v1/nid/me";

    private final String naverClientId;
    private final String naverClientSecret;
    private final RestClient restClient;

    public NaverOAuth2Service(
            @Value("${oauth.naver.client-id}") String naverClientId,
            @Value("${oauth.naver.client-secret}") String naverClientSecret) {
        this.naverClientId = naverClientId;
        this.naverClientSecret = naverClientSecret;
        this.restClient = RestClient.create();
    }

    @PostConstruct
    public void init() {
        log.debug("NaverOAuth2Service initialized - clientId: {}", naverClientId);
    }

    @Override
    public SocialType getSocialType() {
        return SocialType.NAVER;
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
        params.add("client_id", naverClientId);
        params.add("client_secret", naverClientSecret);
        params.add("code", oauthRequest.code());
        if (oauthRequest.state() != null) {
            params.add("state", oauthRequest.state());
        }

        var response = restClient.post()
                .uri(TOKEN_URI)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .body(params)
                .retrieve()
                .onStatus(status -> status.is4xxClientError(), (req, res) -> {
                    log.error("4xx error on Naver getAccessToken. status: {}", res.getStatusCode());
                    throw new OAuth2Exception(OAuth2Exception.OAuth2ErrorCode.CLIENT, "[Naver] 400 Client Error");
                })
                .onStatus(status -> status.is5xxServerError(), (req, res) -> {
                    log.error("5xx error on Naver getAccessToken. status: {}", res.getStatusCode());
                    throw new OAuth2Exception(OAuth2Exception.OAuth2ErrorCode.PROVIDER, "[Naver] 500 Provider Error");
                })
                .toEntity(new ParameterizedTypeReference<Map<String, Object>>() {});

        Map<String, Object> body = response.getBody();
        if (body == null || !(body.get("access_token") instanceof String token)) {
            throw new OAuth2Exception(OAuth2Exception.OAuth2ErrorCode.SERVER, "[Naver] access_token not found");
        }
        return token;
    }

    @Override
    public OAuth2Data getUserInfo(String accessToken) {
        var response = restClient.get()
                .uri(USER_INFO_URI)
                .header(HttpHeaders.AUTHORIZATION, HeaderConstants.BEARER_PREFIX + accessToken)
                .retrieve()
                .onStatus(status -> status.is4xxClientError(), (req, res) -> {
                    log.error("4xx error on Naver getUserInfo. status: {}", res.getStatusCode());
                    throw new OAuth2Exception(OAuth2Exception.OAuth2ErrorCode.CLIENT, "[Naver] 400 Client Error");
                })
                .onStatus(status -> status.is5xxServerError(), (req, res) -> {
                    log.error("5xx error on Naver getUserInfo. status: {}", res.getStatusCode());
                    throw new OAuth2Exception(OAuth2Exception.OAuth2ErrorCode.PROVIDER, "[Naver] 500 Provider Error");
                })
                .toEntity(new ParameterizedTypeReference<Map<String, Object>>() {});

        Map<String, Object> body = response.getBody();
        if (body == null) {
            throw new OAuth2Exception(OAuth2Exception.OAuth2ErrorCode.SERVER, "[Naver] response body is null");
        }
        return new NaverOAuth2Data(body);
    }

    /**
     * 네이버 사용자 정보 DTO
     */
    private record NaverOAuth2Data(Map<String, Object> attribute) implements OAuth2Data {

        @SuppressWarnings("unchecked")
        private Map<String, Object> response() {
            return (Map<String, Object>) attribute.get("response");
        }

        @Override
        public String getProvider() {
            return "naver";
        }

        @Override
        public String getProviderId() {
            Object id = response().get("id");
            return id != null ? id.toString() : "";
        }

        @Override
        public String getEmail() {
            Object email = response().get("email");
            return email != null ? email.toString() : "";
        }

        @Override
        public String getName() {
            Object name = response().get("name");
            return name != null ? name.toString() : "";
        }

        @Override
        public String getNickName() {
            Object nickname = response().get("nickname");
            return nickname != null ? nickname.toString() : "";
        }
    }
}
