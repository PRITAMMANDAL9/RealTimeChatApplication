package com.pritam44.RealTimeChatApp.controller;

import java.security.Principal;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pritam44.RealTimeChatApp.model.User;
import com.pritam44.RealTimeChatApp.repository.UserRepository;
import com.pritam44.RealTimeChatApp.service.UserBlockService;

@RestController
@RequestMapping("/api/block")
public class UserBlockController {

    private final UserBlockService blockService;
    private final UserRepository  userRepository;

    public UserBlockController(UserBlockService blockService , UserRepository  userRepository) {
        this.blockService = blockService;
		this.userRepository = userRepository;
    }

    @PostMapping("/{username}")
    public ResponseEntity<?> blockUser(
            @PathVariable String username,
            Principal principal) {

        blockService.blockUser(principal.getName(), username);
        return ResponseEntity.ok("User blocked");
    }

    @DeleteMapping("/{username}")
    public ResponseEntity<?> unblockUser(
            @PathVariable String username,
            Principal principal) {

        blockService.unblockUser(principal.getName(), username);
        return ResponseEntity.ok("User unblocked");
    }
    
    @GetMapping("/status/{username}")
    public ResponseEntity<Boolean> isBlocked(
            @PathVariable String username,
            Principal principal) {

        User sender = userRepository.findByUsername(principal.getName()).orElseThrow();
        User receiver = userRepository.findByUsername(username).orElseThrow();

        return ResponseEntity.ok(blockService.isBlocked(sender, receiver));
    }

}

