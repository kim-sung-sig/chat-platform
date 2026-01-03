package com.example.chat.auth.jwt.config;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@Getter
@ConfigurationProperties(prefix = "security.jwt")
public class JwtProperties {

	private final String issuerUri;
	private final String jwkSetUri;

	public JwtProperties(
			@DefaultValue("http://localhost:8080") String issuerUri,
			@DefaultValue("http://localhost:8080/.well-known/jwks.json") String jwkSetUri) {
		this.issuerUri = issuerUri;
		this.jwkSetUri = jwkSetUri;
	}
}