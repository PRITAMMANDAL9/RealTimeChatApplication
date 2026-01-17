package com.pritam44.RealTimeChatApp.dto;

import java.time.Instant;

public class IncomingChatRequestResponseDto {

    private Long requestId;
    private String senderUsername;
    private Instant createdAt;

    public IncomingChatRequestResponseDto(Long requestId, String senderUsername, Instant createdAt) {
        this.requestId = requestId;
        this.senderUsername = senderUsername;
        this.createdAt = createdAt;
    }

    public Long getRequestId() {
        return requestId;
    }

    public String getSenderUsername() {
        return senderUsername;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
