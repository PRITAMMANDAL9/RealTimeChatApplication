package com.pritam44.RealTimeChatApp.controller;

import java.security.Principal;
import java.time.Instant;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import com.pritam44.RealTimeChatApp.model.ChatMessage;
import com.pritam44.RealTimeChatApp.repository.ChatMessageRepository;

@Controller
public class ChatController {

    private final ChatMessageRepository repository;

    public ChatController(ChatMessageRepository repository) {
        this.repository = repository;
    }


    @MessageMapping("/sendMessage")
    @SendTo("/topic/message")
    public ChatMessage sendMessage(
            ChatMessage message,
            Principal principal,
            SimpMessageHeaderAccessor accessor) {

        // 🔐 Safety check (VERY IMPORTANT)
        if (principal == null) {
            throw new IllegalStateException("Unauthenticated WebSocket message");
        }

        String username = principal.getName();

        if (message.getType() == ChatMessage.MessageType.JOIN) {
            accessor.getSessionAttributes().put("username", username);
        }

        message.setSender(username);
        message.setTimestamp(Instant.now());

        if (message.getType() == ChatMessage.MessageType.CHAT) {
            repository.save(message);
        }

        return message;
    }


    @MessageMapping("/chat/{roomId}")
    @SendTo("/topic/chat/{roomId}")
    public ChatMessage sendPrivateMessage(
            @DestinationVariable String roomId,
            ChatMessage message,
            Principal principal) {

        if (principal == null) {
            throw new IllegalStateException("Unauthenticated private message");
        }

        message.setRoomId(roomId);
        message.setSender(principal.getName());
        message.setTimestamp(Instant.now());

        if (message.getType() == ChatMessage.MessageType.CHAT) {
            repository.save(message);
        }

        return message;
    }


  
    @GetMapping("/chat")
    public String chat() {
        return "chat";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

}
