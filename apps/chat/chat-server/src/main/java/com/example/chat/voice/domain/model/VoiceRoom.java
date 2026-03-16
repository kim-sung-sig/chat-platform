package com.example.chat.voice.domain.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class VoiceRoom {
    private final String channelId;
    private final Instant createdAt;
    private VoiceRoomStatus status;
    private final Map<String, VoiceParticipant> participants;

    public VoiceRoom(String channelId, Instant createdAt) {
        this.channelId = channelId;
        this.createdAt = createdAt;
        this.status = VoiceRoomStatus.ACTIVE;
        this.participants = new ConcurrentHashMap<>();
    }

    public String getChannelId() {
        return channelId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public VoiceRoomStatus getStatus() {
        return status;
    }

    public void addParticipant(String userId, Instant joinedAt) {
        participants.putIfAbsent(userId, new VoiceParticipant(userId, joinedAt));
    }

    public void removeParticipant(String userId) {
        participants.remove(userId);
    }

    public List<VoiceParticipant> getParticipants() {
        return Collections.unmodifiableList(new ArrayList<>(participants.values()));
    }

    public boolean hasParticipant(String userId) {
        return participants.containsKey(userId);
    }
}
