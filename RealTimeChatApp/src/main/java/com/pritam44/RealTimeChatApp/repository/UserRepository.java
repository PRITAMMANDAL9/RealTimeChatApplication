package com.pritam44.RealTimeChatApp.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pritam44.RealTimeChatApp.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username); 
}
