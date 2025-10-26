package com.example.chat.system.dto.response;

import com.example.chat.system.domain.entity.Message;
import com.example.chat.system.domain.enums.MessageStatus;
import com.example.chat.system.domain.enums.MessageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 메시지 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageResponse {

    private Long id;
    private Long channelId;
    private String channelName;
    private String title;
    private String content;
    private MessageType messageType;
    private MessageStatus status;
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Entity -> DTO 변환
     */
    public static MessageResponse from(Message message) {
        return MessageResponse.builder()
                .id(message.getId())
                .channelId(message.getChannel().getId())
                .channelName(message.getChannel().getChannelName())
                .title(message.getTitle())
                .content(message.getContent())
                .messageType(message.getMessageType())
                .status(message.getStatus())
                .createdBy(message.getCreatedBy())
                .createdAt(message.getCreatedAt())
                .updatedAt(message.getUpdatedAt())
                .build();
    }
}