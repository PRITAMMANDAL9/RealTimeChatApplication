package com.pritam44.RealTimeChatApp.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthFilter,
            AuthenticationProvider authenticationProvider) {

        this.jwtAuthFilter = jwtAuthFilter;
        this.authenticationProvider = authenticationProvider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> {})
            .sessionManagement(sm ->
                sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            .authenticationProvider(authenticationProvider)
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)

            .authorizeHttpRequests(auth -> auth
            	    .requestMatchers(
            	        "/login",
            	        "/signup",
            	        "/api/auth/**",

            	        // HTML pages
            	        "/chat",
            	        "/requests",

            	        // 🔴 REQUIRED FOR WEBSOCKET
            	        "/ws/**",
            	        "/chat/**",
            	        "/topic/**",
            	        "/app/**",

            	        // static
            	        "/css/**",
            	        "/js/**",
            	        "/favicon.ico",
            	        "/error"
            	    ).permitAll()

            	    .requestMatchers(
            	        "/api/chat-requests/**",
            	        "/api/private-chats/**"
            	    ).authenticated()

            	    .anyRequest().authenticated()
            	
            );

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
