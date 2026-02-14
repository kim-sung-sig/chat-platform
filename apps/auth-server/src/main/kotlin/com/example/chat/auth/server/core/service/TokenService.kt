package com.example.chat.auth.server.core.service

import com.example.chat.auth.server.common.exception.AuthException
import com.example.chat.auth.server.common.exception.AuthServerErrorCode
import com.example.chat.auth.server.core.domain.AuthLevel
import com.example.chat.auth.server.core.domain.RefreshToken
import com.example.chat.auth.server.core.domain.Token
import com.example.chat.auth.server.core.domain.TokenType
import com.example.chat.auth.server.core.domain.credential.Device
import com.example.chat.auth.server.core.repository.RefreshTokenRepository
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.crypto.MACSigner
import com.nimbusds.jose.crypto.MACVerifier
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

/** JWT 토큰 서비스 */
@Service
class TokenService(
        @Value("\${auth.jwt.secret:your-256-bit-secret-key-change-this-in-production-environment}")
        private val secretKey: String,
        @Value("\${auth.jwt.access-ttl-minutes:15}") private val accessTokenTtlMinutes: Long,
        @Value("\${auth.jwt.refresh-ttl-days:7}") private val refreshTokenTtlDays: Long,
        @Value("\${auth.jwt.mfa-ttl-minutes:5}") private val mfaTokenTtlMinutes: Long,
        private val refreshTokenRepository: RefreshTokenRepository
) {
    fun createFullAccessToken(
            principalId: UUID,
            identifier: String,
            authLevel: AuthLevel,
            device: Device
    ): Token {
        val now = Instant.now()
        val ttlMinutes = calculateTtl(authLevel)
        val expiresAt = now.plus(ttlMinutes, ChronoUnit.MINUTES)
        val refreshExpiresAt = now.plus(refreshTokenTtlDays, ChronoUnit.DAYS)

        val accessToken =
                buildJwt(principalId, identifier, authLevel, TokenType.FULL_ACCESS, expiresAt)
        val refreshTokenVal =
                buildJwt(
                        principalId,
                        identifier,
                        authLevel,
                        TokenType.FULL_ACCESS,
                        refreshExpiresAt
                )

        val refreshToken =
                RefreshToken(
                        id = UUID.randomUUID(),
                        principalId = principalId,
                        device = device,
                        tokenValue = refreshTokenVal,
                        expiryAt = refreshExpiresAt,
                        createdAt = now,
                        lastUsedAt = now
                )
        refreshTokenRepository.save(refreshToken)

        return Token(
                accessToken = accessToken,
                refreshToken = refreshTokenVal,
                expiresAt = expiresAt,
                refreshExpiresAt = refreshExpiresAt,
                principalId = principalId,
                authLevel = authLevel,
                tokenType = TokenType.FULL_ACCESS
        )
    }

    fun createMfaPendingToken(
            principalId: UUID,
            identifier: String,
            currentLevel: AuthLevel,
            mfaSessionId: String
    ): Token {
        val now = Instant.now()
        val expiresAt = now.plus(mfaTokenTtlMinutes, ChronoUnit.MINUTES)

        val accessToken =
                buildMfaJwt(principalId, identifier, currentLevel, mfaSessionId, expiresAt)

        return Token(
                accessToken = accessToken,
                refreshToken = null,
                expiresAt = expiresAt,
                refreshExpiresAt = null,
                principalId = principalId,
                authLevel = currentLevel,
                tokenType = TokenType.MFA_PENDING
        )
    }

    private fun buildJwt(
            principalId: UUID,
            identifier: String,
            authLevel: AuthLevel,
            tokenType: TokenType,
            expiresAt: Instant
    ): String {
        return try {
            val claimsSet =
                    JWTClaimsSet.Builder()
                            .subject(principalId.toString())
                            .claim("identifier", identifier)
                            .claim("auth_level", authLevel.name)
                            .claim("auth_level_value", authLevel.level)
                            .claim("token_type", tokenType.name)
                            .issueTime(Date())
                            .expirationTime(Date.from(expiresAt))
                            .jwtID(UUID.randomUUID().toString())
                            .build()

            val signedJWT = SignedJWT(JWSHeader(JWSAlgorithm.HS256), claimsSet)
            signedJWT.sign(MACSigner(secretKey.toByteArray()))
            signedJWT.serialize()
        } catch (e: Exception) {
            throw AuthException(AuthServerErrorCode.INTERNAL_SERVER_ERROR, cause = e)
        }
    }

    private fun buildMfaJwt(
            principalId: UUID,
            identifier: String,
            authLevel: AuthLevel,
            mfaSessionId: String,
            expiresAt: Instant
    ): String {
        return try {
            val claimsSet =
                    JWTClaimsSet.Builder()
                            .subject(principalId.toString())
                            .claim("identifier", identifier)
                            .claim("auth_level", authLevel.name)
                            .claim("auth_level_value", authLevel.level)
                            .claim("token_type", TokenType.MFA_PENDING.name)
                            .claim("mfa_session_id", mfaSessionId)
                            .claim("mfa_role", "MFA_USER")
                            .issueTime(Date())
                            .expirationTime(Date.from(expiresAt))
                            .jwtID(UUID.randomUUID().toString())
                            .build()

            val signedJWT = SignedJWT(JWSHeader(JWSAlgorithm.HS256), claimsSet)
            signedJWT.sign(MACSigner(secretKey.toByteArray()))
            signedJWT.serialize()
        } catch (e: Exception) {
            throw AuthException(AuthServerErrorCode.INTERNAL_SERVER_ERROR, cause = e)
        }
    }

    private fun calculateTtl(authLevel: AuthLevel): Long {
        return when (authLevel) {
            AuthLevel.HIGH -> accessTokenTtlMinutes * 4
            AuthLevel.MEDIUM -> accessTokenTtlMinutes * 2
            AuthLevel.LOW -> accessTokenTtlMinutes
        }
    }

    fun verify(token: String): JWTClaimsSet {
        return try {
            val signedJWT = SignedJWT.parse(token)
            val verifier = MACVerifier(secretKey.toByteArray())
            if (!signedJWT.verify(verifier)) {
                throw AuthException(AuthServerErrorCode.TOKEN_SIGNATURE_INVALID)
            }

            val claims = signedJWT.jwtClaimsSet
            if (claims.expirationTime.before(Date())) {
                throw AuthException(AuthServerErrorCode.TOKEN_EXPIRED)
            }
            claims
        } catch (e: AuthException) {
            throw e
        } catch (e: Exception) {
            throw AuthException(AuthServerErrorCode.INVALID_TOKEN, cause = e)
        }
    }

    fun rotateRefreshToken(refreshTokenVal: String, device: Device): Token {
        val claims = verify(refreshTokenVal)
        val refreshToken =
                refreshTokenRepository.findByTokenValue(refreshTokenVal).orElseThrow {
                    AuthException(AuthServerErrorCode.REFRESH_TOKEN_NOT_FOUND)
                }

        if (refreshToken.isExpired()) {
            throw AuthException(AuthServerErrorCode.TOKEN_EXPIRED)
        }
        if (!refreshToken.device.isSameDevice(device)) {
            throw AuthException(AuthServerErrorCode.ACCESS_DENIED)
        }

        val principalId = refreshToken.principalId
        val identifier = claims.getClaim("identifier") as String
        val authLevel = AuthLevel.valueOf(claims.getClaim("auth_level") as String)

        refreshTokenRepository.deleteByTokenValue(refreshTokenVal)
        return createFullAccessToken(principalId, identifier, authLevel, device)
    }
}
