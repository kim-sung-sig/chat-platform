package com.example.chat.auth.server.core.service;

import com.example.chat.auth.server.common.exception.AuthException;
import com.example.chat.auth.server.common.exception.AuthServerErrorCode;
import com.example.chat.auth.server.core.domain.AuthLevel;
import com.example.chat.auth.server.core.domain.RefreshToken;
import com.example.chat.auth.server.core.domain.Token;
import com.example.chat.auth.server.core.domain.TokenType;
import com.example.chat.auth.server.core.domain.credential.Device;
import com.example.chat.auth.server.core.repository.RefreshTokenRepository;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

/**
 * JWT 토큰 서비스
 */
@Service
@Slf4j
public class TokenService {

    private final String secretKey;
    private final long accessTokenTtlMinutes;
    private final long refreshTokenTtlDays;
    private final long mfaTokenTtlMinutes;
    private final RefreshTokenRepository refreshTokenRepository;

    public TokenService(
            @Value("${auth.jwt.secret:your-256-bit-secret-key-change-this-in-production-environment}") String secretKey,
            @Value("${auth.jwt.access-ttl-minutes:15}") long accessTokenTtlMinutes,
            @Value("${auth.jwt.refresh-ttl-days:7}") long refreshTokenTtlDays,
            @Value("${auth.jwt.mfa-ttl-minutes:5}") long mfaTokenTtlMinutes,
            RefreshTokenRepository refreshTokenRepository) {
        this.secretKey = secretKey;
        this.accessTokenTtlMinutes = accessTokenTtlMinutes;
        this.refreshTokenTtlDays = refreshTokenTtlDays;
        this.mfaTokenTtlMinutes = mfaTokenTtlMinutes;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    public Token createFullAccessToken(
            UUID principalId,
            String identifier,
            AuthLevel authLevel,
            Device device) {
        Instant now = Instant.now();
        long ttlMinutes = calculateTtl(authLevel);
        Instant expiresAt = now.plus(ttlMinutes, ChronoUnit.MINUTES);
        Instant refreshExpiresAt = now.plus(refreshTokenTtlDays, ChronoUnit.DAYS);

        String accessToken = buildJwt(principalId, identifier, authLevel, TokenType.FULL_ACCESS, expiresAt);
        String refreshTokenVal = buildJwt(principalId, identifier, authLevel, TokenType.FULL_ACCESS, refreshExpiresAt);

        RefreshToken refreshToken = RefreshToken.builder()
                .id(UUID.randomUUID())
                .principalId(principalId)
                .device(device)
                .tokenValue(refreshTokenVal)
                .expiryAt(refreshExpiresAt)
                .createdAt(now)
                .lastUsedAt(now)
                .build();

        refreshTokenRepository.save(refreshToken);

        return new Token(
                accessToken,
                refreshTokenVal,
                expiresAt,
                refreshExpiresAt,
                principalId,
                authLevel,
                TokenType.FULL_ACCESS);
    }

    public Token createMfaPendingToken(
            UUID principalId,
            String identifier,
            AuthLevel currentLevel,
            String mfaSessionId) {
        Instant now = Instant.now();
        Instant expiresAt = now.plus(mfaTokenTtlMinutes, ChronoUnit.MINUTES);

        String accessToken = buildMfaJwt(principalId, identifier, currentLevel, mfaSessionId, expiresAt);

        return new Token(
                accessToken,
                null,
                expiresAt,
                null,
                principalId,
                currentLevel,
                TokenType.MFA_PENDING);
    }

    private String buildJwt(
            UUID principalId,
            String identifier,
            AuthLevel authLevel,
            TokenType tokenType,
            Instant expiresAt) {
        try {
            JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                    .subject(principalId.toString())
                    .claim("identifier", identifier)
                    .claim("auth_level", authLevel.name())
                    .claim("auth_level_value", authLevel.getLevel())
                    .claim("token_type", tokenType.name())
                    .issueTime(new Date())
                    .expirationTime(Date.from(expiresAt))
                    .jwtID(UUID.randomUUID().toString())
                    .build();

            SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claimsSet);
            signedJWT.sign(new MACSigner(secretKey.getBytes()));
            return signedJWT.serialize();
        } catch (Exception e) {
            throw new AuthException(AuthServerErrorCode.INTERNAL_SERVER_ERROR, null, e);
        }
    }

    private String buildMfaJwt(
            UUID principalId,
            String identifier,
            AuthLevel authLevel,
            String mfaSessionId,
            Instant expiresAt) {
        try {
            JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                    .subject(principalId.toString())
                    .claim("identifier", identifier)
                    .claim("auth_level", authLevel.name())
                    .claim("auth_level_value", authLevel.getLevel())
                    .claim("token_type", TokenType.MFA_PENDING.name())
                    .claim("mfa_session_id", mfaSessionId)
                    .claim("mfa_role", "MFA_USER")
                    .issueTime(new Date())
                    .expirationTime(Date.from(expiresAt))
                    .jwtID(UUID.randomUUID().toString())
                    .build();

            SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claimsSet);
            signedJWT.sign(new MACSigner(secretKey.getBytes()));
            return signedJWT.serialize();
        } catch (Exception e) {
            throw new AuthException(AuthServerErrorCode.INTERNAL_SERVER_ERROR, null, e);
        }
    }

    private long calculateTtl(AuthLevel authLevel) {
        return switch (authLevel) {
            case HIGH -> accessTokenTtlMinutes * 4;
            case MEDIUM -> accessTokenTtlMinutes * 2;
            case LOW -> accessTokenTtlMinutes;
        };
    }

    public JWTClaimsSet verify(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            MACVerifier verifier = new MACVerifier(secretKey.getBytes());
            if (!signedJWT.verify(verifier)) {
                throw new AuthException(AuthServerErrorCode.TOKEN_SIGNATURE_INVALID);
            }

            JWTClaimsSet claims = signedJWT.getJWTClaimsSet();
            if (claims.getExpirationTime().before(new Date())) {
                throw new AuthException(AuthServerErrorCode.TOKEN_EXPIRED);
            }
            return claims;
        } catch (AuthException e) {
            throw e;
        } catch (Exception e) {
            throw new AuthException(AuthServerErrorCode.INVALID_TOKEN, null, e);
        }
    }

    public Token rotateRefreshToken(String refreshTokenVal, Device device) {
        JWTClaimsSet claims = verify(refreshTokenVal);
        RefreshToken refreshToken = refreshTokenRepository.findByTokenValue(refreshTokenVal)
                .orElseThrow(() -> new AuthException(AuthServerErrorCode.REFRESH_TOKEN_NOT_FOUND));

        if (refreshToken.isExpired()) {
            throw new AuthException(AuthServerErrorCode.TOKEN_EXPIRED);
        }
        if (!refreshToken.getDevice().isSameDevice(device)) {
            throw new AuthException(AuthServerErrorCode.ACCESS_DENIED);
        }

        UUID principalId = refreshToken.getPrincipalId();
        String identifier = (String) claims.getClaim("identifier");
        AuthLevel authLevel = AuthLevel.valueOf((String) claims.getClaim("auth_level"));

        refreshTokenRepository.deleteByTokenValue(refreshTokenVal);
        return createFullAccessToken(principalId, identifier, authLevel, device);
    }
}
