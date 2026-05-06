package com.zzl.platform.common.core.util;

import cn.hutool.core.util.StrUtil;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT Token解析工具类
 * 提供统一的Token解析、验证、生成能力
 */
@Slf4j
public class JwtUtils {

    private static final String DEFAULT_SECRET = "platform-default-secret-key-must-be-at-least-256-bits";
    private static final long DEFAULT_EXPIRATION = 86400000L; // 24小时

    private JwtUtils() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * 解析Token
     *
     * @param token JWT token
     * @return Claims claims
     */
    public static Claims parseToken(String token) {
        return parseToken(token, DEFAULT_SECRET);
    }

    /**
     * 解析Token
     *
     * @param token  JWT token
     * @param secret 密钥
     * @return Claims claims
     */
    public static Claims parseToken(String token, String secret) {
        if (StrUtil.isBlank(token)) {
            return null;
        }
        try {
            SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            log.debug("Token已过期: {}", e.getMessage());
            return null;
        } catch (SignatureException e) {
            log.debug("Token签名验证失败: {}", e.getMessage());
            return null;
        } catch (MalformedJwtException e) {
            log.debug("Token格式错误: {}", e.getMessage());
            return null;
        } catch (JwtException e) {
            log.debug("Token解析失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 验证Token是否有效
     *
     * @param token JWT token
     * @return 是否有效
     */
    public static boolean validateToken(String token) {
        return validateToken(token, DEFAULT_SECRET);
    }

    /**
     * 验证Token是否有效
     *
     * @param token  JWT token
     * @param secret 密钥
     * @return 是否有效
     */
    public static boolean validateToken(String token, String secret) {
        if (StrUtil.isBlank(token)) {
            return false;
        }
        try {
            SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException e) {
            log.debug("Token验证失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 判断Token是否过期
     *
     * @param token JWT token
     * @return 是否过期
     */
    public static boolean isTokenExpired(String token) {
        Claims claims = parseToken(token);
        if (claims == null) {
            return true;
        }
        Date expiration = claims.getExpiration();
        return expiration == null || expiration.before(new Date());
    }

    /**
     * 获取用户ID
     *
     * @param token JWT token
     * @return 用户ID
     */
    public static String getUserId(String token) {
        Claims claims = parseToken(token);
        if (claims == null) {
            return null;
        }
        Object userId = claims.get("userId");
        if (userId == null) {
            userId = claims.getSubject();
        }
        return userId != null ? userId.toString() : null;
    }

    /**
     * 获取用户名
     *
     * @param token JWT token
     * @return 用户名
     */
    public static String getUsername(String token) {
        Claims claims = parseToken(token);
        if (claims == null) {
            return null;
        }
        return claims.getSubject();
    }

    /**
     * 获取租户ID
     *
     * @param token JWT token
     * @return 租户ID
     */
    public static String getTenantId(String token) {
        Claims claims = parseToken(token);
        if (claims == null) {
            return null;
        }
        Object tenantId = claims.get("tenantId");
        return tenantId != null ? tenantId.toString() : null;
    }

    /**
     * 获取Token中的指定属性
     *
     * @param token    JWT token
     * @param claimKey 属性名
     * @return 属性值
     */
    public static Object getClaim(String token, String claimKey) {
        Claims claims = parseToken(token);
        if (claims == null) {
            return null;
        }
        return claims.get(claimKey);
    }

    /**
     * 生成Token
     *
     * @param userId   用户ID
     * @param username 用户名
     * @return JWT token
     */
    public static String generateToken(String userId, String username) {
        return generateToken(userId, username, DEFAULT_EXPIRATION);
    }

    /**
     * 生成Token
     *
     * @param userId     用户ID
     * @param username   用户名
     * @param expiration 过期时间（毫秒）
     * @return JWT token
     */
    public static String generateToken(String userId, String username, long expiration) {
        return generateToken(userId, username, expiration, new HashMap<>());
    }

    /**
     * 生成Token
     *
     * @param userId      用户ID
     * @param username    用户名
     * @param expiration  过期时间（毫秒）
     * @param extraClaims 额外参数
     * @return JWT token
     */
    public static String generateToken(String userId, String username, long expiration, Map<String, Object> extraClaims) {
        SecretKey key = Keys.hmacShaKeyFor(DEFAULT_SECRET.getBytes(StandardCharsets.UTF_8));
        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + expiration);

        JwtBuilder builder = Jwts.builder()
                .subject(username)
                .claim("userId", userId)
                .issuedAt(now)
                .expiration(expirationDate)
                .signWith(key);

        extraClaims.forEach(builder::claim);

        return builder.compact();
    }
}
