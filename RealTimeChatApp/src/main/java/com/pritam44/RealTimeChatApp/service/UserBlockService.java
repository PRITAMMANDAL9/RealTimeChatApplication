package com.pritam44.RealTimeChatApp.service;

import org.springframework.stereotype.Service;

import com.pritam44.RealTimeChatApp.model.User;
import com.pritam44.RealTimeChatApp.model.UserBlock;
import com.pritam44.RealTimeChatApp.repository.PrivateChatRoomRepository;
import com.pritam44.RealTimeChatApp.repository.UserBlockRepository;
import com.pritam44.RealTimeChatApp.repository.UserRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class UserBlockService {

    private final UserRepository userRepository;
    private final UserBlockRepository blockRepository;
    private final PrivateChatRoomRepository roomRepository;

    public UserBlockService(
            UserRepository userRepository,
            UserBlockRepository blockRepository,
            PrivateChatRoomRepository roomRepository) {
        this.userRepository = userRepository;
        this.blockRepository = blockRepository;
        this.roomRepository = roomRepository;
    }

    public void blockUser(String blockerUsername, String blockedUsername) {

        if (blockerUsername.equals(blockedUsername)) {
            throw new IllegalArgumentException("You cannot block yourself");
        }

        User blocker = userRepository.findByUsername(blockerUsername)
                .orElseThrow();

        User blocked = userRepository.findByUsername(blockedUsername)
                .orElseThrow();

        if (blockRepository.existsByBlockerAndBlocked(blocker, blocked)) {
            return;
        }

        blockRepository.save(new UserBlock(blocker, blocked));
    }


    public void unblockUser(String blockerUsername, String blockedUsername) {

        User blocker = userRepository.findByUsername(blockerUsername)
                .orElseThrow();

        User blocked = userRepository.findByUsername(blockedUsername)
                .orElseThrow();

        blockRepository.deleteByBlockerAndBlocked(blocker, blocked);
    }

    public boolean isBlocked(User sender, User receiver) {
        return blockRepository.existsByBlockerAndBlocked(sender, receiver)
            || blockRepository.existsByBlockerAndBlocked(receiver, sender);
    }
}
