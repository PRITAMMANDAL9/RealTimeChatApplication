package com.pritam44.RealTimeChatApp.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.pritam44.RealTimeChatApp.dto.ChatRequestDto;
import com.pritam44.RealTimeChatApp.model.ChatRequest;
import com.pritam44.RealTimeChatApp.model.ChatRequest.Status;
import com.pritam44.RealTimeChatApp.model.PrivateChatRoom;
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

    /* =================================================
       SEND REQUEST
       ================================================= */
    @PostMapping("/{username}")
    public ResponseEntity<?> sendRequest(
            @PathVariable String username,
            Principal principal) {

        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String senderUsername = principal.getName();

        if (senderUsername.equals(username)) {
            return ResponseEntity.badRequest()
                    .body("You cannot send a request to yourself");
        }

        User sender = userRepo.findByUsername(senderUsername)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        User receiver = userRepo.findByUsername(username)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND, "User not found"));

        // prevent duplicate pending request
        boolean exists = requestRepo
                .existsBySenderAndReceiverAndStatus(
                        sender, receiver, Status.PENDING);

        if (exists) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Request already sent");
        }

        ChatRequest request = new ChatRequest();
        request.setSender(sender);
        request.setReceiver(receiver);
        // status + createdAt handled by @PrePersist

        requestRepo.save(request);

        return ResponseEntity.ok().build();
    }

    /* =================================================
       INCOMING REQUESTS (ONLY PENDING)
       ================================================= */
    @GetMapping("/incoming")
    public ResponseEntity<List<ChatRequestDto>> incoming(
            Principal principal) {

        User me = userRepo.findByUsername(principal.getName())
                .orElseThrow();

        return ResponseEntity.ok(
                requestRepo
                        .findByReceiverAndStatus(me, Status.PENDING)
                        .stream()
                        .map(ChatRequestDto::from)
                        .toList()
        );
    }

    /* =================================================
       SENT REQUESTS
       ================================================= */
    @GetMapping("/sent")
    public ResponseEntity<List<ChatRequestDto>> sent(
            Principal principal) {

        User me = userRepo.findByUsername(principal.getName())
                .orElseThrow();

        return ResponseEntity.ok(
                requestRepo.findBySender(me)
                        .stream()
                        .map(ChatRequestDto::from)
                        .toList()
        );
    }

    /* =================================================
       ACCEPT REQUEST
       ================================================= */
    @PostMapping("/{id}/accept")
    @Transactional
    public ResponseEntity<?> acceptRequest(
            @PathVariable Long id,
            Principal principal) {

        ChatRequest request = requestRepo.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND));

        if (!request.getReceiver().getUsername()
                .equals(principal.getName())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        if (request.getStatus() != Status.PENDING) {
            return ResponseEntity.badRequest().build();
        }

        User u1 = request.getSender();
        User u2 = request.getReceiver();

        // ensure only ONE private room
        if (!roomRepo.existsRoomBetweenUsers(u1.getId(), u2.getId())) {
            roomRepo.save(new PrivateChatRoom(u1, u2));
        }

        // remove request so it disappears immediately
        requestRepo.delete(request);

        return ResponseEntity.ok().build();
    }

    /* =================================================
       REJECT REQUEST
       ================================================= */
    @PostMapping("/{id}/reject")
    @Transactional
    public ResponseEntity<?> reject(
            @PathVariable Long id,
            Principal principal) {

        ChatRequest request = requestRepo.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND));

        if (!request.getReceiver().getUsername()
                .equals(principal.getName())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        requestRepo.delete(request);

        return ResponseEntity.ok().build();
    }
}
