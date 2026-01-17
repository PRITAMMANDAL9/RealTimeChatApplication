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
import com.pritam44.RealTimeChatApp.model.User;
import com.pritam44.RealTimeChatApp.repository.ChatMessageRepository;
import com.pritam44.RealTimeChatApp.repository.PrivateChatRoomRepository;
import com.pritam44.RealTimeChatApp.repository.UserRepository;
import com.pritam44.RealTimeChatApp.service.UserBlockService;

@Controller
public class ChatController {

    private final ChatMessageRepository messageRepo;
    private final PrivateChatRoomRepository roomRepo;
    private final UserRepository userRepository;
    private final UserBlockService blockService;

    public ChatController(
            ChatMessageRepository messageRepo,
            PrivateChatRoomRepository roomRepo,
            UserRepository userRepository,
            UserBlockService blockService) {

        this.messageRepo = messageRepo;
        this.roomRepo = roomRepo;
        this.userRepository = userRepository;
        this.blockService = blockService;
    }

    /* -------------------------------------------------
       PUBLIC CHAT (GLOBAL)
       ------------------------------------------------- */
    @MessageMapping("/sendMessage")
    @SendTo("/topic/message")
    public ChatMessage sendMessage(
            ChatMessage message,
            Principal principal,
            SimpMessageHeaderAccessor accessor) {

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
            messageRepo.save(message);
        }

        return message;
    }

    /* -------------------------------------------------
       PRIVATE CHAT (ROOM BASED)
       ------------------------------------------------- */
    @MessageMapping("/chat/{roomId}")
    @SendTo("/topic/chat/{roomId}")
    public ChatMessage sendPrivateMessage(
            @DestinationVariable String roomId,
            ChatMessage message,
            Principal principal) {

        if (principal == null) {
            throw new IllegalStateException("Unauthenticated private message");
        }

        Long roomIdLong;
        try {
            roomIdLong = Long.parseLong(roomId);
        } catch (NumberFormatException ex) {
            throw new IllegalStateException("Invalid room ID");
        }

        User sender = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new IllegalStateException("User not found"));

        // ✅ Check room membership
        if (!roomRepo.isUserInRoom(roomIdLong, sender)) {
            throw new IllegalStateException("You are not allowed in this room");
        }

        // ✅ Block check (VERY IMPORTANT)
        User receiver = roomRepo.findOtherUserInRoom(roomIdLong, sender)
                .orElseThrow(() -> new IllegalStateException("Receiver not found"));

        if (blockService.isBlocked(sender, receiver)) {
            throw new IllegalStateException("Messaging blocked between users");
        }

        message.setRoomId(roomId); // still String (as per your model)
        message.setSender(sender.getUsername());
        message.setTimestamp(Instant.now());

        messageRepo.save(message);

        return message;
    }

    /* -------------------------------------------------
       PAGE ROUTES
       ------------------------------------------------- */
    @GetMapping("/chat")
    public String chat() {
        return "chat";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/signup")
    public String signupPage() {
        return "signup";
    }
    @GetMapping("/requests")
    public String requests() {
        return "requests";
    }
}
