package com.lab.face.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

public class JwtUtils {

    private static final String SECRET = "lab-face-system-secret-key-must-be-at-least-256-bits-long";
    private static final long EXPIRE_TIME = 24 * 60 * 60 * 1000;

    private static SecretKey getKey() {
        return Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
    }

    public static String generateToken(Long adminId, String username) {
        return Jwts.builder()
                .subject(username)
                .claim("adminId", adminId)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRE_TIME))
                .signWith(getKey())
                .compact();
    }

    public static Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public static boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}