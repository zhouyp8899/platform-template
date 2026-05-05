package com.zzl.platform.gw.filter;

import com.zzl.platform.common.core.util.MdcUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * Gateway MDC链路追踪过滤器
 * 负责在Gateway层初始化、传递和清理MDC上下文，确保与下游服务的链路追踪一致性
 */
@Component
public class MdcGatewayFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(MdcGatewayFilter.class);

    /**
     * 请求头中携带追踪ID的键名
     */
    private static final String TRACE_ID_HEADER = "X-Trace-Id";

    /**
     * 请求头中携带父级追踪ID的键名
     */
    private static final String PARENT_TRACE_ID_HEADER = "X-Parent-Trace-Id";

    /**
     * 请求头中携带用户ID的键名
     */
    private static final String USER_ID_HEADER = "X-User-Id";

    /**
     * 请求头中携带用户名的键名
     */
    private static final String USERNAME_HEADER = "X-Username";

    /**
     * 请求头中携带租户ID的键名
     */
    private static final String TENANT_ID_HEADER = "X-Tenant-Id";

    /**
     * 存储原始MDC上下文的请求属性键
     */
    private static final String ORIGINAL_MDC_CONTEXT_ATTR = "ORIGINAL_MDC_CONTEXT";

    /**
     * 存储追踪ID的请求属性键
     */
    private static final String TRACE_ID_ATTR = "TRACE_ID";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 捕获当前线程的原始MDC上下文（如果存在）
        Map<String, String> originalContext = MdcUtils.getCopyOfContextMap();
        if (originalContext != null) {
            exchange.getAttributes().put(ORIGINAL_MDC_CONTEXT_ATTR, originalContext);
        }

        return Mono.defer(() -> {
            try {
                // 恢复或初始化MDC上下文
                Map<String, String> contextToRestore = exchange.getAttribute(ORIGINAL_MDC_CONTEXT_ATTR);
                if (contextToRestore != null && !contextToRestore.isEmpty()) {
                    MdcUtils.setContextMap(contextToRestore);
                }

                // 处理MDC上下文
                processMdcContext(exchange);

                String traceId = exchange.getAttribute(TRACE_ID_ATTR);

                return chain.filter(exchange)
                        .doFinally(signalType -> {
                            try {
                                // 在finally中清理MDC，防止内存泄漏
                                loggerWithTrace(traceId, "清理MDC上下文: signalType={}", signalType);
                                MdcUtils.clear();
                            } catch (Exception e) {
                                logError("清理MDC上下文异常: traceId={}", traceId, e);
                            }
                        });
            } catch (Exception e) {
                logError("MDC过滤器执行异常", null, e);
                return chain.filter(exchange);
            }
        });
    }

    /**
     * 处理MDC上下文
     */
    private void processMdcContext(ServerWebExchange exchange) {
        ServerHttpRequest request = exchange.getRequest();

        // 从请求头获取追踪ID，如果没有则生成新的
        String traceId = request.getHeaders().getFirst(TRACE_ID_HEADER);
        String parentTraceId = request.getHeaders().getFirst(PARENT_TRACE_ID_HEADER);

        String currentTraceId;
        if (parentTraceId != null && !parentTraceId.isEmpty()) {
            // 如果存在父级追踪ID，创建子追踪ID
            currentTraceId = MdcUtils.createChildTraceId(parentTraceId);
            loggerWithTrace(currentTraceId, "创建子追踪ID: parentTraceId={}", parentTraceId);
        } else if (traceId != null && !traceId.isEmpty()) {
            // 如果存在追踪ID，直接使用
            currentTraceId = MdcUtils.init(traceId);
            loggerWithTrace(currentTraceId, "使用传入的追踪ID");
        } else {
            // 生成新的追踪ID
            currentTraceId = MdcUtils.init(null);
            loggerWithTrace(currentTraceId, "生成新的追踪ID");
        }

        // 存储追踪ID到请求属性
        exchange.getAttributes().put(TRACE_ID_ATTR, currentTraceId);

        // 设置用户信息
        String userId = request.getHeaders().getFirst(USER_ID_HEADER);
        String username = request.getHeaders().getFirst(USERNAME_HEADER);
        if (userId != null || username != null) {
            MdcUtils.setUser(userId, username);
        }

        // 设置租户ID
        String tenantId = request.getHeaders().getFirst(TENANT_ID_HEADER);
        if (tenantId != null) {
            MdcUtils.setTenantId(tenantId);
        }

        // 设置客户端IP
        String clientIp = getRemoteIp(exchange);
        MdcUtils.setClientIp(clientIp);

        // 设置请求信息
        String path = request.getURI().getPath();
        String method = request.getMethod().name();
        MdcUtils.setRequest(path, method);

        // 将追踪ID添加到响应头（如果需要返回给客户端）
        ServerHttpRequest mutatedRequest = request.mutate()
                .header(TRACE_ID_HEADER, currentTraceId)
                .build();
        exchange.mutate().request(mutatedRequest).build();

        loggerWithTrace(currentTraceId, "Gateway MDC上下文初始化完成: path={}, method={}, clientIp={}",
                path, method, clientIp);
    }

    /**
     * 获取客户端真实IP地址
     */
    private String getRemoteIp(ServerWebExchange exchange) {
        String ip = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = exchange.getRequest().getHeaders().getFirst("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = exchange.getRequest().getHeaders().getFirst("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = exchange.getRequest().getHeaders().getFirst("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = exchange.getRequest().getRemoteAddress() != null ?
                    exchange.getRequest().getRemoteAddress().getAddress().getHostAddress() : "unknown";
        }

        // 处理多个IP的情况
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }

        return ip;
    }

    /**
     * 带追踪ID的日志记录
     */
    private void loggerWithTrace(String traceId, String message, Object... args) {
        if (log.isDebugEnabled()) {
            String logMessage = String.format(message, args);
            log.debug("[Gateway MDC] traceId=%s - %s", traceId, logMessage);
        }
    }

    /**
     * 错误日志记录
     */
    private void logError(String message, String traceId, Throwable e) {
        String logMessage = traceId != null ?
                String.format("traceId=%s - %s", traceId, message) :
                message;
        log.error("[Gateway MDC] {}", logMessage, e);
    }

    @Override
    public int getOrder() {
        // 最高优先级，确保在其他过滤器之前执行
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
