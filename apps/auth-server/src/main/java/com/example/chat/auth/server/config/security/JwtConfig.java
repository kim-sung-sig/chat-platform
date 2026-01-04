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
import java.util.UUID;

/**
 * JWT 설정
 * <p>
 * ECDSA (Elliptic Curve Digital Signature Algorithm) 키쌍을 생성하고 JWT 인코더를 제공합니다.
 * RSA 대비 장점:
 * - 키 크기: 256-bit (RSA 2048-bit 대비 8배 작음)
 * - 서명 속도: 5~10배 빠름
 * - 검증 속도: RSA와 유사하거나 더 빠름
 * - 네트워크 전송량 감소
 * <p>
 * 사용 알고리즘: ES256 (ECDSA with P-256 and SHA-256)
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class JwtConfig {

	/**
	 * EC 키쌍 생성
	 * <p>
	 * P-256 (secp256r1) 곡선 사용 - NIST 표준, 광범위하게 지원됨
	 * <p>
	 * 실제 운영 환경에서는 키를 안전하게 저장하고 로드해야 합니다.
	 * 현재는 간단한 구현을 위해 애플리케이션 시작 시 키를 생성합니다.
	 */
	@Bean
	public ECKey ecKey() {
		try {
			// EC 키 생성기 초기화 (P-256 곡선)
			KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("EC");
			ECGenParameterSpec ecSpec = new ECGenParameterSpec("secp256r1"); // P-256
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

		} catch (Exception e) {
			throw new IllegalStateException("Failed to generate EC key", e);
		}
	}

	/**
	 * JWK Set - 공개키 목록
	 * <p>
	 * /.well-known/jwks.json 엔드포인트에서 사용됩니다.
	 */
	@Bean
	public JWKSet jwkSet(ECKey ecKey) {
		return new JWKSet(ecKey);
	}

	@Bean
	public JwsHeader jwsHeader(ECKey ecKey) {
		// 알고리즘과 Key ID를 결합하여 헤더 정책 정의
		return JwsHeader.with(SignatureAlgorithm.ES256)
				.keyId(ecKey.getKeyID())
				.build();
	}

	/**
	 * JWK Source - JWT 인코더가 사용
	 */
	@Bean
	public JWKSource<SecurityContext> jwkSource(JWKSet jwkSet) {
		return new ImmutableJWKSet<>(jwkSet);
	}

	/**
	 * JWT 인코더
	 * <p>
	 * JWT 토큰 생성 시 사용됩니다.
	 * ES256 알고리즘으로 서명됩니다.
	 */
	@Bean
	public JwtEncoder jwtEncoder(JWKSource<SecurityContext> jwkSource) {
		return new NimbusJwtEncoder(jwkSource);
	}
}

