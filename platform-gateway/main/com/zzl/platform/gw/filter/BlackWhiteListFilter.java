package com.zzl.platform.gw.filter;

import com.zzl.platform.gw.properties.GatewayProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 黑白名单过滤器
 * 支持IP和用户级别的访问控制
 */
@Slf4j
@Component
public class BlackWhiteListFilter extends AbstractGatewayFilter {

    private final GatewayProperties gatewayProperties;

    public BlackWhiteListFilter(GatewayProperties gatewayProperties) {
        this.gatewayProperties = gatewayProperties;
    }

    @Override
    protected String getFilterName() {
        return "BlackWhiteListFilter";
    }

    @Override
    protected Mono<Void> doFilter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = getPath(exchange);
        String ip = getRemoteIp(exchange);
        String userId = getUserId(exchange);

        // 白名单优先：白名单路径直接放行，不受黑名单限制
        if (gatewayProperties.getWhitePaths() != null && !gatewayProperties.getWhitePaths().isEmpty()) {
            if (isInWhiteList(path)) {
                log.debug("Path is in whitelist, allowing: path={}", path);
                return chain.filter(exchange);
            }
        }

        // 检查黑名单
        if (isInBlackList(ip, userId, exchange)) {
            log.warn("Request blocked by blacklist: ip={}, userId={}, path={}", ip, userId, path);
            return handleBlocked(exchange, "Access denied by blacklist");
        }

        return chain.filter(exchange);
    }

    /**
     * 检查是否在黑名单中
     */
    private boolean isInBlackList(String ip, String userId, ServerWebExchange exchange) {
        // 检查IP黑名单
        if (gatewayProperties.getBlackPaths() != null) {
            for (String blackItem : gatewayProperties.getBlackPaths()) {
                if (blackItem.startsWith("ip:")) {
                    String blackIp = blackItem.substring(3);
                    if (ip.equals(blackIp) || isIpInRange(ip, blackIp)) {
                        return true;
                    }
                } else if (blackItem.startsWith("user:")) {
                    String blackUser = blackItem.substring(5);
                    if (userId != null && userId.equals(blackUser)) {
                        return true;
                    }
                } else if (blackItem.startsWith("path:")) {
                    String blackPath = blackItem.substring(5);
                    String path = getPath(exchange);
                    if (path.startsWith(blackPath)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * 检查是否在白名单中
     */
    private boolean isInWhiteList(String path) {
        return gatewayProperties.getWhitePaths().stream()
                .anyMatch(whitePath -> pathMatches(path, whitePath));
    }

    /**
     * 获取用户ID
     */
    private String getUserId(ServerWebExchange exchange) {
        // 从Token中提取用户ID（简化版）
        String token = exchange.getRequest().getHeaders().getFirst("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            String jwt = token.substring(7);
            // TODO: 实际应解析JWT获取用户ID
            return extractUserIdFromJwt(jwt);
        }
        return null;
    }

    /**
     * 从JWT中提取用户ID（简化版）
     */
    private String extractUserIdFromJwt(String jwt) {
        try {
            // TODO: 实际应使用JWT库解析
            String[] parts = jwt.split("\\.");
            if (parts.length >= 2) {
                String payload = parts[1];
                // 简化版：直接从payload中提取
                if (payload.contains("\"userId\"")) {
                    int userIdStart = payload.indexOf("\"userId\"") + 8;
                    int userIdEnd = payload.indexOf("\"", userIdStart);
                    if (userIdEnd > userIdStart) {
                        return payload.substring(userIdStart, userIdEnd);
                    }
                }
            }
        } catch (Exception e) {
            log.debug("Failed to extract user ID from JWT", e);
        }
        return null;
    }

    /**
     * 检查IP是否在范围内（支持CIDR）
     */
    private boolean isIpInRange(String ip, String ipRange) {
        try {
            // 简化版：检查是否匹配（实际应支持CIDR）
            return ip.equals(ipRange);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 处理被拒绝的请求
     */
    private Mono<Void> handleBlocked(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.FORBIDDEN);
        response.getHeaders().add("Content-Type", "application/json");

        String body = String.format(
                "{\"code\":403,\"message\":\"%s\",\"data\":null,\"traceId\":\"%s\"}",
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
        // 在鉴权过滤器之前执行
        return 50;
    }
}
