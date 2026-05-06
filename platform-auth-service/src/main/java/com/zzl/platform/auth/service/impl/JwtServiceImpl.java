package com.zzl.platform.auth.service.impl;

import com.zzl.platform.auth.constants.AuthConstants;
import com.zzl.platform.auth.service.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT Token服务实现
 */
@Slf4j
@Service
public class JwtServiceImpl implements JwtService {

    @Value("${auth.jwt.secret-admin:platform-admin-jwt-secret-key-change-in-production}")
    private String jwtSecretAdmin;

    @Value("${auth.jwt.secret-h5:platform-h5-jwt-secret-key-change-in-production}")
    private String jwtSecretH5;

    private SecretKey getSecretKey(String tokenType) {
        String secret = jwtSecretAdmin;
        if (AuthConstants.TOKEN_TYPE_H5.equals(tokenType)) {
            secret = jwtSecretH5;
        }
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public String generateToken(Long userId, String username, String tokenType, Map<String, Object> extraClaims) {
        SecretKey key = getSecretKey(tokenType);
        long expireTime = getTokenExpireTime(tokenType) * 1000;

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("type", tokenType);
        if (extraClaims != null) {
            claims.putAll(extraClaims);
        }

        return Jwts.builder()
                .claims(claims)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expireTime))
                .signWith(key)
                .compact();
    }

    @Override
    public String generateRefreshToken(Long userId, String username, String tokenType) {
        SecretKey key = getSecretKey(tokenType);
        long expireTime = getRefreshTokenExpireTime(tokenType) * 1000;

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("type", tokenType);
        claims.put("refresh", true);

        return Jwts.builder()
                .claims(claims)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expireTime))
                .signWith(key)
                .compact();
    }

    @Override
    public Map<String, Object> parseToken(String token, String tokenType) {
        try {
            SecretKey key = getSecretKey(tokenType);
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            return new HashMap<>(claims);
        } catch (Exception e) {
            log.error("Parse token error: {}", e.getMessage());
            return null;
        }
    }

    @Override
    public boolean validateToken(String token, String tokenType) {
        try {
            SecretKey key = getSecretKey(tokenType);
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            log.debug("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public Long getUserIdFromToken(String token, String tokenType) {
        Map<String, Object> claims = parseToken(token, tokenType);
        if (claims == null) {
            return null;
        }
        Object userId = claims.get("userId");
        if (userId instanceof Number) {
            return ((Number) userId).longValue();
        }
        return null;
    }

    @Override
    public String getUsernameFromToken(String token, String tokenType) {
        Map<String, Object> claims = parseToken(token, tokenType);
        if (claims == null) {
            return null;
        }
        return (String) claims.get("username");
    }

    @Override
    public boolean isTokenExpiringSoon(String token, String tokenType) {
        try {
            SecretKey key = getSecretKey(tokenType);
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            Date expiration = claims.getExpiration();
            long timeToExpiry = expiration.getTime() - System.currentTimeMillis();
            return timeToExpiry < 5 * 60 * 1000; // 5分钟内过期
        } catch (Exception e) {
            return true;
        }
    }

    @Override
    public long getTokenExpireTime(String tokenType) {
        if (AuthConstants.TOKEN_TYPE_H5.equals(tokenType)) {
            return AuthConstants.TOKEN_EXPIRE_H5;
        }
        return AuthConstants.TOKEN_EXPIRE_ADMIN;
    }

    @Override
    public long getRefreshTokenExpireTime(String tokenType) {
        if (AuthConstants.TOKEN_TYPE_H5.equals(tokenType)) {
            return AuthConstants.REFRESH_TOKEN_EXPIRE_H5;
        }
        return AuthConstants.REFRESH_TOKEN_EXPIRE_ADMIN;
    }
}
