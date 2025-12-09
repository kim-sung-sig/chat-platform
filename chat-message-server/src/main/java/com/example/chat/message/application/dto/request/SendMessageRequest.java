package com.example.chat.message.application.dto.request;

import com.example.chat.domain.message.MessageType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;

/**
 * 메시지 발송 요청 DTO
 */
@Getter
@Builder
public class SendMessageRequest {

    @NotBlank(message = "channelId is required")
    private String channelId;

    @NotNull(message = "messageType is required")
    private MessageType messageType;

    @NotNull(message = "payload is required")
    private Map<String, Object> payload;
}
