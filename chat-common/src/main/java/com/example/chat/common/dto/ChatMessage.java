package com.example.chat.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class ChatMessage {
    private Long id; // DB PK - optional in DTO
    private String roomId;
    private UserId senderId; // changed from String to UserId
    private String content;
    private OffsetDateTime sentAt;
}