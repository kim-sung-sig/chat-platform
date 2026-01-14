package com.example.chat.auth.server.domain.service;

import com.example.chat.auth.server.config.security.JwtProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

/**
 * JWT 토큰 생성 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JwtTokenProvider {

	private final JwtEncoder jwtEncoder;
	private final JwtProperties jwtProperties;
	private final JwsHeader jwsHeader;

	/**
	 * Access Token 생성
	 *
	 * @param userId 사용자 ID
	 * @param role   사용자 권한
	 * @return JWT Access Token
	 */
	public String createAccessToken(String userId, String role) {
		Instant now = Instant.now();
		Instant expiresAt = now.plusSeconds(jwtProperties.accessTokenExpiration());

		JwtClaimsSet claims = JwtClaimsSet.builder()
				.issuer(jwtProperties.issuer())
				.subject(userId)
				.issuedAt(now)
				.expiresAt(expiresAt)
				.claim("scope", role)
				.claim("roles", List.of(role))
				.claim("type", "access")
				.build();

		String token = jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, claims)).getTokenValue();
		log.debug("Access token created for user: {}", userId);
		return token;
	}

	/**
	 * Refresh Token 생성
	 *
	 * @param userId 사용자 ID
	 * @return JWT Refresh Token
	 */
	public String createRefreshToken(String userId) {
		Instant now = Instant.now();
		Instant expiresAt = now.plusSeconds(jwtProperties.refreshTokenExpiration());

		JwtClaimsSet claims = JwtClaimsSet.builder()
				.issuer(jwtProperties.issuer())
				.subject(userId)
				.issuedAt(now)
				.expiresAt(expiresAt)
				.claim("type", "refresh")
				.build();

		String token = jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, claims)).getTokenValue();
		log.debug("Refresh token created for user: {}", userId);
		return token;
	}
}

