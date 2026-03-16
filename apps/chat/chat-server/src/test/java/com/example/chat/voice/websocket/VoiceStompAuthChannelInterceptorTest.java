package com.example.chat.voice.websocket;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("VoiceStompAuthChannelInterceptor")
class VoiceStompAuthChannelInterceptorTest {

    @Mock
    JwtDecoder jwtDecoder;

    @InjectMocks
    VoiceStompAuthChannelInterceptor interceptor;

    @Nested
    @DisplayName("preSend")
    class PreSend {

        @Nested
        @DisplayName("성공")
        class HappyPath {
            @Test
            @DisplayName("CONNECT 시 Authorization 헤더로 사용자 인증을 설정한다")
            void givenBearerToken_whenConnect_thenSetsUser() {
                // Given
                Jwt jwt = Jwt.withTokenValue("token")
                        .header("alg", "none")
                        .subject("user-1")
                        .claim("roles", List.of("ROLE_USER"))
                        .build();
                when(jwtDecoder.decode("token")).thenReturn(jwt);

                StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
                accessor.addNativeHeader("Authorization", "Bearer token");
                Message<byte[]> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

                // When
                Message<?> result = interceptor.preSend(message, mock(MessageChannel.class));

                // Then
                StompHeaderAccessor resultAccessor = StompHeaderAccessor.wrap(result);
                assertNotNull(resultAccessor.getUser());
                assertNotNull(resultAccessor.getUser().getName());
            }
        }

        @Nested
        @DisplayName("실패")
        class Failure {
            @Test
            @DisplayName("Authorization 헤더가 없으면 예외가 발생한다")
            void givenNoAuthHeader_whenConnect_thenThrows() {
                // Given
                StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
                Message<byte[]> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

                // When / Then
                assertThrows(IllegalStateException.class, () -> interceptor.preSend(message, mock(MessageChannel.class)));
            }
        }
    }
}
