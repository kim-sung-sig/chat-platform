package com.example.chat.auth.server.infrastructure.oauth;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

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
@ConditionalOnProperty(prefix = "oauth.kakao", name = "client-id")
public class KakaoOAuth2Service implements SocialOAuth2Service {

    private final RestClient restClient = RestClient.create();

    @Value("${oauth.kakao.client-id}")
    private String kakaoClientId;

    @Value("${oauth.kakao.client-secret}")
    private String kakaoClientSecret;

    @PostConstruct
    public void init() {
        log.debug("kakaoClientId: {}", kakaoClientId);
        log.debug("kakaoClientSecret: {}", kakaoClientSecret);
    }

    @Override
    public SocialType getSocialType() {
        return SocialType.KAKAO;
    }

    @Override
    public OAuth2Data getUserInfo(OAuthRequest oauthRequest) throws OAuth2Exception {
        String accessToken = this.getAccessToken(oauthRequest);
        return this.getUserInfo(accessToken);
    }

    @Override
    public String getAccessToken(OAuthRequest oauthRequest) throws OAuth2Exception {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", kakaoClientId);
        params.add("client_secret", kakaoClientSecret);
        params.add("code", oauthRequest.code());
        params.add("state", oauthRequest.state());

        ResponseEntity<Map<String, Object>> response = restClient.post()
                .uri("https://kauth.kakao.com/oauth/token")
                .header(HttpHeaders.CONTENT_TYPE,
                        new MediaType(
                                Objects.requireNonNull(MediaType.APPLICATION_FORM_URLENCODED),
                                Objects.requireNonNull(StandardCharsets.UTF_8)).toString())
                .body(params)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
                    log.error(
                            "4xx error during(get access token) request to Kakao. Request: {}, Response status: {}, Response: {}",
                            req, res.getStatusCode(), res);
                    throw new OAuth2Exception(OAuth2ErrorCode.CLIENT,
                            "[Kakao getAccessToken 400 Client Error] Failed to get access token");
                })
                .onStatus(HttpStatusCode::is5xxServerError, (req, res) -> {
                    log.error("5xx error during(get access token) request to Kakao. Request: {}, Response status: {}",
                            req, res.getStatusCode());
                    throw new OAuth2Exception(OAuth2ErrorCode.PROVIDER,
                            "[Kakao getAccessToken 500 Provider Error] Failed to get access token");
                })
                .toEntity(new ParameterizedTypeReference<Map<String, Object>>() {
                });

        Map<String, Object> body = response.getBody();
        if (body == null || !body.containsKey("access_token")) {
            log.error("[Kakao getAccessToken 500 Server Error] Response body is null or missing access_token. Body: {}",
                    body);
            throw new OAuth2Exception(OAuth2ErrorCode.SERVER,
                    "[Kakao getAccessToken 500 Server Error] 응답에 access_token이 없습니다.");
        }

        Object token = body.get("access_token");
        if (!(token instanceof String)) {
            log.error("[Kakao getAccessToken 500 Server Error] access_token 형식이 잘못됨. Value: {}", token);
            throw new OAuth2Exception(OAuth2ErrorCode.SERVER,
                    "[Kakao getAccessToken 500 Server Error] access_token 형식 오류");
        }

        return (String) token;
    }

    @Override
    public OAuth2Data getUserInfo(String accessToken) throws OAuth2Exception {
        ResponseEntity<Map<String, Object>> response = restClient.get()
                .uri("https://kapi.kakao.com/v2/user/me")
                .header(HttpHeaders.AUTHORIZATION, HeaderConstants.BEARER_PREFIX + accessToken)
                .header(HttpHeaders.CONTENT_TYPE,
                        new MediaType(
                                Objects.requireNonNull(MediaType.APPLICATION_FORM_URLENCODED),
                                Objects.requireNonNull(StandardCharsets.UTF_8)).toString())
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
                    log.error(
                            "4xx error during(get userInfo) request to Kakao. Request: {}, Response status: {}, Response: {}",
                            req, res.getStatusCode(), res);
                    throw new OAuth2Exception(OAuth2ErrorCode.CLIENT,
                            "[Kakao getUserInfo 400 Client Error] Failed to get userInfo");
                })
                .onStatus(HttpStatusCode::is5xxServerError, (req, res) -> {
                    log.error(
                            "5xx error during(get userInfo) request to Kakao. Request: {}, Response status: {}, Response: {}",
                            req, res.getStatusCode(), res);
                    throw new OAuth2Exception(OAuth2ErrorCode.PROVIDER,
                            "[Kakao getUserInfo 500 Provider Error] Failed to get userInfo");
                })
                .toEntity(new ParameterizedTypeReference<Map<String, Object>>() {
                });

        Map<String, Object> body = response.getBody();
        if (body == null) {
            log.error("[Kakao getUserInfo 500 Server Error] Response body is null. Body: {}", body);
            throw new OAuth2Exception(OAuth2ErrorCode.SERVER, "[Kakao getUserInfo 500 Server Error] 응답이 없습니다.");
        }

        return new KakaoOAuth2Data(body);
    }

}

@ToString
class KakaoOAuth2Data implements OAuth2Data {

    private final Map<String, Object> attribute;

    public KakaoOAuth2Data(Map<String, Object> attribute) {
        this.attribute = attribute;
    }

    @Override
    public String getProvider() {
        return "kakao";
    }

    @Override
    public String getProviderId() {
        return String.valueOf(attribute.getOrDefault("id", ""));
    }

    @Override
    public String getEmail() {
        return "";
    }

    @Override
    public String getName() {
        @SuppressWarnings("unchecked")
        Map<String, Object> properties = (Map<String, Object>) attribute.getOrDefault("properties",
                Collections.emptyMap());
        return String.valueOf(properties.getOrDefault("nickname", ""));
    }

    @Override
    public String getNickName() {
        @SuppressWarnings("unchecked")
        Map<String, Object> properties = (Map<String, Object>) attribute.getOrDefault("properties",
                Collections.emptyMap());
        return String.valueOf(properties.getOrDefault("nickname", ""));
    }

}
