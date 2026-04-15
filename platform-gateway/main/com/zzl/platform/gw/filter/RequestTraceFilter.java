package com.zzl.platform.gw.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 请求追踪过滤器
 * 添加TraceId、请求ID、时间戳等信息到请求头
 */
@Slf4j
@Component
public class RequestTraceFilter extends AbstractGatewayFilter {

    private static final String TRACE_ID_HEADER = "X-Trace-Id";
    private static final String REQUEST_ID_HEADER = "X-Request-Id";
    private static final String REQUEST_TIME_HEADER = "X-Request-Time";
    private static final String CLIENT_IP_HEADER = "X-Client-Ip";
    private static final String CLIENT_USER_AGENT_HEADER = "X-Client-User-Agent";
    private static final String START_TIME_ATTR = "request_start_time";

    private static final DateTimeFormatter TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    @Override
    protected String getFilterName() {
        return "RequestTraceFilter";
    }

    @Override
    protected Mono<Void> doFilter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 记录开始时间
        long startTime = System.currentTimeMillis();
        exchange.getAttributes().put(START_TIME_ATTR, startTime);

        // 生成/获取TraceId
        String traceId = getTraceId(exchange);
        if (!exchange.getRequest().getHeaders().containsKey(TRACE_ID_HEADER)) {
            exchange.getRequest().mutate().header(TRACE_ID_HEADER, traceId).build();
        }

        // 生成/获取RequestId
        String requestId = generateRequestId();
        if (!exchange.getRequest().getHeaders().containsKey(REQUEST_ID_HEADER)) {
            exchange.getRequest().mutate().header(REQUEST_ID_HEADER, requestId).build();
        }

        // 添加请求时间戳
        String requestTime = TIME_FORMATTER.format(LocalDateTime.now());
        if (!exchange.getRequest().getHeaders().containsKey(REQUEST_TIME_HEADER)) {
            exchange.getRequest().mutate().header(REQUEST_TIME_HEADER, requestTime).build();
        }

        // 添加客户端IP
        String clientIp = getRemoteIp(exchange);
        if (!exchange.getRequest().getHeaders().containsKey(CLIENT_IP_HEADER)) {
            exchange.getRequest().mutate().header(CLIENT_IP_HEADER, clientIp).build();
        }

        // 添加User-Agent
        String userAgent = exchange.getRequest().getHeaders().getFirst("User-Agent");
        if (userAgent != null && !exchange.getRequest().getHeaders().containsKey(CLIENT_USER_AGENT_HEADER)) {
            exchange.getRequest().mutate().header(CLIENT_USER_AGENT_HEADER, userAgent).build();
        }

        // 记录请求追踪信息
        logRequest(exchange, traceId, requestId, startTime);

        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            long duration = System.currentTimeMillis() - startTime;
            logResponse(exchange, traceId, requestId, duration);
        }));
    }

    /**
     * 生成RequestId
     */
    private String generateRequestId() {
        return java.util.UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 记录请求追踪信息
     */
    private void logRequest(ServerWebExchange exchange, String traceId, String requestId, long startTime) {
        log.info("========== Request Start ==========");
        log.info("TraceId: {}, RequestId: {}", traceId, requestId);
        log.info("Method: {}, Path: {}", getMethod(exchange), getPath(exchange));
        log.info("RemoteIP: {}, UserAgent: {}",
                exchange.getRequest().getHeaders().getFirst("X-Forwarded-For"),
                exchange.getRequest().getHeaders().getFirst("User-Agent"));
        log.info("StartTime: {}", Instant.ofEpochMilli(startTime));
        log.info("================================");
    }

    /**
     * 记录响应追踪信息
     */
    private void logResponse(ServerWebExchange exchange, String traceId, String requestId, long duration) {
        int statusCode = exchange.getResponse().getStatusCode() != null ?
                exchange.getResponse().getStatusCode().value() : 0;

        log.info("========== Request End ==========");
        log.info("TraceId: {}, RequestId: {}", traceId, requestId);
        log.info("Method: {}, Path: {}, Status: {}",
                getMethod(exchange), getPath(exchange), statusCode);
        log.info("Duration: {}ms", duration);
        log.info("================================");
    }

    @Override
    public int getOrder() {
        // 优先级最高，最先执行
        return HIGHEST_PRECEDENCE;
    }
}
