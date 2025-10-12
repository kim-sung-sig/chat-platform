package com.example.chat.common.dto;

import java.time.OffsetDateTime;
import lombok.*;

@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class ChatMessage {
    private String roomId;
    private String senderId;
    private String content;
    private OffsetDateTime sentAt;
}