package com.example.demo.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.util.Date;

public class JwtUtil {

    private static final SecretKey KEY = Keys.hmacShaKeyFor(
            "CampusTradeJWTSecretKey2024!@#$%^&*()".getBytes());
    private static final long EXPIRE_DAYS = 7;

    public static String generate(Long userId, Integer role) {
        Date now = new Date();
        Date expire = new Date(now.getTime() + EXPIRE_DAYS * 86400000L);
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("role", role)
                .issuedAt(now)
                .expiration(expire)
                .signWith(KEY)
                .compact();
    }

    public static Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(KEY)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public static Long getUserId(Claims claims) {
        return Long.parseLong(claims.getSubject());
    }

    public static Integer getRole(Claims claims) {
        return claims.get("role", Integer.class);
    }
}
