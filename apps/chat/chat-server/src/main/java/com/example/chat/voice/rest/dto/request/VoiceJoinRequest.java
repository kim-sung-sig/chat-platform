package com.example.chat.voice.rest.dto.request;

import jakarta.validation.constraints.NotBlank;

public record VoiceJoinRequest(
        @NotBlank(message = "userId is required") String userId
) {}
