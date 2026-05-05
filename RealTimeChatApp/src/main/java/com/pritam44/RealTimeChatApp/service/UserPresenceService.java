package com.pritam44.RealTimeChatApp.service;

import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.pritam44.RealTimeChatApp.dto.PresenceEvent;
@Service
public class UserPresenceService {

    private final SimpMessagingTemplate messagingTemplate;

    // ✅ ONLINE USERS
    private final Set<String> onlineUsers = ConcurrentHashMap.newKeySet();

    // ✅ LAST SEEN
    private final Map<String, Instant> lastSeen = new ConcurrentHashMap<>();

    public UserPresenceService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void markOnline(String username) {
        onlineUsers.add(username);

        messagingTemplate.convertAndSend(
            "/topic/presence",
            new PresenceEvent(username, "ONLINE", null)
        );
    }

    public void markOffline(String username) {
        onlineUsers.remove(username);

        Instant now = Instant.now();
        lastSeen.put(username, now);

        messagingTemplate.convertAndSend(
            "/topic/presence",
            new PresenceEvent(username, "OFFLINE", now)
        );
    }

    // ✅ IMPORTANT
    public Set<String> getOnlineUsers() {
        return onlineUsers;
    }
}
