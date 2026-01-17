package com.pritam44.RealTimeChatApp.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pritam44.RealTimeChatApp.dto.NotificationDto;
import com.pritam44.RealTimeChatApp.model.ChatRequest;
import com.pritam44.RealTimeChatApp.model.User;
import com.pritam44.RealTimeChatApp.model.ChatRequest.Status;
import com.pritam44.RealTimeChatApp.repository.ChatRequestRepository;
import com.pritam44.RealTimeChatApp.repository.UserRepository;

@Service
public class ChatRequestService {

    private final ChatRequestRepository chatRequestRepository;
    private final NotificationService notificationService;
    private final UserRepository userRepository;

    public ChatRequestService(
            ChatRequestRepository chatRequestRepository,
            NotificationService notificationService) {

        this.chatRequestRepository = chatRequestRepository;
        this.notificationService = notificationService;
		this.userRepository = null;
    }

    @Transactional
    public void sendChatRequest(String fromUsername, String toUsername) {

        User sender = userRepository.findByUsername(fromUsername)
                .orElseThrow(() -> new IllegalArgumentException("Sender not found"));

        User receiver = userRepository.findByUsername(toUsername)
                .orElseThrow(() -> new IllegalArgumentException("Receiver not found"));

        // Prevent duplicate requests
        if (chatRequestRepository
                .existsBySenderAndReceiverAndStatus(sender, receiver, Status.PENDING)) {
            return;
        }

        ChatRequest request = new ChatRequest();
        request.setSender(sender);
        request.setReceiver(receiver);
        request.setStatus(Status.PENDING);

        chatRequestRepository.save(request);

        // Real-time notification
        notificationService.sendToUser(
                receiver.getUsername(),
                new NotificationDto(
                        "CHAT_REQUEST",
                        sender.getUsername(),
                        sender.getUsername() + " sent you a chat request"
                )
        );
    }
    
    @Transactional
    public void respondToRequest(Long requestId, String receiverUsername, boolean accept) {

        User receiver = userRepository.findByUsername(receiverUsername)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        ChatRequest request = chatRequestRepository
                .findByIdAndReceiverAndStatus(requestId, receiver, Status.PENDING)
                .orElseThrow(() -> new IllegalStateException("Invalid chat request"));

        User sender = request.getSender();

        if (accept) {
            request.setStatus(Status.ACCEPTED);

            notificationService.sendToUser(
                    sender.getUsername(),
                    new NotificationDto(
                            "CHAT_ACCEPTED",
                            receiver.getUsername(),
                            receiver.getUsername() + " accepted your chat request"
                    )
            );

        } else {
            request.setStatus(Status.REJECTED);

            notificationService.sendToUser(
                    sender.getUsername(),
                    new NotificationDto(
                            "CHAT_REJECTED",
                            receiver.getUsername(),
                            receiver.getUsername() + " rejected your chat request"
                    )
            );
        }

        chatRequestRepository.save(request);
    }


}
