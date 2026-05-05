package com.pritam44.RealTimeChatApp.controller;

import java.util.Set;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pritam44.RealTimeChatApp.service.UserPresenceService;

@RestController
@RequestMapping("/api/presence")
public class PresenceController {

    private final UserPresenceService presenceService;

    public PresenceController(UserPresenceService presenceService) {
        this.presenceService = presenceService;
    }

    @GetMapping("/online")
    public Set<String> onlineUsers() {
        return presenceService.getOnlineUsers();
    }
}

