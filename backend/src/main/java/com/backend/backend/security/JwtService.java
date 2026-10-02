package com.backend.backend.security;

import java.time.Instant;
import java.util.Date;
import java.util.Map;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;

@Component
public class JwtService {
    @Value("${auth.token.jwtSecret}")
    private String secret;

    @Value("${auth.token.expirationInMils:86400000}")
    private long expirationMs;

    private SecretKey signingKey;

    @PostConstruct
    void initialize() {
        signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
    }

    public String generateToken(Map<String, Object> extraClaims, String subjectEmail) {
        Instant now = Instant.now();

        return Jwts.builder()
            .claims()
                .add(extraClaims)
                .subject(subjectEmail)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(expirationMs)))
            .and()
            .signWith(signingKey)
            .compact();
    }

    public String extractUsername(String token) {
        return parse(token).getPayload().getSubject();
    }

    public boolean isTokenValid(String token, UserDetails user) {
        return user.getUsername().equals(extractUsername(token));
    }

    private Jws<Claims> parse(String token) {
        return Jwts.parser()
            .verifyWith(signingKey)
            .build()
            .parseSignedClaims(token);
    }
}