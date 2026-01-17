package com.pritam44.RealTimeChatApp.dto;

import java.time.Instant;

public class PrivateChatRoomDto {

    private Long roomId;
    private String username;
    private Instant createdAt;

    public PrivateChatRoomDto(Long roomId, String username, Instant createdAt) {
        this.roomId = roomId;
        this.username = username;
        this.createdAt = createdAt;
    }

    public Long getRoomId() {
        return roomId;
    }

    public String getUsername() {
        return username;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
