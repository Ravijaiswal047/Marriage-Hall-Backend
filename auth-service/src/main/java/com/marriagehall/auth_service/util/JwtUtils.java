package com.marriagehall.auth_service.util;

import com.marriagehall.auth_service.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtUtils {

    private final Key key;

    public JwtUtils(@Value("${jwt.secret:mysecretkeymysecretkeymysecretkey123}") String secret) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    // 1. Generate Token
    public String generateToken(User user){

        return Jwts.builder()
                .claim("userId", user.getId().toString())
                .claim("role", user.getRole().name())
                .setSubject(user.getEmail()) // Who is this user
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60)) // 1 hr
                .signWith(key , SignatureAlgorithm.HS256)
                .compact();
    }
    private Claims extractAllClaims(String token) {

        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
  // 2. Extract Email
    public String extractEmail(String token){
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }
 // 3.Validate token
    public boolean validateToken(String token){
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    public UUID extractUserId(String token){
        Claims claims = extractAllClaims(token);
        return UUID.fromString(claims.get("userId").toString());
    }

    public String extractRole(String token){
        Claims claims = extractAllClaims(token);
        return claims.get("role").toString();
    }

}
