package com.example.chat.auth.server.core.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.chat.auth.server.core.domain.AuthLevel;
import com.example.chat.auth.server.core.domain.Token;
import com.example.chat.auth.server.core.domain.TokenType;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

/**
 * JWT 토큰 서비스
 * - 토큰 생성, 검증
 * - AuthLevel에 따른 TTL 차등 적용
 * - Stateless 설계 (Redis/DB 불필요)
 */
@Service
public class TokenService {

    private final String secretKey;
    private final long accessTokenTtlMinutes;
    private final long refreshTokenTtlDays;
    private final long mfaTokenTtlMinutes;

    public TokenService(
            @Value("${auth.jwt.secret:your-256-bit-secret-key-change-this-in-production-environment}") String secretKey,
            @Value("${auth.jwt.access-ttl-minutes:15}") long accessTokenTtlMinutes,
            @Value("${auth.jwt.refresh-ttl-days:7}") long refreshTokenTtlDays,
            @Value("${auth.jwt.mfa-ttl-minutes:5}") long mfaTokenTtlMinutes) {
        this.secretKey = secretKey;
        this.accessTokenTtlMinutes = accessTokenTtlMinutes;
        this.refreshTokenTtlDays = refreshTokenTtlDays;
        this.mfaTokenTtlMinutes = mfaTokenTtlMinutes;
    }

    /**
     * 완전 인증 토큰 생성 (FULL_ACCESS)
     * - AuthLevel에 따라 TTL 차등 적용
     */
    public Token createFullAccessToken(UUID principalId, String identifier, AuthLevel authLevel) {
        Instant now = Instant.now();
        
        // AuthLevel에 따른 TTL 조정
        long ttlMinutes = calculateTtl(authLevel);
        Instant expiresAt = now.plus(ttlMinutes, ChronoUnit.MINUTES);
        Instant refreshExpiresAt = now.plus(refreshTokenTtlDays, ChronoUnit.DAYS);

        // Access Token
        String accessToken = buildJwt(principalId, identifier, authLevel, TokenType.FULL_ACCESS, expiresAt);
        
        // Refresh Token (AuthLevel 정보도 포함)
        String refreshToken = buildJwt(principalId, identifier, authLevel, TokenType.FULL_ACCESS, refreshExpiresAt);

        return new Token(
                accessToken,
                refreshToken,
                expiresAt,
                refreshExpiresAt,
                principalId,
                authLevel,
                TokenType.FULL_ACCESS
        );
    }

    /**
     * MFA 대기 토큰 생성 (MFA_PENDING)
     * - 짧은 TTL (5분)
     * - refresh token 없음
     * - MFA 완료 API만 접근 가능
     */
    public Token createMfaPendingToken(UUID principalId, String identifier, 
                                      AuthLevel currentLevel, String mfaSessionId) {
        Instant now = Instant.now();
        Instant expiresAt = now.plus(mfaTokenTtlMinutes, ChronoUnit.MINUTES);

        String accessToken = buildMfaJwt(principalId, identifier, currentLevel, mfaSessionId, expiresAt);

        return new Token(
                accessToken,
                null,  // MFA 토큰은 refresh 불가
                expiresAt,
                null,
                principalId,
                currentLevel,
                TokenType.MFA_PENDING
        );
    }

    /**
     * JWT 생성
     */
    private String buildJwt(UUID principalId, String identifier, AuthLevel authLevel, 
                           TokenType tokenType, Instant expiresAt) {
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

            SignedJWT signedJWT = new SignedJWT(
                    new JWSHeader(JWSAlgorithm.HS256),
                    claimsSet
            );

            signedJWT.sign(new MACSigner(secretKey.getBytes()));
            return signedJWT.serialize();

        } catch (JOSEException e) {
            throw new RuntimeException("Failed to create JWT", e);
        }
    }

    /**
     * MFA 토큰 생성 (특별한 claim 추가)
     */
    private String buildMfaJwt(UUID principalId, String identifier, AuthLevel authLevel,
                              String mfaSessionId, Instant expiresAt) {
        try {
            JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                    .subject(principalId.toString())
                    .claim("identifier", identifier)
                    .claim("auth_level", authLevel.name())
                    .claim("auth_level_value", authLevel.getLevel())
                    .claim("token_type", TokenType.MFA_PENDING.name())
                    .claim("mfa_session_id", mfaSessionId)  // MFA 세션 ID 포함
                    .claim("mfa_role", "MFA_USER")          // MFA 전용 role
                    .issueTime(new Date())
                    .expirationTime(Date.from(expiresAt))
                    .jwtID(UUID.randomUUID().toString())
                    .build();

            SignedJWT signedJWT = new SignedJWT(
                    new JWSHeader(JWSAlgorithm.HS256),
                    claimsSet
            );

            signedJWT.sign(new MACSigner(secretKey.getBytes()));
            return signedJWT.serialize();

        } catch (JOSEException e) {
            throw new RuntimeException("Failed to create MFA JWT", e);
        }
    }

    /**
     * AuthLevel에 따른 TTL 계산
     * - LOW: 기본 TTL
     * - MEDIUM: 2배
     * - HIGH: 4배
     */
    private long calculateTtl(AuthLevel authLevel) {
        return switch (authLevel) {
            case HIGH -> accessTokenTtlMinutes * 4;    // 60분
            case MEDIUM -> accessTokenTtlMinutes * 2;  // 30분
            case LOW -> accessTokenTtlMinutes;         // 15분
        };
    }

    /**
     * JWT 검증 및 파싱
     */
    public JWTClaimsSet verify(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            
            // 서명 검증
            JWSVerifier verifier = new MACVerifier(secretKey.getBytes());
            if (!signedJWT.verify(verifier)) {
                throw new RuntimeException("Invalid JWT signature");
            }

            JWTClaimsSet claims = signedJWT.getJWTClaimsSet();

            // 만료 확인
            if (claims.getExpirationTime().before(new Date())) {
                throw new RuntimeException("JWT expired");
            }

            return claims;

        } catch (Exception e) {
            throw new RuntimeException("Failed to verify JWT", e);
        }
    }

    /**
     * JWT에서 AuthLevel 추출
     */
    public AuthLevel extractAuthLevel(String token) {
        JWTClaimsSet claims = verify(token);
        String levelName = (String) claims.getClaim("auth_level");
        return AuthLevel.valueOf(levelName);
    }

    /**
     * JWT에서 TokenType 추출
     */
    public TokenType extractTokenType(String token) {
        JWTClaimsSet claims = verify(token);
        String typeName = (String) claims.getClaim("token_type");
        return TokenType.valueOf(typeName);
    }
}
