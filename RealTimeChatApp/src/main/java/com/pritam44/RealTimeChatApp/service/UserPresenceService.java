package com.pritam44.RealTimeChatApp.service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

@Service
public class UserPresenceService {

    private final Map<String, Instant> lastSeenMap = new ConcurrentHashMap<>();

    public void markOnline(String username) {
        lastSeenMap.remove(username);
    }

    public void markOffline(String username, Instant lastSeen) {
        lastSeenMap.put(username, lastSeen);
    }

    public boolean isOnline(String username) {
        return !lastSeenMap.containsKey(username);
    }

    public Instant getLastSeen(String username) {
        return lastSeenMap.get(username);
    }
}
