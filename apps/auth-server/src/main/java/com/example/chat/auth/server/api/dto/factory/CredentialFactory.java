package com.example.chat.auth.server.api.dto.factory;

import com.example.chat.auth.server.api.dto.request.AuthenticateRequest;
import com.example.chat.auth.server.common.exception.AuthException;
import com.example.chat.auth.server.common.exception.AuthServerErrorCode;
import com.example.chat.auth.server.core.domain.Credential;
import com.example.chat.auth.server.core.domain.credential.OtpCredential;
import com.example.chat.auth.server.core.domain.credential.PasswordCredential;
import com.example.chat.auth.server.core.domain.credential.SocialCredential;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Credential Factory
 */
@Component
@RequiredArgsConstructor
public class CredentialFactory {
    private final ObjectMapper objectMapper = new ObjectMapper();

    public Credential createFromRequest(AuthenticateRequest request) {
        String type = request.credentialType();
        if (type == null) {
            throw new AuthException(AuthServerErrorCode.INVALID_CREDENTIALS);
        }
        String data = request.credentialData();
        if (data == null) {
            throw new AuthException(AuthServerErrorCode.INVALID_CREDENTIALS);
        }

        return switch (type.toUpperCase()) {
            case "PASSWORD" -> createPasswordCredential(data);
            case "SOCIAL" -> createSocialCredential(data);
            case "OTP" -> createOtpCredential(data);
            case "PASSKEY" -> throw new AuthException(AuthServerErrorCode.BAD_REQUEST);
            default -> throw new AuthException(AuthServerErrorCode.INVALID_CREDENTIALS);
        };
    }

    private Credential createPasswordCredential(String plainPassword) {
        return new PasswordCredential(null, plainPassword);
    }

    private Credential createSocialCredential(String jsonData) {
        try {
            JsonNode json = objectMapper.readTree(jsonData);
            String provider = json.get("provider").asText();
            String token = json.get("token").asText();
            String email = json.has("email") ? json.get("email").asText() : null;

            String socialUserId = extractUserIdFromToken(provider, token);
            return new SocialCredential(provider, socialUserId, email, false);
        } catch (Exception e) {
            throw new AuthException(AuthServerErrorCode.INVALID_CREDENTIALS, null, e);
        }
    }

    private Credential createOtpCredential(String jsonData) {
        try {
            JsonNode json = objectMapper.readTree(jsonData);
            String code = json.get("code").asText();
            String deliveryMethod = json.get("deliveryMethod").asText();

            return new OtpCredential(code, deliveryMethod);
        } catch (Exception e) {
            throw new AuthException(AuthServerErrorCode.INVALID_CREDENTIALS, null, e);
        }
    }

    private String extractUserIdFromToken(String provider, String token) {
        return "social-user-" + token.substring(0, Math.min(token.length(), 10));
    }
}
