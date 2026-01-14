package com.example.chat.auth.server.api;

import com.nimbusds.jose.jwk.JWKSet;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * JWK Set 컨트롤러
 * <p>
 * OAuth2 Resource Server가 JWT 검증을 위해 사용하는 공개키를 제공합니다.
 * /.well-known/jwks.json 엔드포인트는 표준 경로입니다.
 */
@RestController
@RequiredArgsConstructor
public class JwkSetApi {

	private final JWKSet jwkSet;

	/**
	 * JWK Set 엔드포인트
	 * <p>
	 * OAuth2 Resource Server는 이 엔드포인트에서 공개키를 가져와 JWT를 검증합니다.
	 *
	 * @return JWK Set (JSON)
	 */
	@GetMapping("/.well-known/jwks.json")
	public Map<String, Object> jwkSet() {
		return jwkSet.toJSONObject();
	}
}

