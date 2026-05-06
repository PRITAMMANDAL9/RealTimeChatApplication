package com.pritam44.RealTimeChatApp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.pritam44.RealTimeChatApp.model.ChatMessage;

import java.time.Instant;
import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    /* ================= PUBLIC CHAT ================= */

    List<ChatMessage> findTop30ByRoomIdIsNullAndTimestampAfterOrderByTimestampDesc(
            Instant since
    );

    List<ChatMessage> findTop30ByRoomIdIsNullAndTimestampBeforeAndTimestampAfterOrderByTimestampDesc(
            Instant before,
            Instant since
    );

    /* ================= PRIVATE CHAT ================= */

    List<ChatMessage> findTop20ByRoomIdOrderByTimestampDesc(Long roomId);

    List<ChatMessage> findTop20ByRoomIdAndTimestampBeforeOrderByTimestampDesc(
            Long roomId,
            Instant before
    );
}