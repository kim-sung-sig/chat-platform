package com.example.chat.auth.server.api;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nimbusds.jose.jwk.JWKSet;

import lombok.RequiredArgsConstructor;

/**
 * JWK Set 컨트롤러
 */
@RestController
@RequiredArgsConstructor
public class JwkSetApi {

    private final JWKSet jwkSet;

    /**
     * JWK Set 엔드포인트
     */
    @GetMapping("/.well-known/jwks.json")
    public Map<String, Object> jwkSet() {
        return jwkSet.toJSONObject();
    }
}
