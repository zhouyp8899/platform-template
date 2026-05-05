package com.zzl.platform.gw.filter;

import com.zzl.platform.common.core.util.TraceIdGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Gateway抽象过滤器基类
 * 提供通用的时间统计、日志记录等功能
 */
@Slf4j
public abstract class AbstractGatewayFilter implements GlobalFilter, Ordered {

    /**
     * 开始时间戳的请求属性名
     */
    protected static final String START_TIME_ATTR = "gateway_start_time";

    /**
     * TraceId的请求属性名
     */
    protected static final String TRACE_ID_ATTR = "trace_id";

    /**
     * 获取过滤器名称
     */
    protected abstract String getFilterName();

    /**
     * 执行过滤逻辑
     */
    protected abstract Mono<Void> doFilter(ServerWebExchange exchange, GatewayFilterChain chain);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 记录开始时间
        exchange.getAttributes().put(START_TIME_ATTR, System.currentTimeMillis());

        log.debug("[{}] Filter start", getFilterName());

        return doFilter(exchange, chain)
                .doFinally(signalType -> {
                    long startTime = exchange.getAttributeOrDefault(START_TIME_ATTR, 0L);
                    long duration = System.currentTimeMillis() - startTime;

                    log.debug("[{}] Filter end, duration: {}ms", getFilterName(), duration);
                });
    }

    /**
     * 获取请求路径
     */
    protected String getPath(ServerWebExchange exchange) {
        return exchange.getRequest().getURI().getPath();
    }

    /**
     * 获取请求方法
     */
    protected String getMethod(ServerWebExchange exchange) {
        return exchange.getRequest().getMethod().name();
    }

    /**
     * 获取客户端IP
     */
    protected String getRemoteIp(ServerWebExchange exchange) {
        String ip = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
            ip = exchange.getRequest().getHeaders().getFirst("X-Real-IP");
        }
        if (ip == null || ip.isEmpty()) {
            ip = exchange.getRequest().getRemoteAddress() != null ?
                    exchange.getRequest().getRemoteAddress().getAddress().getHostAddress() : "unknown";
        }
        return ip;
    }

    /**
     * 获取TraceId
     */
    protected String getTraceId(ServerWebExchange exchange) {
        String traceId = exchange.getRequest().getHeaders().getFirst("X-Trace-Id");
        if (traceId == null || traceId.isEmpty()) {
            traceId = exchange.getAttribute(TRACE_ID_ATTR);
        }
        if (traceId == null || traceId.isEmpty()) {
            traceId = generateTraceId();
        }
        return traceId;
    }

    /**
     * 生成TraceId
     */
    protected String generateTraceId() {
        return TraceIdGenerator.generate();
    }

    /**
     * 判断路径是否匹配模式
     */
    protected boolean pathMatches(String path, String pattern) {
        return path.startsWith(pattern);
    }
}
