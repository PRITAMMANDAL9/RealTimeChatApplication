package com.pritam44.RealTimeChatApp.dto;

import java.time.Instant;

public record PresenceEvent(
        String user,
        String status,
        Instant lastSeen
) {}
