package com.example.chat.voice.websocket;

import com.example.chat.auth.core.util.SecurityUtils;
import com.example.chat.common.core.exception.ChatErrorCode;
import com.example.chat.shared.exception.ChatException;
import com.example.chat.voice.application.service.VoiceSignalService;
import com.example.chat.voice.domain.model.VoiceSignal;
import com.example.chat.voice.domain.model.VoiceSignalType;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class VoiceSignalWebSocketController {

    private final VoiceSignalService signalService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/voice/{channelId}/signal")
    public void sendSignal(@DestinationVariable String channelId, VoiceSignalMessage message) {
        String fromUserId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new ChatException(ChatErrorCode.VOICE_SIGNAL_INVALID));

        VoiceSignalType type = parseType(message.type());
        VoiceSignal signal = signalService.send(channelId, fromUserId, message.toUserId(), type, message.payload());

        messagingTemplate.convertAndSendToUser(
                signal.getToUserId(),
                "/queue/voice/" + channelId,
                VoiceSignalPayload.from(signal)
        );
    }

    private VoiceSignalType parseType(String type) {
        try {
            return VoiceSignalType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new ChatException(ChatErrorCode.VOICE_SIGNAL_INVALID, new Object[]{type});
        }
    }

    public record VoiceSignalMessage(String toUserId, String type, String payload) {
    }

    public record VoiceSignalPayload(String id, String fromUserId, String toUserId, String type, String payload, String createdAt) {
        static VoiceSignalPayload from(VoiceSignal signal) {
            return new VoiceSignalPayload(
                    signal.getId(),
                    signal.getFromUserId(),
                    signal.getToUserId(),
                    signal.getType().name(),
                    signal.getPayload(),
                    signal.getCreatedAt().toString()
            );
        }
    }
}
