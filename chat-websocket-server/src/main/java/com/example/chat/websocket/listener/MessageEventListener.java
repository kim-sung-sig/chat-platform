package com.example.chat.websocket.listener;

import com.example.chat.common.dto.ChatMessage;
import com.example.chat.common.dto.UserId;
import com.example.chat.domain.service.register.ChatRoomRegister;
import com.example.chat.websocket.application.MessageBroadcaster;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

/**
 * RabbitMQ로부터 MESSAGE_CREATED 같은 이벤트를 수신하면 해당 채널 참여자에게 STOMP로 전송.
 * Listener delegates channel broadcast to MessageBroadcaster and uses domain ChatRoomRegister to obtain participants.
 */
@Component
public class MessageEventListener {

    private static final Logger logger = LoggerFactory.getLogger(MessageEventListener.class);

    private final ObjectMapper objectMapper;
    private final SimpMessagingTemplate messagingTemplate;
    private final ChatRoomRegister chatRoomRegister;
    private final MessageBroadcaster broadcaster;

    public MessageEventListener(ObjectMapper objectMapper, SimpMessagingTemplate messagingTemplate,
                                ChatRoomRegister chatRoomRegister, MessageBroadcaster broadcaster) {
        this.objectMapper = objectMapper;
        this.messagingTemplate = messagingTemplate;
        this.chatRoomRegister = chatRoomRegister;
        this.broadcaster = broadcaster;
    }

    @RabbitListener(queues = "chat.message.created")
    public void onMessageCreated(String payload) {
        try {
            Map<?,?> map = objectMapper.readValue(payload, Map.class);
            String channelId = String.valueOf(map.get("channelId"));

            ChatMessage msg = new ChatMessage();
            msg.setId(map.get("messageId") == null ? null : Long.valueOf(String.valueOf(map.get("messageId"))));
            msg.setRoomId(channelId);
            // convert senderId numeric to UserId
            Object s = map.get("senderId");
            if (s != null) {
                Long sid = Long.valueOf(String.valueOf(s));
                msg.setSenderId(UserId.of(sid));
            }
            msg.setContent(String.valueOf(map.get("content")));
            // TODO: parse createdAt into OffsetDateTime if present

            // broadcast to channel topic via broadcaster
            broadcaster.broadcast(channelId, msg);

            // push to individual users (if needed)
            Set<String> users = chatRoomRegister.listUsers(channelId);
            if (users != null && !users.isEmpty()) {
                logger.debug("MessageEventListener: pushing message to {} users for channel={}", users.size(), channelId);
                for (String userId : users) {
                    messagingTemplate.convertAndSendToUser(userId, "/queue/messages", msg);
                }
            }

        } catch (Exception e) {
            logger.error("Failed to handle message.created event", e);
        }
    }
}