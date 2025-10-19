package com.example.chat.message.api.response;

import java.time.OffsetDateTime;

public class MessageResponse {
    private Long id;
    private String roomId;
    private Long senderId; // expose numeric senderId in API
    private String content;
    private OffsetDateTime sentAt;

    public MessageResponse() {}

    public MessageResponse(Long id, String roomId, Long senderId, String content, OffsetDateTime sentAt) {
        this.id = id; this.roomId = roomId; this.senderId = senderId; this.content = content; this.sentAt = sentAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }
    public Long getSenderId() { return senderId; }
    public void setSenderId(Long senderId) { this.senderId = senderId; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public OffsetDateTime getSentAt() { return sentAt; }
    public void setSentAt(OffsetDateTime sentAt) { this.sentAt = sentAt; }
}