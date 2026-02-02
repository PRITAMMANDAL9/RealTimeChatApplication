package com.pritam44.RealTimeChatApp.model;

import java.time.Instant;

import jakarta.persistence.*;

@Entity
@Table(
    name = "private_chat_room",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user1_id", "user2_id"})
    }
)
public class PrivateChatRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user1_id")
    private User user1;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user2_id")
    private User user2;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    /* -------------------------------------------------
       REQUIRED BY JPA
       ------------------------------------------------- */
    protected PrivateChatRoom() {
    }

    /* -------------------------------------------------
       CONSTRUCTOR USED BY APPLICATION CODE
       ------------------------------------------------- */
    public PrivateChatRoom(User user1, User user2) {
        this.user1 = user1;
        this.user2 = user2;
        this.createdAt = Instant.now();
    }

    /* -------------------------------------------------
       GETTERS / SETTERS
       ------------------------------------------------- */
    public Long getId() {
        return id;
    }

    public User getUser1() {
        return user1;
    }

    public User getUser2() {
        return user2;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
