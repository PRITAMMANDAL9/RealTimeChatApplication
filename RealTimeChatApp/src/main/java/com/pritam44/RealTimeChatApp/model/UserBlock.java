package com.pritam44.RealTimeChatApp.model;

import java.time.Instant;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
    name = "user_blocks",
    uniqueConstraints = @UniqueConstraint(
        columnNames = {"blocker_id", "blocked_id"}
    )
)
public class UserBlock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private User blocker;

    @ManyToOne(optional = false)
    private User blocked;

    private Instant createdAt = Instant.now();

    protected UserBlock() {}

    public UserBlock(User blocker, User blocked) {
        this.blocker = blocker;
        this.blocked = blocked;
    }

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public User getBlocker() {
		return blocker;
	}

	public void setBlocker(User blocker) {
		this.blocker = blocker;
	}

	public User getBlocked() {
		return blocked;
	}

	public void setBlocked(User blocked) {
		this.blocked = blocked;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Instant createdAt) {
		this.createdAt = createdAt;
	}

   
}

