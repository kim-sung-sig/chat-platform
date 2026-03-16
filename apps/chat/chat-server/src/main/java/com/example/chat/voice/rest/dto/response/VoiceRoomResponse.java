package com.example.chat.voice.rest.dto.response;

import java.util.List;

public record VoiceRoomResponse(
        String channelId,
        String status,
        List<VoiceParticipantResponse> participants
) {}
