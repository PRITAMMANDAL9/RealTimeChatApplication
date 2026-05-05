package com.pritam44.RealTimeChatApp.config;

import java.time.Instant;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import com.pritam44.RealTimeChatApp.model.ChatMessage;
import com.pritam44.RealTimeChatApp.service.UserPresenceService;

@Component
public class WebSocketEventListener {

    private final SimpMessageSendingOperations messagingTemplate;
    private final UserPresenceService presenceService;

    public WebSocketEventListener(
            SimpMessageSendingOperations messagingTemplate,
            UserPresenceService presenceService) {
        this.messagingTemplate = messagingTemplate;
        this.presenceService = presenceService;
    }

    /* ---------------- CONNECT ---------------- */
    @EventListener
    public void handleWebSocketConnect(SessionConnectedEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());

        if (accessor.getUser() == null) return;

        String username = accessor.getUser().getName();

        // ✅ MARK ONLINE
        presenceService.markOnline(username);

        System.out.println("🟢 USER ONLINE → " + username);
    }

    /* ---------------- DISCONNECT ---------------- */
    @EventListener
    public void handleWebSocketDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());

        if (accessor.getUser() == null) return;

        String username = accessor.getUser().getName();

        // ✅ MARK OFFLINE
        presenceService.markOffline(username);

        // Optional: public LEAVE message
        ChatMessage leaveMessage = new ChatMessage();
        leaveMessage.setType(ChatMessage.MessageType.LEAVE);
        leaveMessage.setSender(username);
        leaveMessage.setTimestamp(Instant.now());

        messagingTemplate.convertAndSend("/topic/message", leaveMessage);

        System.out.println("🔴 USER OFFLINE → " + username);
    }
}
