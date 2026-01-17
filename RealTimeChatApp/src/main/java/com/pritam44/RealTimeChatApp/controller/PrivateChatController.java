package com.pritam44.RealTimeChatApp.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pritam44.RealTimeChatApp.dto.PrivateChatRoomDto;
import com.pritam44.RealTimeChatApp.model.PrivateChatRoom;
import com.pritam44.RealTimeChatApp.model.User;
import com.pritam44.RealTimeChatApp.repository.PrivateChatRoomRepository;
import com.pritam44.RealTimeChatApp.repository.UserRepository;

@RestController
@RequestMapping("/api/private-chats")
public class PrivateChatController {

    private final PrivateChatRoomRepository roomRepo;
    private final UserRepository userRepo;

    public PrivateChatController(
            PrivateChatRoomRepository roomRepo,
            UserRepository userRepo) {
        this.roomRepo = roomRepo;
        this.userRepo = userRepo;
    }

    @GetMapping
    public List<PrivateChatRoomDto> myPrivateChats(Principal principal) {

        User me = userRepo.findByUsername(principal.getName())
                .orElseThrow(() -> new IllegalStateException("User not found"));

        return roomRepo.findAllRoomsOfUser(me)
                .stream()
                .map(room -> mapToDto(room, me))
                .toList();
    }

    private PrivateChatRoomDto mapToDto(PrivateChatRoom room, User me) {

        User otherUser =
                room.getUser1().equals(me)
                        ? room.getUser2()
                        : room.getUser1();

        return new PrivateChatRoomDto(
                room.getId(),
                otherUser.getUsername(),
                room.getCreatedAt()
        );
    }
}
