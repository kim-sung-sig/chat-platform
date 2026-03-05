package com.example.chat.auth.jwt.config;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtIssuerValidator;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * JWT 보안 자동 설정
 *
 * - security.jwt.secret-key 가 있으면 HS256 대칭키 방식으로 검증
 * - security.jwt.jwk-set-uri 가 있으면 JWK Set URI 방식 (RS256/ES256) 으로 검증
 */
@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class JwtSecurityAutoConfiguration {
    private static final Logger log = LoggerFactory.getLogger(JwtSecurityAutoConfiguration.class);

    @Bean
    @ConditionalOnMissingBean
    public JwtDecoder jwtDecoder(JwtProperties properties) {
        // HS256 대칭키 모드 (secret-key 설정된 경우)
        if (properties.isHmacMode()) {
            log.info("Initializing JwtDecoder with HS256 (secret-key mode)");
            // auth-server의 MACSigner(secretKey.getBytes()) 와 동일한 방식으로 바이트 변환
            byte[] keyBytes = properties.secretKey().getBytes(StandardCharsets.UTF_8);
            SecretKeySpec secretKeySpec = new SecretKeySpec(keyBytes, "HmacSHA256");
            NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(secretKeySpec)
                    .macAlgorithm(MacAlgorithm.HS256)
                    .build();
            if (properties.issuerUri() != null && !properties.issuerUri().isBlank()) {
                // auth-server가 iss claim을 설정하는 경우에만 issuer 검증
                decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                        new JwtIssuerValidator(properties.issuerUri()),
                        new JwtTimestampValidator()));
            } else {
                // iss claim 없는 경우 만료 시간만 검증
                decoder.setJwtValidator(new JwtTimestampValidator());
            }
            log.info("JwtDecoder (HS256) initialized successfully");
            return decoder;
        }

        // JWK Set URI 모드 (RS256/ES256 비대칭키)
        if (properties.jwkSetUri() != null && !properties.jwkSetUri().isBlank()) {
            log.info("Initializing JwtDecoder with JWK Set URI: {}", properties.jwkSetUri());
            try {
                var restOperations = new RestTemplateBuilder()
                        .connectTimeout(Duration.ofSeconds(5))
                        .readTimeout(Duration.ofSeconds(5))
                        .build();
                var decoder = NimbusJwtDecoder.withJwkSetUri(properties.jwkSetUri())
                        .jwsAlgorithm(SignatureAlgorithm.ES256)
                        .restOperations(restOperations)
                        .build();
                decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                        new JwtIssuerValidator(properties.issuerUri()),
                        new JwtTimestampValidator()));
                log.info("JwtDecoder (JWK Set URI / ES256) initialized successfully");
                return decoder;
            } catch (Exception e) {
                log.error("Failed to initialize JwtDecoder", e);
                throw new IllegalStateException("Could not create JwtDecoder", e);
            }
        }

        throw new IllegalStateException(
                "JWT 설정 오류: security.jwt.secret-key 또는 security.jwt.jwk-set-uri 를 설정해야 합니다.");
    }

    @Bean
    @ConditionalOnMissingBean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(new JwtAuthenticationConverterSupport());
        return converter;
    }

    @Bean
    @ConditionalOnMissingBean
    public JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint(ObjectMapper objectMapper) {
        return new JwtAuthenticationEntryPoint(objectMapper);
    }

    @Bean
    @Order(1)
    @ConditionalOnMissingBean(name = "jwtSecurityFilterChain")
    public SecurityFilterChain jwtSecurityFilterChain(
            HttpSecurity http,
            JwtAuthenticationConverter jwtAuthenticationConverter,
            JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint,
            ObjectProvider<SecurityRequestCustomizer> customizerProvider) throws Exception {

        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> {
                    auth.requestMatchers("/auth/**", "/health", "/actuator/**").permitAll();
                    customizerProvider.ifAvailable(customizer -> customizer.customize(auth));
                    auth.anyRequest().authenticated();
                })
                .oauth2ResourceServer(
                        oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter))
                                .authenticationEntryPoint(jwtAuthenticationEntryPoint))
                .exceptionHandling(exception -> exception.authenticationEntryPoint(jwtAuthenticationEntryPoint));

        return http.build();
    }
}
