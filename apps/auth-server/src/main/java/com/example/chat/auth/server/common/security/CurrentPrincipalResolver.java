package com.example.chat.auth.server.common.security;

import com.example.chat.auth.server.common.exception.AuthException;
import com.example.chat.auth.server.common.exception.AuthServerErrorCode;
import com.example.chat.auth.server.core.service.TokenService;
import com.nimbusds.jwt.JWTClaimsSet;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.UUID;

/**
 * 현재 인증된 Principal ID 추출기
 * <p>
 * Authorization 헤더의 Bearer 토큰을 검증하여 principalId 를 반환한다.
 */
@Component
@RequiredArgsConstructor
public class CurrentPrincipalResolver {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final TokenService tokenService;

    /**
     * HTTP 요청에서 인증된 Principal ID 를 추출한다.
     */
    public UUID resolve(HttpServletRequest request) {
        String token = extractBearerToken(request);
        JWTClaimsSet claims = tokenService.verify(token);
        return UUID.fromString(claims.getSubject());
    }

    /**
     * HTTP 요청에서 JWT Claims 전체를 추출한다.
     */
    public JWTClaimsSet resolveClaims(HttpServletRequest request) {
        String token = extractBearerToken(request);
        return tokenService.verify(token);
    }

    private String extractBearerToken(HttpServletRequest request) {
        String authHeader = request.getHeader(AUTHORIZATION_HEADER);
        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith(BEARER_PREFIX)) {
            throw new AuthException(AuthServerErrorCode.INVALID_TOKEN);
        }
        return authHeader.substring(BEARER_PREFIX.length());
    }
}
