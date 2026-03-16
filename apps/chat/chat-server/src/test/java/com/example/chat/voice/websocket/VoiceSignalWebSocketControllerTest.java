package com.example.chat.voice.websocket;

import com.example.chat.voice.application.service.VoiceSignalService;
import com.example.chat.voice.domain.model.VoiceSignal;
import com.example.chat.voice.domain.model.VoiceSignalType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("VoiceSignalWebSocketController")
class VoiceSignalWebSocketControllerTest {

    @Mock
    VoiceSignalService signalService;

    @Mock
    SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    VoiceSignalWebSocketController controller;

    @Nested
    @DisplayName("sendSignal")
    class SendSignal {

        @Nested
        @DisplayName("성공")
        class HappyPath {
            @Test
            @DisplayName("시그널 전송 시 대상 사용자에게 전달한다")
            void givenValidSignal_whenSend_thenDeliverToUser() {
                // Given
                Jwt jwt = Jwt.withTokenValue("token")
                        .header("alg", "none")
                        .subject("user-1")
                        .build();
                SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt, List.of()));

                VoiceSignal signal = new VoiceSignal(
                        "signal-1",
                        "ch-1",
                        "user-1",
                        "user-2",
                        VoiceSignalType.OFFER,
                        "payload",
                        Instant.now()
                );
                when(signalService.send("ch-1", "user-1", "user-2", VoiceSignalType.OFFER, "payload"))
                        .thenReturn(signal);

                VoiceSignalWebSocketController.VoiceSignalMessage message =
                        new VoiceSignalWebSocketController.VoiceSignalMessage("user-2", "OFFER", "payload");

                // When
                controller.sendSignal("ch-1", message);

                // Then
                verify(messagingTemplate).convertAndSendToUser(
                        eq("user-2"),
                        eq("/queue/voice/ch-1"),
                        any(VoiceSignalWebSocketController.VoiceSignalPayload.class)
                );
            }
        }
    }
}
