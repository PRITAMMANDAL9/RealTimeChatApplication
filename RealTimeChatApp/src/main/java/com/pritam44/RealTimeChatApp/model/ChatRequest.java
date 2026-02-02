package com.pritam44.RealTimeChatApp.model;

import java.time.Instant;

import jakarta.persistence.*;

@Entity
@Table(
    name = "chat_requests",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"sender_id", "receiver_id"})
    }
)
public class ChatRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "sender_id")
    private User sender;

    @ManyToOne(optional = false)
    @JoinColumn(name = "receiver_id")
    private User receiver;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    public enum Status {
        PENDING,
        ACCEPTED,
        REJECTED
    }

    public ChatRequest() {}

    @PrePersist
    void onCreate() {
        this.createdAt = Instant.now();
        if (this.status == null) {
            this.status = Status.PENDING;
        }
    }

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public User getSender() {
		return sender;
	}

	public void setSender(User sender) {
		this.sender = sender;
	}

	public User getReceiver() {
		return receiver;
	}

	public void setReceiver(User receiver) {
		this.receiver = receiver;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Instant createdAt) {
		this.createdAt = createdAt;
	}

    
}
