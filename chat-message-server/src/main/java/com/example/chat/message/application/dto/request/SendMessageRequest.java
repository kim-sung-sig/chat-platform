package com.example.chat.message.application.dto.request;

import com.example.chat.storage.domain.message.MessageType;
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

    @NotBlank(message = "roomId is required")
    private String roomId;

    private String channelId;

    @NotNull(message = "messageType is required")
    private MessageType messageType;

    @NotNull(message = "payload is required")
    private Map<String, Object> payload;

    private Long replyToMessageId;
}
