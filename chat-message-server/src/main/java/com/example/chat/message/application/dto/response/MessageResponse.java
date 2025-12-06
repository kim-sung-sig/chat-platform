package com.example.chat.message.application.dto.response;

import com.example.chat.storage.domain.message.MessageStatus;
import com.example.chat.storage.domain.message.MessageType;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

/**
 * 메시지 응답 DTO
 */
@Getter
@Builder
public class MessageResponse {

    private Long id;
    private String roomId;
    private String channelId;
    private Long senderId;
    private MessageType messageType;
    private String contentJson;
    private MessageStatus status;
    private Instant sentAt;
    private Instant updatedAt;
    private Long replyToMessageId;
    private Boolean isEdited;
    private Boolean isDeleted;
}
