package com.example.chat.auth.server.infrastructure.oauth;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import com.example.chat.auth.server.core.service.oauth.OAuth2Data;
import com.example.chat.auth.server.core.service.oauth.OAuth2Exception;
import com.example.chat.auth.server.core.service.oauth.OAuth2Exception.OAuth2ErrorCode;
import com.example.chat.auth.server.core.service.oauth.OAuthRequest;
import com.example.chat.auth.server.core.service.oauth.SocialOAuth2Service;
import com.example.chat.auth.server.core.service.oauth.SocialType;
import com.example.chat.common.core.constants.HeaderConstants;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "oauth.naver", name = "client-id")
public class NaverOAuth2Service implements SocialOAuth2Service {

    private final RestClient restClient = RestClient.create();

    @Value("${oauth.naver.client-id}")
    private String naverClientId;

    @Value("${oauth.naver.client-secret}")
    private String naverClientSecret;

    @PostConstruct
    public void init() {
        log.debug("naverClientId: {}", naverClientId);
        log.debug("naverClientSecret: {}", naverClientSecret);
    }

    @Override
    public SocialType getSocialType() {
        return SocialType.NAVER;
    }

    @Override
    public OAuth2Data getUserInfo(OAuthRequest oauthRequest) throws OAuth2Exception {
        String accessToken = this.getAccessToken(oauthRequest);
        return this.getUserInfo(accessToken);
    }

    @Override
    public String getAccessToken(OAuthRequest oauthRequestWrapper) throws OAuth2Exception {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", naverClientId);
        params.add("client_secret", naverClientSecret);
        params.add("code", oauthRequestWrapper.code());
        params.add("state", oauthRequestWrapper.state());

        ResponseEntity<Map<String, Object>> response = restClient.post()
                .uri("https://nid.naver.com/oauth2.0/token")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .body(params)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
                    log.error(
                            "4xx error during(get access token) request to Naver. Request: {}, Response status: {}, Response: {}",
                            req, res.getStatusCode(), res);
                    throw new OAuth2Exception(OAuth2ErrorCode.CLIENT,
                            "[Naver getAccessToken 400 Client Error] Failed to get access token");
                })
                .onStatus(HttpStatusCode::is5xxServerError, (req, res) -> {
                    log.error(
                            "5xx error during(get access token) request to Naver. Request: {}, Response status: {}, Response: {}",
                            req, res.getStatusCode(), res);
                    throw new OAuth2Exception(OAuth2ErrorCode.PROVIDER,
                            "[Naver getAccessToken 500 Provider Error] Failed to get access token");
                })
                .toEntity(new ParameterizedTypeReference<Map<String, Object>>() {
                });

        Map<String, Object> body = response.getBody();
        if (body == null || !body.containsKey("access_token")) {
            log.error("[Naver getAccessToken 500 Server Error] Response body is null or missing access_token. Body: {}",
                    body);
            throw new OAuth2Exception(OAuth2ErrorCode.SERVER,
                    "[Naver getAccessToken 500 Server Error] 응답에 access_token이 없습니다.");
        }

        Object token = body.get("access_token");
        if (!(token instanceof String)) {
            log.error("[Naver getAccessToken 500 Server Error] access_token 형식이 잘못됨. Value: {}", token);
            throw new OAuth2Exception(OAuth2ErrorCode.SERVER,
                    "[Naver getAccessToken 500 Server Error] access_token 형식 오류");
        }

        return (String) token;
    }

    @Override
    public OAuth2Data getUserInfo(String accessToken) throws OAuth2Exception {
        ResponseEntity<Map<String, Object>> response = restClient.get()
                .uri("https://openapi.naver.com/v1/nid/me")
                .header(HttpHeaders.AUTHORIZATION, HeaderConstants.BEARER_PREFIX + accessToken)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
                    log.error(
                            "4xx error during(get userInfo) request to Naver. Request: {}, Response status: {}, Response: {}",
                            req, res.getStatusCode(), res);
                    throw new OAuth2Exception(OAuth2ErrorCode.CLIENT,
                            "[Naver getUserInfo 400 Client Error] Failed to get userInfo");
                })
                .onStatus(HttpStatusCode::is5xxServerError, (req, res) -> {
                    log.error(
                            "5xx error during(get userInfo) request to Naver. Request: {}, Response status: {}, Response: {}",
                            req, res.getStatusCode(), res);
                    throw new OAuth2Exception(OAuth2ErrorCode.PROVIDER,
                            "[Naver getUserInfo 500 Provider Error] Failed to get userInfo");
                })
                .toEntity(new ParameterizedTypeReference<Map<String, Object>>() {
                });

        Map<String, Object> body = response.getBody();
        if (body == null) {
            log.error("[Naver getUserInfo 500 Server Error] Response body is null. Body: {}", body);
            throw new OAuth2Exception(OAuth2ErrorCode.SERVER, "[Naver getUserInfo 500 Server Error] 응답이 없습니다.");
        }
        return new NaverOAuth2Data(body);
    }

}

@ToString
class NaverOAuth2Data implements OAuth2Data {

    private final Map<String, Object> attribute;

    @SuppressWarnings("unchecked")
    public NaverOAuth2Data(Map<String, Object> attribute) {
        this.attribute = (Map<String, Object>) attribute.get("response");
    }

    @Override
    public String getProvider() {
        return "naver";
    }

    @Override
    public String getProviderId() {
        return attribute.getOrDefault("id", "").toString();
    }

    @Override
    public String getEmail() {
        return attribute.getOrDefault("email", "").toString();
    }

    @Override
    public String getName() {
        return attribute.getOrDefault("name", "").toString();
    }

    @Override
    public String getNickName() {
        return attribute.getOrDefault("nickname", "").toString();
    }

}
