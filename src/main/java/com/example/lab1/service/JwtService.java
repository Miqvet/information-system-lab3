package com.example.lab1.service;

import com.example.lab1.domain.entity.auth.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;


import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import javax.crypto.SecretKey;

@Service
public class JwtService {
    private final String jwtSecret = "3q2+796tvu8x9+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v";
    public String extractUserName(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        if (userDetails instanceof User customUserDetails) {
            claims.put("id", customUserDetails.getId());
            claims.put("role", customUserDetails.getRole().getName().name());
        }
        return createToken(claims, userDetails.getUsername());
    }

    public boolean isTokenValid(String token, String userName) {
        final String username = extractUserName(token);
        return (username.equals(userName)) && !isTokenExpired(token);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 1000000 * 60 * 24))
                .signWith(getSigningKey())
                .compact();
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }
}