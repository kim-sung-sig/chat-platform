package com.example.chat.auth.server.config.security

import com.nimbusds.jose.jwk.Curve
import com.nimbusds.jose.jwk.ECKey
import com.nimbusds.jose.jwk.JWKSet
import com.nimbusds.jose.jwk.source.ImmutableJWKSet
import com.nimbusds.jose.jwk.source.JWKSource
import com.nimbusds.jose.proc.SecurityContext
import java.security.KeyPairGenerator
import java.security.interfaces.ECPrivateKey
import java.security.interfaces.ECPublicKey
import java.security.spec.ECGenParameterSpec
import java.util.*
import org.slf4j.LoggerFactory
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm
import org.springframework.security.oauth2.jwt.JwsHeader
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder

/** JWT 설정 */
@Configuration
@EnableConfigurationProperties(JwtProperties::class)
class JwtConfig {
    private val log = LoggerFactory.getLogger(JwtConfig::class.java)

    @Bean
    fun ecKey(): ECKey {
        try {
            val keyPairGenerator = KeyPairGenerator.getInstance("EC")
            val ecSpec = ECGenParameterSpec("secp256r1")
            keyPairGenerator.initialize(ecSpec)
            val keyPair = keyPairGenerator.generateKeyPair()

            val publicKey = keyPair.public as ECPublicKey
            val privateKey = keyPair.private as ECPrivateKey

            val ecKey =
                    ECKey.Builder(Curve.P_256, publicKey)
                            .privateKey(privateKey)
                            .keyID(UUID.randomUUID().toString())
                            .build()

            log.info("EC Key (P-256) generated with ID: {} (Algorithm: ES256)", ecKey.keyID)
            return ecKey
        } catch (e: Exception) {
            throw IllegalStateException("Failed to generate EC key", e)
        }
    }

    @Bean
    fun jwkSet(ecKey: ECKey): JWKSet {
        return JWKSet(ecKey)
    }

    @Bean
    fun jwsHeader(ecKey: ECKey): JwsHeader {
        return JwsHeader.with(SignatureAlgorithm.ES256).keyId(ecKey.keyID).build()
    }

    @Bean
    fun jwkSource(jwkSet: JWKSet): JWKSource<SecurityContext> {
        return ImmutableJWKSet(jwkSet)
    }

    @Bean
    fun jwtEncoder(jwkSource: JWKSource<SecurityContext>): JwtEncoder {
        return NimbusJwtEncoder(jwkSource)
    }
}
