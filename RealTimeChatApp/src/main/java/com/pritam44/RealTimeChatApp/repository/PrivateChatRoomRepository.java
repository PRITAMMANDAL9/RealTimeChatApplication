package com.pritam44.RealTimeChatApp.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.pritam44.RealTimeChatApp.model.PrivateChatRoom;
import com.pritam44.RealTimeChatApp.model.User;

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
CHECK IF ROOM EXISTS BETWEEN USERS (FOR ACCEPT)
------------------------------------------------- */
@Query("""
SELECT COUNT(r) > 0
FROM PrivateChatRoom r
WHERE (r.user1.id = :u1 AND r.user2.id = :u2)
   OR (r.user1.id = :u2 AND r.user2.id = :u1)
""")
boolean existsRoomBetweenUsers(
    @Param("u1") Long u1,
    @Param("u2") Long u2
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

/* -------------------------------------------------
FIND ALL ROOMS OF USER
------------------------------------------------- */
@Query("""
SELECT r
FROM PrivateChatRoom r
WHERE r.user1 = :user OR r.user2 = :user
""")
List<PrivateChatRoom> findAllRoomsOfUser(
    @Param("user") User user
);

/* -------------------------------------------------
DELETE ROOM BETWEEN USERS
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
/* -------------------------------------------------
FIND OTHER USER IN ROOM (SAFE - NATIVE QUERY)
------------------------------------------------- */
@Query(
 value = """
     SELECT u.*
     FROM users u
     JOIN private_chat_room r
       ON (
             (r.user1_id = :userId AND r.user2_id = u.id)
          OR (r.user2_id = :userId AND r.user1_id = u.id)
       )
     WHERE r.id = :roomId
     """,
 nativeQuery = true
)

Optional<User> findRoomWithUser(
     @Param("roomId") Long roomId,
     @Param("userId") Long userId
);

}
