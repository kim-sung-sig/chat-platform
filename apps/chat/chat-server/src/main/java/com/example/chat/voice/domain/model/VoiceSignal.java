package com.example.chat.voice.domain.model;

import java.time.Instant;
import java.util.Objects;

public class VoiceSignal {
    private final String id;
    private final String channelId;
    private final String fromUserId;
    private final String toUserId;
    private final VoiceSignalType type;
    private final String payload;
    private final Instant createdAt;

    public VoiceSignal(String id, String channelId, String fromUserId, String toUserId, VoiceSignalType type, String payload, Instant createdAt) {
        this.id = Objects.requireNonNull(id, "id");
        this.channelId = Objects.requireNonNull(channelId, "channelId");
        this.fromUserId = Objects.requireNonNull(fromUserId, "fromUserId");
        this.toUserId = Objects.requireNonNull(toUserId, "toUserId");
        this.type = Objects.requireNonNull(type, "type");
        this.payload = Objects.requireNonNull(payload, "payload");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt");
    }

    public String getId() {
        return id;
    }

    public String getChannelId() {
        return channelId;
    }

    public String getFromUserId() {
        return fromUserId;
    }

    public String getToUserId() {
        return toUserId;
    }

    public VoiceSignalType getType() {
        return type;
    }

    public String getPayload() {
        return payload;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
