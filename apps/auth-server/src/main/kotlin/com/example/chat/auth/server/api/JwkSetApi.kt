
package com.example.chat.auth.server.api

import com.nimbusds.jose.jwk.JWKSet
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

/**
 * JWK Set 컨트롤러
 */
@RestController
class JwkSetApi(private val jwkSet: JWKSet) {

    /**
     * JWK Set 엔드포인트
     */
    @GetMapping("/.well-known/jwks.json")
    fun jwkSet(): Map<String, Any> {
        return jwkSet.toJSONObject()
    }
}
