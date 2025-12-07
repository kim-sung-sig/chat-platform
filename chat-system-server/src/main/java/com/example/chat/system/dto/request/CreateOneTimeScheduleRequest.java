package com.example.chat.system.dto.request;

import com.example.chat.storage.domain.message.MessageType;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 단발성 스케줄 생성 요청 DTO
 */
@Getter
@Builder
public class CreateOneTimeScheduleRequest {

    @NotBlank(message = "roomId is required")
    private String roomId;

    private String channelId;

    @NotNull(message = "messageType is required")
    private MessageType messageType;

    @NotNull(message = "payload is required")
    private Map<String, Object> payload;

    @NotNull(message = "executeAt is required")
    @Future(message = "executeAt must be in the future")
    private LocalDateTime executeAt;
}
