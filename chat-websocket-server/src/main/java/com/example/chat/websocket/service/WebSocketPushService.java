package com.example.chat.websocket.service;

import com.example.chat.common.dto.ChatMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class WebSocketPushService {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketPushService.class);

    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketPushService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void pushToChannel(String channelId, ChatMessage message) {
        logger.debug("pushToChannel channel={} messageId={}", channelId, message.getId());
        messagingTemplate.convertAndSend("/topic/channel/" + channelId, message);
    }

    public void pushToUser(String userId, ChatMessage message) {
        logger.debug("pushToUser user={} messageId={}", userId, message.getId());
        messagingTemplate.convertAndSendToUser(userId, "/queue/messages", message);
    }
}