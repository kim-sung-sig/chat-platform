package com.example.chat.auth.jwt.config

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.oshai.kotlinlogging.KotlinLogging
import java.time.Duration
import org.springframework.beans.factory.ObjectProvider
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.JwtIssuerValidator
import org.springframework.security.oauth2.jwt.JwtTimestampValidator
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter
import org.springframework.security.web.SecurityFilterChain

private val log = KotlinLogging.logger {}

/**
 * JWT 인증 보안 설정
 *
 * 이 설정은 [com.example.chat.auth.jwt.annotation.EnableJwtSecurity] 어노테이션을 통해서만 활성화됩니다.
 * Auto-Configuration이 아니므로 명시적으로 @EnableJwtSecurity를 사용해야 합니다.
 */
@Configuration
@EnableConfigurationProperties(JwtProperties::class)
class JwtSecurityAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    fun jwtDecoder(properties: JwtProperties): JwtDecoder {
        log.info {
            "Initializing JwtDecoder with issuerUri: ${properties.issuerUri}, jwkSetUri: ${properties.jwkSetUri}"
        }

        return try {
            // RestTemplate 설정 (타임아웃 추가)
            val restOperations =
                    RestTemplateBuilder()
                            .connectTimeout(Duration.ofSeconds(5))
                            .readTimeout(Duration.ofSeconds(5))
                            .build()

            // JWK Set URI로 JwtDecoder 생성
            // ES256 알고리즘(ECDSA with P-256)을 명시적으로 지정
            val decoder =
                    NimbusJwtDecoder.withJwkSetUri(properties.jwkSetUri)
                            .jwsAlgorithm(SignatureAlgorithm.ES256) // ES256 알고리즘 명시
                            .restOperations(restOperations)
                            .build()

            // Issuer 검증을 포함한 Validator 설정
            val issuerValidator = JwtIssuerValidator(properties.issuerUri)
            val timestampValidator = JwtTimestampValidator()

            val validators =
                    DelegatingOAuth2TokenValidator<Jwt>(issuerValidator, timestampValidator)

            decoder.setJwtValidator(validators)

            log.info { "JwtDecoder initialized successfully" }
            log.info { "  - Issuer URI: ${properties.issuerUri}" }
            log.info { "  - JWK Set URI: ${properties.jwkSetUri}" }
            log.info { "  - Algorithm: ES256 (ECDSA with P-256)" }

            decoder
        } catch (e: Exception) {
            log.error(e) { "Failed to initialize JwtDecoder" }
            throw IllegalStateException("Could not create JwtDecoder", e)
        }
    }

    @Bean
    @ConditionalOnMissingBean
    fun jwtAuthenticationConverter(): JwtAuthenticationConverter {
        val converter = JwtAuthenticationConverter()
        converter.setJwtGrantedAuthoritiesConverter(JwtAuthenticationConverterSupport())
        return converter
    }

    @Bean
    @ConditionalOnMissingBean
    fun jwtAuthenticationEntryPoint(objectMapper: ObjectMapper): JwtAuthenticationEntryPoint {
        return JwtAuthenticationEntryPoint(objectMapper)
    }

    /** JWT 인증을 위한 SecurityFilterChain 구성 */
    @Bean
    @Order(1)
    @ConditionalOnMissingBean(name = ["jwtSecurityFilterChain"])
    fun jwtSecurityFilterChain(
            http: HttpSecurity,
            jwtAuthenticationConverter: JwtAuthenticationConverter,
            jwtAuthenticationEntryPoint: JwtAuthenticationEntryPoint,
            customizerProvider: ObjectProvider<SecurityRequestCustomizer>
    ): SecurityFilterChain {
        http
                .csrf { it.disable() }
                .sessionManagement { session ->
                    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                }
                .authorizeHttpRequests { auth ->
                    // 1. 인증 불필요 경로 설정
                    auth.requestMatchers("/auth/**", "/health", "/actuator/**").permitAll()
                    // 2. 서비스별 커스텀 설정 적용 (여기서 더 잠그거나 열 수 있음)
                    customizerProvider.ifAvailable { customizer -> customizer.customize(auth) }
                    // 3. 나머지 요청은 인증 필요
                    auth.anyRequest().authenticated()
                }
                .oauth2ResourceServer { oauth2 ->
                    oauth2
                            .jwt { jwt ->
                                jwt.jwtAuthenticationConverter(jwtAuthenticationConverter)
                            }
                            .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                }
                .exceptionHandling { exception ->
                    exception.authenticationEntryPoint(jwtAuthenticationEntryPoint)
                }

        return http.build()
    }
}
