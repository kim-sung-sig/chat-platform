package com.example.chat.message.application.dto.request;

import java.util.Map;

import com.example.chat.domain.message.MessageType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 메시지 발송 요청 DTO
 *
 * Validation을 통한 조기 에러 표출
 */
public record SendMessageRequest(
        @NotBlank(message = "channelId is required") String channelId,

        @NotNull(message = "messageType is required") MessageType messageType,

        @NotNull(message = "payload is required") Map<String, Object> payload) {
}
