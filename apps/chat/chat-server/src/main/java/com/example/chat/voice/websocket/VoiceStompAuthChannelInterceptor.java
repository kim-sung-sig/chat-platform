package com.example.chat.voice.websocket;

import com.example.chat.auth.core.model.AuthenticatedUser;
import com.example.chat.auth.jwt.config.JwtAuthenticationConverterSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class VoiceStompAuthChannelInterceptor implements ChannelInterceptor {

    private final JwtDecoder jwtDecoder;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authHeader = accessor.getFirstNativeHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                throw new IllegalStateException("Missing Authorization header");
            }
            String token = authHeader.substring("Bearer ".length());
            Jwt jwt = jwtDecoder.decode(token);

            AuthenticatedUser user = AuthenticatedUser.from(jwt);
            Authentication authentication = new JwtStompAuthenticationToken(jwt, user.userId());
            accessor.setUser(authentication);
        }
        return message;
    }

    private static class JwtStompAuthenticationToken extends AbstractAuthenticationToken {
        private final Jwt jwt;
        private final String name;

        JwtStompAuthenticationToken(Jwt jwt, String name) {
            super(new JwtAuthenticationConverterSupport().convert(jwt));
            this.jwt = jwt;
            this.name = name;
            setAuthenticated(true);
        }

        @Override
        public Object getCredentials() {
            return jwt.getTokenValue();
        }

        @Override
        public Object getPrincipal() {
            return jwt;
        }

        @Override
        public String getName() {
            return name;
        }
    }
}
