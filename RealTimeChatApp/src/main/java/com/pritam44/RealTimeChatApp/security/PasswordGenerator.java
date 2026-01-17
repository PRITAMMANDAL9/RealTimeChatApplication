package com.pritam44.RealTimeChatApp.security;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.pritam44.RealTimeChatApp.model.User;
import com.pritam44.RealTimeChatApp.repository.UserRepository;

@Configuration   
public class PasswordGenerator {

    @Bean
    CommandLineRunner initUser(
            UserRepository repo,
            PasswordEncoder encoder) {

        return args -> {
            repo.findByUsername("indra").ifPresentOrElse(
                u -> System.out.println("User already exists"),
                () -> {
                    User user = new User();
                    user.setUsername("indra");
                    user.setPassword(encoder.encode("indra@"));
                    repo.save(user);
                    System.out.println("User indra created");
                }
            );
        };
    }
}

