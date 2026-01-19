package com.example.chat.auth.server.api.dto.factory;

import org.springframework.stereotype.Component;

import com.example.chat.auth.server.api.dto.request.AuthenticateRequest;
import com.example.chat.auth.server.core.domain.Credential;
import com.example.chat.auth.server.core.domain.credential.OtpCredential;
import com.example.chat.auth.server.core.domain.credential.PasswordCredential;
import com.example.chat.auth.server.core.domain.credential.SocialCredential;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Credential Factory
 * - HTTP 요청 DTO를 도메인 Credential로 변환
 * - credentialType에 따라 적절한 Credential 생성
 */
@Component
public class CredentialFactory {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * AuthenticateRequest → Credential
     * 
     * credentialData 형식:
     * - PASSWORD: 평문 비밀번호 문자열
     * - SOCIAL: JSON {"provider": "GOOGLE", "token": "oauth_token", "email": "user@example.com"}
     * - OTP: JSON {"code": "123456", "deliveryMethod": "SMS"}
     */
    public Credential createFromRequest(AuthenticateRequest request) {
        String type = request.getCredentialType();
        String data = request.getCredentialData();

        return switch (type.toUpperCase()) {
            case "PASSWORD" -> createPasswordCredential(data);
            case "SOCIAL" -> createSocialCredential(data);
            case "OTP" -> createOtpCredential(data);
            case "PASSKEY" -> throw new UnsupportedOperationException("Passkey not yet implemented");
            default -> throw new IllegalArgumentException("Unknown credential type: " + type);
        };
    }

    /**
     * 비밀번호 자격증명 생성
     * data: 평문 비밀번호
     */
    private Credential createPasswordCredential(String plainPassword) {
        // 임시 해시 (실제로는 저장된 해시가 필요하지만, 여기서는 검증용으로만 사용)
        return new PasswordCredential(null, plainPassword);
    }

    /**
     * 소셜 자격증명 생성
     * data: {"provider": "GOOGLE", "token": "...", "email": "..."}
     */
    private Credential createSocialCredential(String jsonData) {
        try {
            JsonNode json = objectMapper.readTree(jsonData);
            String provider = json.get("provider").asText();
            String token = json.get("token").asText();
            String email = json.has("email") ? json.get("email").asText() : null;

            // OAuth 토큰에서 사용자 ID 추출 (실제로는 OAuth 서버에 검증)
            String socialUserId = extractUserIdFromToken(provider, token);

            return new SocialCredential(provider, socialUserId, email, false);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid social credential data", e);
        }
    }

    /**
     * OTP 자격증명 생성
     * data: {"code": "123456", "deliveryMethod": "SMS"}
     */
    private Credential createOtpCredential(String jsonData) {
        try {
            JsonNode json = objectMapper.readTree(jsonData);
            String code = json.get("code").asText();
            String deliveryMethod = json.get("deliveryMethod").asText();

            return new OtpCredential(code, deliveryMethod);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid OTP credential data", e);
        }
    }

    /**
     * OAuth 토큰에서 사용자 ID 추출 (실제로는 OAuth 서버 호출)
     */
    private String extractUserIdFromToken(String provider, String token) {
        // TODO: 실제 OAuth 공급자 검증
        // - Google: Google OAuth API 호출
        // - Kakao: Kakao OAuth API 호출
        // - Apple: Apple Sign In 검증
        
        // 임시 구현
        return "social-user-" + token.substring(0, Math.min(10, token.length()));
    }
}
