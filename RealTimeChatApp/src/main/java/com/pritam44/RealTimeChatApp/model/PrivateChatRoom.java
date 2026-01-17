package com.pritam44.RealTimeChatApp.model;

import java.time.Instant;

import jakarta.persistence.*;


@Entity
public class PrivateChatRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private User user1;

    @ManyToOne(optional = false)
    private User user2;

    private Instant createdAt = Instant.now();

	public PrivateChatRoom(Long id, User user1, User user2, Instant createdAt) {
		super();
		this.id = id;
		this.user1 = user1;
		this.user2 = user2;
		this.createdAt = createdAt;
	}
	
	

	public PrivateChatRoom() {
		super();
	}



	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public User getUser1() {
		return user1;
	}

	public void setUser1(User user1) {
		this.user1 = user1;
	}

	public User getUser2() {
		return user2;
	}

	public void setUser2(User user2) {
		this.user2 = user2;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Instant createdAt) {
		this.createdAt = createdAt;
	}
    
    
    
}
