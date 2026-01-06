package com.pritam44.RealTimeChatApp.config;

import java.util.Map;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import com.pritam44.RealTimeChatApp.model.ChatMessage;

@Component
public class WebSocketEventListener {

    private final SimpMessageSendingOperations messagingTemplate;

    public WebSocketEventListener(SimpMessageSendingOperations messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @EventListener
    public void handleWebSocketDisconnect(SessionDisconnectEvent event) {

        Map<String, Object> sessionAttributes =
                event.getMessage().getHeaders()
                        .get("simpSessionAttributes", Map.class);

        if (sessionAttributes == null) return;

        String username = (String) sessionAttributes.get("username");

        if (username != null) {
            ChatMessage leaveMessage = new ChatMessage();
            leaveMessage.setType(ChatMessage.MessageType.LEAVE);
            leaveMessage.setSender(username);

            messagingTemplate.convertAndSend("/topic/message", leaveMessage);
        }
    }
}