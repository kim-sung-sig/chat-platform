package com.example.chat.auth.server.config.security;

import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

/**
 * JWT 설정
 */
@Configuration
@EnableConfigurationProperties(JwtProperties.class)
@Slf4j
public class JwtConfig {

	@Bean
	public ECKey ecKey(JwtProperties jwtProperties) {
		String jwkFile = jwtProperties.getJwkFile();
		if (jwkFile == null || jwkFile.isBlank()) {
			throw new IllegalStateException("jwt.jwk-file must be configured to persist signing keys");
		}
		Path jwkPath = Path.of(jwkFile);
		if (Files.exists(jwkPath)) {
			return loadEcKey(jwkPath);
		}
		ECKey generated = generateEcKey();
		persistEcKey(jwkPath, generated);
		return generated;
	}

	private ECKey loadEcKey(Path jwkPath) {
		try {
			String json = Files.readString(jwkPath, StandardCharsets.UTF_8);
			JWKSet jwkSet = JWKSet.parse(json);
			List<com.nimbusds.jose.jwk.JWK> keys = jwkSet.getKeys();
			if (keys.isEmpty() || !(keys.get(0) instanceof ECKey)) {
				throw new IllegalStateException("JWK file does not contain an EC key");
			}
			return (ECKey) keys.get(0);
		}
		catch (Exception e) {
			throw new IllegalStateException("Failed to load EC key from JWK file", e);
		}
	}

	private void persistEcKey(Path jwkPath, ECKey ecKey) {
		try {
			Path parent = jwkPath.getParent();
			if (parent != null) {
				Files.createDirectories(parent);
			}
			JWKSet jwkSet = new JWKSet(ecKey);
			Files.writeString(jwkPath, jwkSet.toString(), StandardCharsets.UTF_8);
			log.info("Persisted EC key to JWK file: {}", jwkPath);
		}
		catch (Exception e) {
			throw new IllegalStateException("Failed to persist EC key to JWK file", e);
		}
	}

	private ECKey generateEcKey() {
		try {
			KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("EC");
			ECGenParameterSpec ecSpec = new ECGenParameterSpec("secp256r1");
			keyPairGenerator.initialize(ecSpec);
			KeyPair keyPair = keyPairGenerator.generateKeyPair();

			ECPublicKey publicKey = (ECPublicKey) keyPair.getPublic();
			ECPrivateKey privateKey = (ECPrivateKey) keyPair.getPrivate();

			ECKey ecKey = new ECKey.Builder(Curve.P_256, publicKey)
					.privateKey(privateKey)
					.keyID(UUID.randomUUID().toString())
					.build();

			log.info("EC Key (P-256) generated with ID: {} (Algorithm: ES256)", ecKey.getKeyID());
			return ecKey;
		}
		catch (Exception e) {
			throw new IllegalStateException("Failed to generate EC key", e);
		}
	}

	@Bean
	public JWKSet jwkSet(ECKey ecKey) {
		return new JWKSet(ecKey);
	}

	@Bean
	public JwsHeader jwsHeader(ECKey ecKey) {
		return JwsHeader.with(SignatureAlgorithm.ES256).keyId(ecKey.getKeyID()).build();
	}

	@Bean
	public JWKSource<SecurityContext> jwkSource(JWKSet jwkSet) {
		return new ImmutableJWKSet<>(jwkSet);
	}

	@Bean
	public JwtEncoder jwtEncoder(JWKSource<SecurityContext> jwkSource) {
		return new NimbusJwtEncoder(jwkSource);
	}
}
