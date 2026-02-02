package com.pritam44.RealTimeChatApp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.pritam44.RealTimeChatApp.model.ChatMessage;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
	 List<ChatMessage> findByRoomIdOrderByTimestampAsc(Long roomId);
}

