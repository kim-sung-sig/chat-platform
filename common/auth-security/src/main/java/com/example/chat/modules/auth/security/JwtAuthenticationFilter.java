package com.example.chat.modules.auth.security;

import java.io.IOException;

import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.chat.modules.auth.jwt.JwtTokenProvider;
import com.example.chat.modules.auth.jwt.TokenBlacklistService;
import com.example.chat.modules.auth.user.UserPrincipal;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;
    private final TokenBlacklistService blacklistService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        String token = resolveToken(request);

        if (token != null && validateToken(token)) {
            Jws<Claims> claimsJws = tokenProvider.validateAndParseToken(token);
            Claims payload = claimsJws.getPayload();

            String userId = payload.getSubject();
            String role = payload.get("role", String.class);

            UserPrincipal principal = new UserPrincipal(userId, role);
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(principal,
                    token, principal.getAuthorities());

            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    private boolean validateToken(String token) {
        try {
            // Basic signature/expiry check
            tokenProvider.validateAndParseToken(token);
            // Blacklist check
            return blacklistService == null || !blacklistService.isBlacklisted(token);
        } catch (Exception e) {
            // Log error or just return false
            return false;
        }
    }
}
