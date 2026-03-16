package com.example.chat.voice.domain.model;

import java.time.Instant;
import java.util.Objects;

public class VoiceParticipant {
    private final String userId;
    private final Instant joinedAt;

    public VoiceParticipant(String userId, Instant joinedAt) {
        this.userId = Objects.requireNonNull(userId, "userId");
        this.joinedAt = Objects.requireNonNull(joinedAt, "joinedAt");
    }

    public String getUserId() {
        return userId;
    }

    public Instant getJoinedAt() {
        return joinedAt;
    }
}
