package com.example.chat.auth.jwt.config;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtIssuerValidator;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.time.Duration;

/**
 * JWT 보안 자동 설정
 *
 * {@link com.example.chat.auth.jwt.annotation.EnableJwtSecurity} 어노테이션으로 활성화됩니다.
 */
@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class JwtSecurityAutoConfiguration {
    private static final Logger log = LoggerFactory.getLogger(JwtSecurityAutoConfiguration.class);

    @Bean
    @ConditionalOnMissingBean
    public JwtDecoder jwtDecoder(JwtProperties properties) {
        log.info("Initializing JwtDecoder with issuerUri: {}, jwkSetUri: {}",
                properties.issuerUri(), properties.jwkSetUri());

        try {
            var restOperations = new RestTemplateBuilder()
                    .connectTimeout(Duration.ofSeconds(5))
                    .readTimeout(Duration.ofSeconds(5))
                    .build();

            var decoder = NimbusJwtDecoder.withJwkSetUri(properties.jwkSetUri())
                    .jwsAlgorithm(SignatureAlgorithm.ES256)
                    .restOperations(restOperations)
                    .build();

            var validators = new DelegatingOAuth2TokenValidator<>(
                    new JwtIssuerValidator(properties.issuerUri()),
                    new JwtTimestampValidator());
            decoder.setJwtValidator(validators);

            log.info("JwtDecoder initialized successfully (Algorithm: ES256)");
            return decoder;
        } catch (Exception e) {
            log.error("Failed to initialize JwtDecoder", e);
            throw new IllegalStateException("Could not create JwtDecoder", e);
        }
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
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> {
                    auth.requestMatchers("/auth/**", "/health", "/actuator/**").permitAll();
                    customizerProvider.ifAvailable(customizer -> customizer.customize(auth));
                    auth.anyRequest().authenticated();
                })
                .oauth2ResourceServer(oauth2 ->
                        oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter))
                                .authenticationEntryPoint(jwtAuthenticationEntryPoint))
                .exceptionHandling(exception ->
                        exception.authenticationEntryPoint(jwtAuthenticationEntryPoint));

        return http.build();
    }
}
