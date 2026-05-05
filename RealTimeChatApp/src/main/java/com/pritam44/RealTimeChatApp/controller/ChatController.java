package com.pritam44.RealTimeChatApp.controller;

import java.security.Principal;
import java.time.Instant;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import com.pritam44.RealTimeChatApp.model.ChatMessage;
import com.pritam44.RealTimeChatApp.model.User;
import com.pritam44.RealTimeChatApp.repository.ChatMessageRepository;
import com.pritam44.RealTimeChatApp.repository.PrivateChatRoomRepository;
import com.pritam44.RealTimeChatApp.repository.UserRepository;
import com.pritam44.RealTimeChatApp.service.UserBlockService;
import com.pritam44.RealTimeChatApp.service.UserPresenceService;

@Controller
public class ChatController {

    private final ChatMessageRepository messageRepo;
    private final PrivateChatRoomRepository roomRepo;
    private final UserRepository userRepo;
    private final UserPresenceService presenceService;
    private final UserBlockService blockService;
    private final SimpMessagingTemplate messagingTemplate;

    public ChatController(
            ChatMessageRepository messageRepo,
            PrivateChatRoomRepository roomRepo,
            UserRepository userRepo,
            UserBlockService blockService,
            UserPresenceService presenceService,
            SimpMessagingTemplate messagingTemplate) {

        this.messageRepo = messageRepo;
        this.roomRepo = roomRepo;
        this.userRepo = userRepo;
        this.blockService = blockService; // ✅ ADD THIS
        this.presenceService = presenceService;
        this.messagingTemplate = messagingTemplate;
    }


    /* =================================================
       PUBLIC CHAT
       ================================================= */
    @MessageMapping("/sendMessage")
    @SendTo("/topic/message")
    public ChatMessage publicChat(
            ChatMessage message,
            Principal principal,
            SimpMessageHeaderAccessor accessor) {

        if (principal == null) {
            throw new IllegalStateException("Unauthenticated WS message");
        }

        String username = principal.getName();

        message.setSender(username);
        message.setTimestamp(Instant.now());

        // JOIN
        if (message.getType() == ChatMessage.MessageType.JOIN) {
            accessor.getSessionAttributes().put("username", username);
            presenceService.markOnline(username);

            return message;
        }

        // TYPING
        if (message.getType() == ChatMessage.MessageType.TYPING) {
            return message;
        }

        // CHAT
        if (message.getType() == ChatMessage.MessageType.CHAT
                && message.getContent() != null
                && !message.getContent().isBlank()) {

            messageRepo.save(message);
            return message;
        }

        return null;
    }

    /* =================================================
       PRIVATE CHAT (ROOM BASED)
       ================================================= */
    @MessageMapping("/chat/{roomId}")
    public void privateChat(
            @DestinationVariable Long roomId,
            ChatMessage message,
            Principal principal) {

        if (principal == null) return;

        if (message.getType() != ChatMessage.MessageType.CHAT
            && message.getType() != ChatMessage.MessageType.TYPING) {
            return;
        }

        User sender = userRepo.findByUsername(principal.getName())
                .orElseThrow();

        if (!roomRepo.isUserInRoom(roomId, sender)) return;

        var room = roomRepo.findById(roomId).orElseThrow();

        User receiver =
                room.getUser1().equals(sender)
                        ? room.getUser2()
                        : room.getUser1();

        if (blockService.isBlocked(sender, receiver)) {
            return;
        }

        message.setRoomId(roomId);
        message.setSender(sender.getUsername());
        message.setTimestamp(Instant.now());

        if (message.getType() == ChatMessage.MessageType.CHAT) {
            message.setStatus(ChatMessage.MessageStatus.SENT);
            messageRepo.save(message);
        }

        // 🔥 MANUAL BROKER SEND
        messagingTemplate.convertAndSend(
                "/topic/chat/" + roomId,
                message
        );
    }

    @MessageMapping("/chat/{roomId}/delivered")
    public void markDelivered(
            @DestinationVariable Long roomId,
            Long messageId,
            Principal principal) {

        if (messageId == null) return;

        ChatMessage msg = messageRepo.findById(messageId)
                .orElse(null);

        if (msg == null) return;

        if (msg.getStatus() == ChatMessage.MessageStatus.SENT) {
            msg.setStatus(ChatMessage.MessageStatus.DELIVERED);
            messageRepo.save(msg);
        }

    }

    @MessageMapping("/chat/{roomId}/read")
    @SendTo("/topic/chat/{roomId}")
    public ChatMessage markRead(
            @DestinationVariable Long roomId,
            Long messageId,
            Principal principal) {

        if (messageId == null) return null;

        ChatMessage msg = messageRepo.findById(messageId).orElse(null);
        if (msg == null) return null;

        // ⛔ STOP LOOP HERE
        if (msg.getStatus() == ChatMessage.MessageStatus.READ) {
            return null;
        }

        msg.setStatus(ChatMessage.MessageStatus.READ);
        messageRepo.save(msg);

        return msg;
    }



}
