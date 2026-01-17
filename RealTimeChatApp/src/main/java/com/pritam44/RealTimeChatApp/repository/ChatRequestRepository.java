package com.pritam44.RealTimeChatApp.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pritam44.RealTimeChatApp.model.ChatRequest;
import com.pritam44.RealTimeChatApp.model.User;
import com.pritam44.RealTimeChatApp.model.ChatRequest.Status;

public interface ChatRequestRepository extends JpaRepository<ChatRequest, Long> {

    // Check if a request already exists between two users
    Optional<ChatRequest> findBySenderAndReceiver(User sender, User receiver);

    // Incoming requests for a user
    List<ChatRequest> findByReceiverAndStatus(User receiver, Status status);

    // Outgoing requests by a user
    List<ChatRequest> findBySenderAndStatus(User sender, Status status);
    
    boolean existsBySenderAndReceiverAndStatus(
            User sender,
            User receiver,
            Status status
    );
    
    Optional<ChatRequest> findByIdAndReceiverAndStatus(
            Long id,
            User receiver,
            Status status
    );
    
    List<ChatRequest> findByReceiver(User receiver);
    
    List<ChatRequest> findBySender(User sender);
   
}

