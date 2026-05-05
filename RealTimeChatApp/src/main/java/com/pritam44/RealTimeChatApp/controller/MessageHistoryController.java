package com.pritam44.RealTimeChatApp.controller;

import java.security.Principal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.pritam44.RealTimeChatApp.model.ChatMessage;
import com.pritam44.RealTimeChatApp.model.User;
import com.pritam44.RealTimeChatApp.repository.ChatMessageRepository;
import com.pritam44.RealTimeChatApp.repository.PrivateChatRoomRepository;
import com.pritam44.RealTimeChatApp.repository.UserRepository;

@RestController
@RequestMapping("/api/messages")
public class MessageHistoryController {

    private final ChatMessageRepository messageRepo;
    private final PrivateChatRoomRepository roomRepo;
    private final UserRepository userRepo;

    public MessageHistoryController(
            ChatMessageRepository messageRepo,
            PrivateChatRoomRepository roomRepo,
            UserRepository userRepo) {
        this.messageRepo = messageRepo;
        this.roomRepo = roomRepo;
        this.userRepo = userRepo;
    }

    @GetMapping("/private/{roomId}")
    public List<ChatMessage> getPrivateMessages(
            @PathVariable Long roomId,
            Principal principal) {

        User me = userRepo.findByUsername(principal.getName())
                .orElseThrow(() ->
                    new ResponseStatusException(HttpStatus.UNAUTHORIZED)
                );

        boolean isMember = roomRepo.isUserInRoom(roomId, me);

        if (!isMember) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Not a member of this room"
            );
        }

        return messageRepo
                .findByRoomIdOrderByTimestampAsc(roomId);
    }
    
    @RestController
    @RequestMapping("/api/messages")
    public class PublicMessageHistoryController {

        private final ChatMessageRepository messageRepo;

        public PublicMessageHistoryController(ChatMessageRepository messageRepo) {
            this.messageRepo = messageRepo;
        }

        @GetMapping("/public")
        public List<ChatMessage> getLastOneDayPublicMessages() {

            Instant since = Instant.now().minus(1, ChronoUnit.DAYS);

            return messageRepo
                    .findPublicMessagesSince(since);
        }
    }

}

