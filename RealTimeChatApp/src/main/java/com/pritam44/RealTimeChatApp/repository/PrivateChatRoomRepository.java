package com.pritam44.RealTimeChatApp.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.pritam44.RealTimeChatApp.model.PrivateChatRoom;
import com.pritam44.RealTimeChatApp.model.User;

import jakarta.transaction.Transactional;

public interface PrivateChatRoomRepository
        extends JpaRepository<PrivateChatRoom, Long> {

    /* -------------------------------------------------
       FIND ROOM BETWEEN TWO USERS (ORDER SAFE)
       ------------------------------------------------- */
    @Query("""
        SELECT r
        FROM PrivateChatRoom r
        WHERE (r.user1 = :u1 AND r.user2 = :u2)
           OR (r.user1 = :u2 AND r.user2 = :u1)
    """)
    Optional<PrivateChatRoom> findRoomBetweenUsers(
            @Param("u1") User u1,
            @Param("u2") User u2
    );

    /* -------------------------------------------------
       CHECK USER MEMBERSHIP
       ------------------------------------------------- */
    @Query("""
            SELECT COUNT(r) > 0
            FROM PrivateChatRoom r
            WHERE r.id = :roomId
              AND (r.user1 = :user OR r.user2 = :user)
        """)
        boolean isUserInRoom(
                @Param("roomId") Long roomId,
                @Param("user") User user
        );

        @Query("""
            SELECT
                CASE
                    WHEN r.user1 = :user THEN r.user2
                    ELSE r.user1
                END
            FROM PrivateChatRoom r
            WHERE r.id = :roomId
        """)
        Optional<User> findOtherUserInRoom(
                @Param("roomId") Long roomId,
                @Param("user") User user
        );
    /* -------------------------------------------------
       DELETE ROOM BETWEEN TWO USERS
       ------------------------------------------------- */
    @Modifying
    @Transactional
    @Query("""
        DELETE FROM PrivateChatRoom r
        WHERE (r.user1 = :u1 AND r.user2 = :u2)
           OR (r.user1 = :u2 AND r.user2 = :u1)
    """)
    void deleteRoomBetweenUsers(
            @Param("u1") User u1,
            @Param("u2") User u2
    );
    
    @Query("""
            SELECT r FROM PrivateChatRoom r
            WHERE r.user1 = :user OR r.user2 = :user
        """)
        List<PrivateChatRoom> findAllRoomsOfUser(@Param("user") User user);
}
