package com.zzl.platform.gw.filter;

import com.zzl.platform.common.core.util.JwtUtils;
import com.zzl.platform.gw.properties.GatewayProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 鉴权过滤器
 * 检查请求是否携带有效的JWT Token，支持白名单配置
 */
@Slf4j
@Component
public class AuthFilter extends AbstractGatewayFilter {

    private final GatewayProperties gatewayProperties;

    public AuthFilter(GatewayProperties gatewayProperties) {
        this.gatewayProperties = gatewayProperties;
    }

    @Override
    protected String getFilterName() {
        return "AuthFilter";
    }

    @Override
    protected Mono<Void> doFilter(ServerWebExchange exchange, GatewayFilterChain chain) {
        if (!gatewayProperties.isAuthEnabled()) {
            // 鉴权未启用，直接放行
            return chain.filter(exchange);
        }

        String path = getPath(exchange);

        // 白名单路径跳过Token校验（如登录接口）
        if (isWhitePath(path)) {
            log.debug("Path is in whitelist, skip auth: {}", path);
            return chain.filter(exchange);
        }

        // 检查Token
        String token = extractToken(exchange);
        if (token == null || token.isEmpty()) {
            log.warn("No token found for path: {}", path);
            return handleUnauthorized(exchange, "Missing authorization token");
        }

        // 验证Token
        try {
            String tokenType = extractTokenTypeFromToken(token);
            String jwtSecret = getJwtSecret(tokenType);

            if (!validateToken(token, jwtSecret)) {
                log.warn("Invalid token for path: {}, tokenType: {}", path, tokenType);
                return handleUnauthorized(exchange, "Invalid or expired authorization token");
            }

            // Token验证通过，提取用户信息并添加到请求头
            String userId = extractUserIdFromToken(token, jwtSecret);
            String username = extractUsernameFromToken(token, jwtSecret);
            String tenantId = extractTenantIdFromToken(token, jwtSecret);

            if (userId != null || username != null || tenantId != null) {
                exchange.getRequest().mutate()
                        .header("X-User-Id", userId != null ? userId : "")
                        .header("X-Username", username != null ? username : "")
                        .header("X-Tenant-Id", tenantId != null ? tenantId : "")
                        .build();
            }

            log.debug("Token validated for path: {}, userId: {}, username: {}, tokenType: {}", path, userId, username, tokenType);
        } catch (Exception e) {
            log.error("Token validation error", e);
            return handleUnauthorized(exchange, "Token validation error");
        }

        return chain.filter(exchange);
    }

    /**
     * 检查是否为白名单路径
     */
    private boolean isWhitePath(String path) {
        return gatewayProperties.getWhitePaths() != null
                && gatewayProperties.getWhitePaths().stream()
                .anyMatch(whitePath -> pathMatches(path, whitePath));
    }

    /**
     * 提取Token
     */
    private String extractToken(ServerWebExchange exchange) {
        String headerName = gatewayProperties.getAuth().getTokenHeader();
        String tokenPrefix = gatewayProperties.getAuth().getTokenPrefix();

        String authHeader = exchange.getRequest().getHeaders().getFirst(headerName);
        if (authHeader == null) {
            return null;
        }

        if (authHeader.startsWith(tokenPrefix)) {
            return authHeader.substring(tokenPrefix.length());
        }

        return authHeader;
    }

    /**
     * 从Token中提取token类型
     */
    private String extractTokenTypeFromToken(String token) {
        try {
            return JwtUtils.getClaim(token, "type") != null ? JwtUtils.getClaim(token, "type").toString() : "admin";
        } catch (Exception e) {
            log.debug("Failed to extract token type, default to admin", e);
            return "admin";
        }
    }

    /**
     * 根据token类型获取JWT密钥
     */
    private String getJwtSecret(String tokenType) {
        if ("h5".equals(tokenType)) {
            return gatewayProperties.getAuth().getJwtSecretH5();
        }
        return gatewayProperties.getAuth().getJwtSecretAdmin();
    }

    /**
     * 验证Token
     */
    private boolean validateToken(String token, String jwtSecret) {
        // 开发环境可跳过Token验证
        if (gatewayProperties.getAuth().isSkipTokenValidation()) {
            log.warn("Token validation skipped (dev mode)");
            return true;
        }

        return JwtUtils.validateToken(token, jwtSecret);
    }

    /**
     * 从Token中提取用户ID
     */
    private String extractUserIdFromToken(String token, String jwtSecret) {
        try {
            io.jsonwebtoken.Claims claims = io.jsonwebtoken.Jwts.parser()
                    .verifyWith(io.jsonwebtoken.security.Keys.hmacShaKeyFor(jwtSecret.getBytes(java.nio.charset.StandardCharsets.UTF_8)))
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            Object userId = claims.get("userId");
            return userId != null ? userId.toString() : null;
        } catch (Exception e) {
            log.debug("Failed to extract user ID from token", e);
            return null;
        }
    }

    /**
     * 从Token中提取用户名
     */
    private String extractUsernameFromToken(String token, String jwtSecret) {
        try {
            io.jsonwebtoken.Claims claims = io.jsonwebtoken.Jwts.parser()
                    .verifyWith(io.jsonwebtoken.security.Keys.hmacShaKeyFor(jwtSecret.getBytes(java.nio.charset.StandardCharsets.UTF_8)))
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return (String) claims.get("username");
        } catch (Exception e) {
            log.debug("Failed to extract username from token", e);
            return null;
        }
    }

    /**
     * 从Token中提取租户ID
     */
    private String extractTenantIdFromToken(String token, String jwtSecret) {
        try {
            io.jsonwebtoken.Claims claims = io.jsonwebtoken.Jwts.parser()
                    .verifyWith(io.jsonwebtoken.security.Keys.hmacShaKeyFor(jwtSecret.getBytes(java.nio.charset.StandardCharsets.UTF_8)))
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            Object tenantId = claims.get("tenantId");
            return tenantId != null ? tenantId.toString() : null;
        } catch (Exception e) {
            log.debug("Failed to extract tenant ID from token", e);
            return null;
        }
    }

    /**
     * 处理未授权响应
     */
    private Mono<Void> handleUnauthorized(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add("Content-Type", "application/json");

        String body = String.format(
                "{\"code\":401,\"message\":\"%s\",\"data\":null,\"traceId\":\"%s\"}",
                message,
                getTraceId(exchange)
        );

        return response.writeWith(
                reactor.core.publisher.Mono.just(
                        org.springframework.core.io.buffer.DefaultDataBufferFactory.sharedInstance
                                .wrap(body.getBytes())
                )
        );
    }

    @Override
    public int getOrder() {
        return 100;
    }
}
