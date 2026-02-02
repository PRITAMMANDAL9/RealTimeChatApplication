package com.pritam44.RealTimeChatApp.model;

import java.time.Instant;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "chat_messages")
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private MessageType type;
    @Enumerated(EnumType.STRING)
    private MessageStatus status;
    private String sender;
    private String content;
    private Long roomId;
    private Instant timestamp;

    public enum MessageType {
        CHAT, JOIN, LEAVE, TYPING
    }
    
    public enum MessageStatus {
        SENT,
        DELIVERED,
        READ
    }
    

	public ChatMessage() {
		super();
	}

	public MessageStatus getStatus() {
		return status;
	}

	public void setStatus(MessageStatus status) {
		this.status = status;
	}

	public ChatMessage(Long id, MessageType type, MessageStatus status, String sender, String content, Long roomId,
			Instant timestamp) {
		super();
		this.id = id;
		this.type = type;
		this.status = status;
		this.sender = sender;
		this.content = content;
		this.roomId = roomId;
		this.timestamp = timestamp;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public MessageType getType() {
		return type;
	}

	public void setType(MessageType type) {
		this.type = type;
	}

	public String getSender() {
		return sender;
	}

	public void setSender(String sender) {
		this.sender = sender;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public Long getRoomId() {
		return roomId;
	}

	public void setRoomId(Long roomId) {
		this.roomId = roomId;
	}

	public Instant getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Instant timestamp) {
		this.timestamp = timestamp;
	}
    
}
