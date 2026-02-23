package com.example.chat.auth.jwt.config;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;

/**
 * 보안 요청 커스터마이저 인터페이스
 *
 * 각 서버에서 permitAll 경로 등 추가 보안 정책을 정의할 때 구현합니다.
 */
@FunctionalInterface
public interface SecurityRequestCustomizer {
    void customize(
            AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry auth);
}
