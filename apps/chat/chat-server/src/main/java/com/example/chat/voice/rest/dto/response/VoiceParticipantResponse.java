package com.example.chat.voice.rest.dto.response;

import java.time.Instant;

public record VoiceParticipantResponse(
        String userId,
        Instant joinedAt
) {}
