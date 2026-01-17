package com.pritam44.RealTimeChatApp.dto;

import java.time.Instant;

public class NotificationDto {

    private String type;      // CHAT_REQUEST, ACCEPTED, REJECTED
    private String fromUser;  // sender username
    private String message;   // readable message
    private Instant timestamp;

    public NotificationDto(String type, String fromUser, String message) {
        this.type = type;
        this.fromUser = fromUser;
        this.message = message;
        this.timestamp = Instant.now();
    }

    public String getType() {
        return type;
    }

    public String getFromUser() {
        return fromUser;
    }

    public String getMessage() {
        return message;
    }

    public Instant getTimestamp() {
        return timestamp;
    }
}