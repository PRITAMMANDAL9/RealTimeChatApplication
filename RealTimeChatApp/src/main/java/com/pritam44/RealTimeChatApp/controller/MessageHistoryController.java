package com.pritam44.RealTimeChatApp.controller;

import java.security.Principal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
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

    /* =================================================
       PRIVATE CHAT - LATEST
    ================================================= */

    @GetMapping("/private/{roomId}")
    public List<ChatMessage> getLatestPrivateMessages(
            @PathVariable Long roomId,
            Principal principal) {

        User me = userRepo.findByUsername(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        if (!roomRepo.isUserInRoom(roomId, me)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        return messageRepo.findTop20ByRoomIdOrderByTimestampDesc(roomId);
    }

    /* =================================================
       PRIVATE CHAT - OLDER
    ================================================= */

    @GetMapping("/private/{roomId}/before")
    public List<ChatMessage> getOlderMessages(
            @PathVariable Long roomId,
            @RequestParam Instant before,
            Principal principal) {

        User me = userRepo.findByUsername(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        if (!roomRepo.isUserInRoom(roomId, me)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        return messageRepo
                .findTop20ByRoomIdAndTimestampBeforeOrderByTimestampDesc(roomId, before);
    }

    /* =================================================
       PUBLIC CHAT - LATEST (LAST 1 DAY)
    ================================================= */
    @GetMapping("/public")
    public List<ChatMessage> getLatestPublicMessages() {

        Instant since = Instant.now().minus(1, ChronoUnit.DAYS);

        List<ChatMessage> result =
                messageRepo.findTop30ByRoomIdIsNullAndTimestampAfterOrderByTimestampDesc(since);

        System.out.println("SINCE: " + since);
        System.out.println("RESULT SIZE: " + result.size());

        return result;
    }

    @GetMapping("/public/before")
    public List<ChatMessage> getOlderPublicMessages(
            @RequestParam Instant before) {

        Instant since = Instant.now().minus(1, ChronoUnit.DAYS);

        return messageRepo
                .findTop30ByRoomIdIsNullAndTimestampBeforeAndTimestampAfterOrderByTimestampDesc(
                        before,
                        since
                );
    }
    }