package com.pritam44.RealTimeChatApp.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pritam44.RealTimeChatApp.dto.ChatRequestDto;
import com.pritam44.RealTimeChatApp.model.ChatRequest;
import com.pritam44.RealTimeChatApp.model.ChatRequest.Status;
import com.pritam44.RealTimeChatApp.model.User;
import com.pritam44.RealTimeChatApp.repository.ChatRequestRepository;
import com.pritam44.RealTimeChatApp.repository.PrivateChatRoomRepository;
import com.pritam44.RealTimeChatApp.repository.UserRepository;

@RestController
@RequestMapping("/api/chat-requests")
public class ChatRequestController {

    private final ChatRequestRepository requestRepo;
    private final UserRepository userRepo;
    private final PrivateChatRoomRepository roomRepo;

    public ChatRequestController(
            ChatRequestRepository requestRepo,
            UserRepository userRepo,
            PrivateChatRoomRepository roomRepo) {

        this.requestRepo = requestRepo;
        this.userRepo = userRepo;
        this.roomRepo = roomRepo;
    }
    @PostMapping("/{username}")
    public ResponseEntity<?> sendRequest(
            @PathVariable String username,
            Principal principal) {

        if (principal == null) {
            return ResponseEntity.status(401).build();
        }

        User sender = userRepo.findByUsername(principal.getName()).orElseThrow();
        User receiver = userRepo.findByUsername(username).orElseThrow();

        if (requestRepo.existsBySenderAndReceiverAndStatus(
                sender, receiver, Status.PENDING)) {
            return ResponseEntity.ok("Already sent");
        }

        ChatRequest req = new ChatRequest();
        req.setSender(sender);
        req.setReceiver(receiver);
        req.setStatus(Status.PENDING);

        requestRepo.save(req);
        return ResponseEntity.ok("Request sent");
    }

    @GetMapping("/incoming")
    public ResponseEntity<List<ChatRequestDto>> incoming(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }

        User me = userRepo.findByUsername(principal.getName()).orElseThrow();

        return ResponseEntity.ok(
            requestRepo.findByReceiver(me).stream()
                .map(ChatRequestDto::from)
                .toList()
        );
    }

    @GetMapping("/sent")
    public ResponseEntity<List<ChatRequestDto>> sent(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }

        User me = userRepo.findByUsername(principal.getName()).orElseThrow();

        return ResponseEntity.ok(
            requestRepo.findBySender(me).stream()
                .map(ChatRequestDto::from)
                .toList()
        );
    }
}