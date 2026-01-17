package com.pritam44.RealTimeChatApp.service;

import java.security.Key;
import java.util.Date;

import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    // ✅ 256-bit minimum secret for HS256
    private static final String SECRET =
            "THIS_IS_A_VERY_SECURE_256_BIT_SECRET_KEY_FOR_JWT_AUTH";

    private static final long EXPIRATION = 1000 * 60 * 60 * 24; // 24 hours

    private final Key key = Keys.hmacShaKeyFor(SECRET.getBytes());

    /* ---------------- GENERATE TOKEN ---------------- */

    public String generateToken(String username) {

        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /* ---------------- EXTRACT USERNAME ---------------- */

    public String extractUsername(String token) {

        return extractAllClaims(token).getSubject();
    }

    /* ---------------- VALIDATION ---------------- */

    public boolean isTokenValid(String token, String username) {

        return extractUsername(token).equals(username)
                && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {

        return extractAllClaims(token)
                .getExpiration()
                .before(new Date());
    }

    private Claims extractAllClaims(String token) {

        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
