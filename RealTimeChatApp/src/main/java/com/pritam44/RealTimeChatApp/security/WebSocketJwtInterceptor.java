package com.pritam44.RealTimeChatApp.security;

import java.util.List;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import com.pritam44.RealTimeChatApp.service.JwtService;

@Component
public class WebSocketJwtInterceptor implements ChannelInterceptor {

    private final JwtService jwtService;

    public WebSocketJwtInterceptor(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {

        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        // ✅ Validate JWT only during CONNECT
        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {

            String authHeader = accessor.getFirstNativeHeader("Authorization");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                // ❌ No token → reject connection
                return null;
            }

            String token = authHeader.substring(7);

            try {
                String username = jwtService.extractUsername(token);

                if (username == null || !jwtService.isTokenValid(token, username)) {
                    return null;
                }

                Authentication authentication =
                        new UsernamePasswordAuthenticationToken(
                                username,
                                null,
                                List.of(new SimpleGrantedAuthority("ROLE_USER"))
                        );

                // ✅ This is what makes `Principal` available in controllers
                accessor.setUser(authentication);

            } catch (Exception ex) {
                // ❌ Invalid token → block WebSocket connection
                return null;
            }
        }

        return message;
    }
}
