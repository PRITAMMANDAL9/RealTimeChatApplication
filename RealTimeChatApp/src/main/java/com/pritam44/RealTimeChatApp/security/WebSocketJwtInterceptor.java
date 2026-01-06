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

@Component
public class WebSocketJwtInterceptor implements ChannelInterceptor {

    private final JwtUtil jwtUtil;

    public WebSocketJwtInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {

        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        // Only validate JWT on CONNECT
        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {

            String authHeader = accessor.getFirstNativeHeader("Authorization");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                // ❌ No exception — just deny authentication
                return null;
            }

            try {
                String token = authHeader.substring(7);
                String username = jwtUtil.extractUsername(token);

                Authentication authentication =
                        new UsernamePasswordAuthenticationToken(
                                username,
                                null,
                                List.of(new SimpleGrantedAuthority("ROLE_USER"))
                        );

                accessor.setUser(authentication);

            } catch (Exception ex) {
                // Invalid token → block connection
                return null;
            }
        }

        return message;
    }
}
