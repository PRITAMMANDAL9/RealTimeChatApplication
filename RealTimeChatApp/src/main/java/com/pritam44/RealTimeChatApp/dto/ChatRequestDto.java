package com.pritam44.RealTimeChatApp.dto;

import com.pritam44.RealTimeChatApp.model.ChatRequest;

public class ChatRequestDto {

    private Long id;
    private String senderUsername;
    private String receiverUsername;
    private String status;

    public ChatRequestDto(Long id, String senderUsername, String receiverUsername, String status) {
        this.id = id;
        this.senderUsername = senderUsername;
        this.receiverUsername = receiverUsername;
        this.status = status;
    }

    public static ChatRequestDto from(ChatRequest r) {
        return new ChatRequestDto(
                r.getId(),
                r.getSender().getUsername(),
                r.getReceiver().getUsername(),
                r.getStatus().name()
        );
    }

    public Long getId() { return id; }
    public String getSenderUsername() { return senderUsername; }
    public String getReceiverUsername() { return receiverUsername; }
    public String getStatus() { return status; }
}
