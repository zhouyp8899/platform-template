package com.zzl.platform.gw.filter;

import com.zzl.platform.gw.properties.GatewayProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 请求日志过滤器
 * 记录所有请求和响应的详细信息，支持全链路追踪
 */
@Slf4j
@Component
public class LogFilter extends AbstractGatewayFilter {

    private final GatewayProperties gatewayProperties;

    public LogFilter(GatewayProperties gatewayProperties) {
        this.gatewayProperties = gatewayProperties;
    }

    @Override
    protected String getFilterName() {
        return "LogFilter";
    }

    @Override
    protected Mono<Void> doFilter(ServerWebExchange exchange, GatewayFilterChain chain) {
        if (!gatewayProperties.isLogEnabled()) {
            return chain.filter(exchange);
        }

        long startTime = System.currentTimeMillis();
        String traceId = getTraceId(exchange);
        String path = getPath(exchange);
        String method = getMethod(exchange);
        String ip = getRemoteIp(exchange);

        // 注意：不在此设置TRACE_ID_ATTR，避免覆盖MdcGatewayFilter已设置的traceId

        // 记录请求日志
        logRequest(exchange, traceId, path, method, ip, startTime);

        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            long duration = System.currentTimeMillis() - startTime;
            int statusCode = exchange.getResponse().getStatusCode() != null ?
                    exchange.getResponse().getStatusCode().value() : 0;

            // 记录响应日志
            logResponse(exchange, traceId, path, method, ip, statusCode, duration);
        }));
    }

    /**
     * 记录请求日志
     */
    private void logRequest(ServerWebExchange exchange, String traceId, String path,
                            String method, String ip, long startTime) {
        ServerHttpRequest request = exchange.getRequest();

        log.info("[{}] Request: {} {} from {}, TraceId: {}",
                startTime, method, path, ip, traceId);

        // 记录请求头（如果配置启用）
        if (gatewayProperties.getLog().isLogHeaders()) {
            request.getHeaders().forEach((key, values) -> {
                log.debug("[{}] Header: {} = {}", traceId, key, values);
            });
        }

        // 记录查询参数
        if (!request.getQueryParams().isEmpty()) {
            log.info("[{}] Query Params: {}", traceId, request.getQueryParams());
        }
    }

    /**
     * 记录响应日志
     */
    private void logResponse(ServerWebExchange exchange, String traceId, String path,
                             String method, String ip, int statusCode, long duration) {
        log.info("[{}] Response: {} {} status={}, duration={}ms, TraceId: {}",
                System.currentTimeMillis(), method, path, statusCode, duration, traceId);
    }

    @Override
    public int getOrder() {
        // 最优先执行
        return HIGHEST_PRECEDENCE + 1;
    }
}
