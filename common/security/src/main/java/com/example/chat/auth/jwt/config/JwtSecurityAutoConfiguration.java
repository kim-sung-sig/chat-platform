package com.example.chat.auth.jwt.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

/**
 * JWT 인증 보안 설정
 * <p>
 * 이 설정은 {@link com.example.chat.auth.jwt.annotation.EnableJwtSecurity} 어노테이션을 통해서만 활성화됩니다.
 * Auto-Configuration이 아니므로 명시적으로 @EnableJwtSecurity를 사용해야 합니다.
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class JwtSecurityAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean
	public JwtDecoder jwtDecoder(JwtProperties properties) {
		log.info("Initializing JwtDecoder with issuerUri: {}", properties.issuerUri());
		return NimbusJwtDecoder.withIssuerLocation(properties.issuerUri()).build();
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
	public JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint() {
		return new JwtAuthenticationEntryPoint();
	}

	/**
	 * JWT 인증을 위한 SecurityFilterChain 구성
	 * <p>
	 * 주의: IDE에서 HttpSecurity 빈을 찾을 수 없다는 경고가 표시될 수 있지만,
	 * 실제 Spring Boot 애플리케이션에서는 Spring Security Auto-configuration이
	 * HttpSecurity를 자동으로 제공하므로 정상 작동합니다.
	 *
	 * @param http                        Spring Security HttpSecurity (자동 주입)
	 * @param jwtAuthenticationConverter  JWT 권한 변환기
	 * @param jwtAuthenticationEntryPoint JWT 인증 실패 핸들러
	 * @return SecurityFilterChain
	 * @throws Exception 설정 오류 시
	 */
	@Bean
	@Order(1)
	@ConditionalOnMissingBean(name = "jwtSecurityFilterChain")
	public SecurityFilterChain jwtSecurityFilterChain(
			HttpSecurity http,
			JwtAuthenticationConverter jwtAuthenticationConverter,
			JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint,
			ObjectProvider<SecurityRequestCustomizer> customizerProvider
	) throws Exception {

		http
				.csrf(AbstractHttpConfigurer::disable)
				.sessionManagement(session -> session
						.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
				)
				.authorizeHttpRequests(auth -> {
						// 1. 인증 불필요 경로 설정
						auth.requestMatchers("/auth/**", "/health", "/actuator/**").permitAll();
						// 2. 서비스별 커스텀 설정 적용 (여기서 더 잠그거나 열 수 있음)
						customizerProvider.ifAvailable(customizer -> customizer.customize(auth));
						// 3. 나머지 요청은 인증 필요
						auth.anyRequest().authenticated();
				})
				.oauth2ResourceServer(oauth2 -> oauth2
						.jwt(jwt -> jwt
								.jwtAuthenticationConverter(jwtAuthenticationConverter)
						)
						.authenticationEntryPoint(jwtAuthenticationEntryPoint)
				)
				.exceptionHandling(exception -> exception
						.authenticationEntryPoint(jwtAuthenticationEntryPoint)
				);

		return http.build();
	}
}
