package com.pritam44.RealTimeChatApp.controller;

import java.security.Principal;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pritam44.RealTimeChatApp.service.UserBlockService;

@RestController
@RequestMapping("/api/block")
public class UserBlockController {

    private final UserBlockService blockService;

    public UserBlockController(UserBlockService blockService) {
        this.blockService = blockService;
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
}

