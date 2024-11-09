package com.example.lab1.service;

import com.example.lab1.domain.entity.auth.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;


import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {
    private String jwtSecret = "3q2+796tvu8x9+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v8+7v";
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

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUserName(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000000 * 60 * 24))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
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
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }
}