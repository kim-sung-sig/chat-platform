package com.example.chat.websocket.listener;

import com.example.chat.domain.service.register.ChatRoomRegister;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

/**
 * 미참여자 알림(푸시) 처리용 리스너 스켈레톤. 실제 디바이스 푸시 서비스(FCM/APNs)와 연동 필요.
 */
@Component
public class NotificationEventListener {

    private static final Logger logger = LoggerFactory.getLogger(NotificationEventListener.class);

    private final ObjectMapper objectMapper;
    private final SimpMessagingTemplate messagingTemplate;
    private final ChatRoomRegister chatRoomRegister;

    public NotificationEventListener(ObjectMapper objectMapper, SimpMessagingTemplate messagingTemplate, ChatRoomRegister chatRoomRegister) {
        this.objectMapper = objectMapper;
        this.messagingTemplate = messagingTemplate;
        this.chatRoomRegister = chatRoomRegister;
    }

    @RabbitListener(queues = "chat.notification")
    public void onNotification(String payload) {
        try {
            Map<?,?> map = objectMapper.readValue(payload, Map.class);
            String channelId = String.valueOf(map.get("channelId"));
            String body = String.valueOf(map.get("body"));

            // decide recipients - e.g. all channel members who are offline, here we just log
            Set<String> members = chatRoomRegister.listUsers(channelId);
            logger.debug("NotificationEventListener: received notification for channel={}, memberCount={}", channelId, members.size());

            // TODO: integrate with push gateway (FCM/APNs) or send websocket notif if connected

        } catch (Exception e) {
            logger.error("Failed to handle notification event", e);
        }
    }
}