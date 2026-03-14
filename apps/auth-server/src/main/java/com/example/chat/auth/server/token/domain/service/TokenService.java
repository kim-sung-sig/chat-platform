package com.example.chat.auth.server.token.domain.service;

import com.example.chat.auth.server.shared.exception.AuthException;
import com.example.chat.auth.server.shared.exception.AuthServerErrorCode;
import com.example.chat.auth.server.auth.domain.AuthLevel;
import com.example.chat.auth.server.token.domain.RefreshToken;
import com.example.chat.auth.server.token.domain.Token;
import com.example.chat.auth.server.token.domain.TokenType;
import com.example.chat.auth.server.auth.domain.credential.Device;
import com.example.chat.auth.server.token.domain.repository.RefreshTokenRepository;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.ECDSASigner;
import com.nimbusds.jose.crypto.ECDSAVerifier;
import com.nimbusds.jose.jwk.ECKey;
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
 * JWT token service
 */
@Service
@Slf4j
public class TokenService {

    private static final String TOKEN_TYPE_CLAIM = "token_type";
    private static final String MFA_SESSION_ID_CLAIM = "mfa_session_id";
    private static final String MFA_ROLE_CLAIM = "mfa_role";

    private final ECKey ecKey;
    private final long accessTokenTtlMinutes;
    private final long refreshTokenTtlDays;
    private final long mfaTokenTtlMinutes;
    private final RefreshTokenRepository refreshTokenRepository;

    public TokenService(
            ECKey ecKey,
            @Value("${auth.jwt.access-ttl-minutes:15}") long accessTokenTtlMinutes,
            @Value("${auth.jwt.refresh-ttl-days:7}") long refreshTokenTtlDays,
            @Value("${auth.jwt.mfa-ttl-minutes:5}") long mfaTokenTtlMinutes,
            RefreshTokenRepository refreshTokenRepository) {
        this.ecKey = ecKey;
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
        String refreshTokenVal = buildJwt(principalId, identifier, authLevel, TokenType.REFRESH, refreshExpiresAt);

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
                    .claim(TOKEN_TYPE_CLAIM, tokenType.name())
                    .issueTime(new Date())
                    .expirationTime(Date.from(expiresAt))
                    .jwtID(UUID.randomUUID().toString())
                    .build();

            JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.ES256)
                    .keyID(ecKey.getKeyID())
                    .build();

            SignedJWT signedJWT = new SignedJWT(header, claimsSet);
            signedJWT.sign(new ECDSASigner(ecKey.toECPrivateKey()));
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
                    .claim(TOKEN_TYPE_CLAIM, TokenType.MFA_PENDING.name())
                    .claim(MFA_SESSION_ID_CLAIM, mfaSessionId)
                    .claim(MFA_ROLE_CLAIM, "MFA_USER")
                    .issueTime(new Date())
                    .expirationTime(Date.from(expiresAt))
                    .jwtID(UUID.randomUUID().toString())
                    .build();

            JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.ES256)
                    .keyID(ecKey.getKeyID())
                    .build();

            SignedJWT signedJWT = new SignedJWT(header, claimsSet);
            signedJWT.sign(new ECDSASigner(ecKey.toECPrivateKey()));
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
            JWSVerifier verifier = new ECDSAVerifier(ecKey.toECPublicKey());
            if (!signedJWT.verify(verifier)) {
                throw new AuthException(AuthServerErrorCode.TOKEN_SIGNATURE_INVALID);
            }

            JWTClaimsSet claims = signedJWT.getJWTClaimsSet();
            if (claims.getExpirationTime().before(new Date())) {
                throw new AuthException(AuthServerErrorCode.TOKEN_EXPIRED);
            }
            ensureTokenTypePresent(claims);
            return claims;
        } catch (AuthException e) {
            throw e;
        } catch (Exception e) {
            throw new AuthException(AuthServerErrorCode.INVALID_TOKEN, null, e);
        }
    }

    public JWTClaimsSet verifyAccessToken(String token) {
        JWTClaimsSet claims = verify(token);
        TokenType tokenType = getTokenType(claims);
        if (tokenType != TokenType.FULL_ACCESS) {
            throw new AuthException(AuthServerErrorCode.INVALID_TOKEN);
        }
        return claims;
    }

    public JWTClaimsSet verifyRefreshToken(String token) {
        JWTClaimsSet claims = verify(token);
        TokenType tokenType = getTokenType(claims);
        if (tokenType != TokenType.REFRESH) {
            throw new AuthException(AuthServerErrorCode.INVALID_TOKEN);
        }
        return claims;
    }

    public JWTClaimsSet verifyMfaToken(String token) {
        JWTClaimsSet claims = verify(token);
        TokenType tokenType = getTokenType(claims);
        if (tokenType != TokenType.MFA_PENDING) {
            throw new AuthException(AuthServerErrorCode.INVALID_TOKEN);
        }
        return claims;
    }

    public Token rotateRefreshToken(String refreshTokenVal, Device device) {
        JWTClaimsSet claims = verifyRefreshToken(refreshTokenVal);
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

    private void ensureTokenTypePresent(JWTClaimsSet claims) {
        Object tokenType = claims.getClaim(TOKEN_TYPE_CLAIM);
        if (tokenType == null) {
            throw new AuthException(AuthServerErrorCode.INVALID_TOKEN);
        }
        getTokenType(claims);
    }

    private TokenType getTokenType(JWTClaimsSet claims) {
        Object tokenType = claims.getClaim(TOKEN_TYPE_CLAIM);
        if (tokenType == null) {
            throw new AuthException(AuthServerErrorCode.INVALID_TOKEN);
        }
        try {
            return TokenType.valueOf(tokenType.toString());
        } catch (IllegalArgumentException e) {
            throw new AuthException(AuthServerErrorCode.INVALID_TOKEN, null, e);
        }
    }
}
