package com.pritam44.RealTimeChatApp.dto;

import com.pritam44.RealTimeChatApp.model.ChatRequest;

public class ChatRequestDto {

    private Long id;
    private String fromUser;
    private String toUser;
    private String status;

    public ChatRequestDto(Long id, String fromUser, String toUser, String status) {
        this.id = id;
        this.fromUser = fromUser;
        this.toUser = toUser;
        this.status = status;
    }

    /* =========================
       STATIC MAPPER (IMPORTANT)
       ========================= */
    public static ChatRequestDto from(ChatRequest r) {
        return new ChatRequestDto(
                r.getId(),
                r.getSender().getUsername(),
                r.getReceiver().getUsername(),
                r.getStatus().name()
        );
    }

    /* ---------- GETTERS ---------- */
    public Long getId() { return id; }
    public String getFromUser() { return fromUser; }
    public String getToUser() { return toUser; }
    public String getStatus() { return status; }

    /* ---------- SETTERS ---------- */
    public void setId(Long id) {
        this.id = id;
    }

    public void setFromUser(String fromUser) {
        this.fromUser = fromUser;
    }

    public void setToUser(String toUser) {
        this.toUser = toUser;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
