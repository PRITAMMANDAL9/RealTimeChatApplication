package com.pritam44.RealTimeChatApp.config;

import java.security.Principal;
import java.time.Instant;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Component;
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

    /* -------------------------------------------------
       DISCONNECT EVENT
       ------------------------------------------------- */
    @EventListener
    public void handleWebSocketDisconnect(SessionDisconnectEvent event) {

        Principal principal = event.getUser();
        if (principal == null) return;

        String username = principal.getName();

        // ✅ update presence
        presenceService.markOffline(username, Instant.now());

        // ✅ broadcast LEAVE message (public chat)
        ChatMessage leaveMessage = new ChatMessage();
        leaveMessage.setType(ChatMessage.MessageType.LEAVE);
        leaveMessage.setSender(username);
        leaveMessage.setTimestamp(Instant.now());

        messagingTemplate.convertAndSend(
                "/topic/message",
                leaveMessage
        );
    }
}
