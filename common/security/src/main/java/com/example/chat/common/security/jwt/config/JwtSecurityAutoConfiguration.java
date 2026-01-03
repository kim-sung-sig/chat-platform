package com.example.chat.common.security.jwt.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
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

import static org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type.SERVLET;

/**
 * JWT ?∏Ï¶ù Î≥¥Ïïà ?§Ï†ï
 * <p>
 * ???§Ï†ï?Ä {@link com.example.chat.auth.jwt.annotation.EnableJwtSecurity} ?¥ÎÖ∏?åÏù¥?òÏùÑ ?µÌï¥?úÎßå ?úÏÑ±?îÎê©?àÎã§.
 * Auto-Configuration???ÑÎãàÎØÄÎ°?Î™ÖÏãú?ÅÏúºÎ°?@EnableJwtSecurityÎ•??¨Ïö©?¥Ïïº ?©Îãà??
 */
@ConditionalOnClass(HttpSecurity.class)
@ConditionalOnWebApplication(type = SERVLET)
@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class JwtSecurityAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean
	public JwtDecoder jwtDecoder(JwtProperties properties) {
		return NimbusJwtDecoder.withIssuerLocation(properties.getIssuerUri()).build();
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
	 * JWT ?∏Ï¶ù???ÑÌïú SecurityFilterChain Íµ¨ÏÑ±
	 * <p>
	 * Ï£ºÏùò: IDE?êÏÑú HttpSecurity ÎπàÏùÑ Ï∞æÏùÑ ???ÜÎã§??Í≤ΩÍ≥†Í∞Ä ?úÏãú?????àÏ?Îß?
	 * ?§Ï†ú Spring Boot ?†ÌîåÎ¶¨Ï??¥ÏÖò?êÏÑú??Spring Security Auto-configuration??
	 * HttpSecurityÎ•??êÎèô?ºÎ°ú ?úÍ≥µ?òÎ?Î°??ïÏÉÅ ?ëÎèô?©Îãà??
	 *
	 * @param http                        Spring Security HttpSecurity (?êÎèô Ï£ºÏûÖ)
	 * @param jwtAuthenticationConverter  JWT Í∂åÌïú Î≥Ä?òÍ∏∞
	 * @param jwtAuthenticationEntryPoint JWT ?∏Ï¶ù ?§Ìå® ?∏Îì§??
	 * @return SecurityFilterChain
	 * @throws Exception ?§Ï†ï ?§Î•ò ??
	 */
	@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
	@Bean
	@Order(1)
	@ConditionalOnMissingBean(name = "jwtSecurityFilterChain")
	public SecurityFilterChain jwtSecurityFilterChain(
			HttpSecurity http,
			JwtAuthenticationConverter jwtAuthenticationConverter,
			JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint
	) throws Exception {

		http
				.csrf(AbstractHttpConfigurer::disable)
				.sessionManagement(session -> session
						.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
				)
				.authorizeHttpRequests(auth -> auth
						.requestMatchers("/auth/**", "/health", "/actuator/**").permitAll()
						.anyRequest().authenticated()
				)
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
