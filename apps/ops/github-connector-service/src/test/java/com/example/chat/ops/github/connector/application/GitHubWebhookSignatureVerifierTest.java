package com.example.chat.ops.github.connector.application;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class GitHubWebhookSignatureVerifierTest {
    private final GitHubWebhookSignatureVerifier verifier = new GitHubWebhookSignatureVerifier();

    @Test
    @DisplayName("matches valid sha256 webhook signature")
    void matchesValidSignature() throws Exception {
        String payload = "{\"action\":\"opened\"}";
        String secret = "my-secret";
        String signature = "sha256=" + sign(payload, secret);

        assertThat(verifier.isValid(payload, signature, secret)).isTrue();
    }

    @Test
    @DisplayName("rejects invalid signature")
    void rejectsInvalidSignature() {
        assertThat(verifier.isValid("{}", "sha256=deadbeef", "secret")).isFalse();
    }

    private String sign(String payload, String secret) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] bytes = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));

        StringBuilder builder = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            builder.append(String.format("%02x", b));
        }
        return builder.toString();
    }
}
