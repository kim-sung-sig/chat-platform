package com.example.chat.ops.github.connector.application;

import java.nio.charset.StandardCharsets;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.stereotype.Component;

@Component
public class GitHubWebhookSignatureVerifier {
    private static final String HMAC_SHA256 = "HmacSHA256";

    public boolean isValid(String rawPayload, String signatureHeader, String webhookSecret) {
        if (signatureHeader == null || !signatureHeader.startsWith("sha256=")) {
            return false;
        }
        String expected = signatureHeader.substring("sha256=".length());
        String actual = hex(hmac(rawPayload, webhookSecret));
        return MessageDigestSupport.constantTimeEquals(expected, actual);
    }

    private byte[] hmac(String payload, String secret) {
        try {
            Mac mac = Mac.getInstance(HMAC_SHA256);
            SecretKeySpec keySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_SHA256);
            mac.init(keySpec);
            return mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to calculate webhook signature", ex);
        }
    }

    private String hex(byte[] bytes) {
        StringBuilder builder = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            builder.append(String.format("%02x", b));
        }
        return builder.toString();
    }

    private static final class MessageDigestSupport {
        private MessageDigestSupport() {
        }

        private static boolean constantTimeEquals(String left, String right) {
            if (left.length() != right.length()) {
                return false;
            }
            int result = 0;
            for (int i = 0; i < left.length(); i++) {
                result |= left.charAt(i) ^ right.charAt(i);
            }
            return result == 0;
        }
    }
}
