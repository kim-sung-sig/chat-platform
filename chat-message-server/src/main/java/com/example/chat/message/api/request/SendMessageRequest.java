package com.example.chat.message.api.request;

import java.time.OffsetDateTime;

public class SendMessageRequest {
    private Long senderId; // numeric id from client
    private String content;
    private OffsetDateTime sentAt;

    public Long getSenderId() { return senderId; }
    public void setSenderId(Long senderId) { this.senderId = senderId; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public OffsetDateTime getSentAt() { return sentAt; }
    public void setSentAt(OffsetDateTime sentAt) { this.sentAt = sentAt; }
}