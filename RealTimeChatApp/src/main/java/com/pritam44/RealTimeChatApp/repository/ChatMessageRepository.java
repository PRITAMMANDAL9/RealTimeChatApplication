package com.pritam44.RealTimeChatApp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.pritam44.RealTimeChatApp.model.ChatMessage;

import java.time.Instant;
import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
	@Query("""
		       SELECT m FROM ChatMessage m
		       WHERE m.roomId IS NULL
		       AND m.timestamp >= :since
		       ORDER BY m.timestamp ASC
		       """)
		List<ChatMessage> findPublicMessagesSince(Instant since);
	 List<ChatMessage> findByRoomIdOrderByTimestampAsc(Long roomId);
}

