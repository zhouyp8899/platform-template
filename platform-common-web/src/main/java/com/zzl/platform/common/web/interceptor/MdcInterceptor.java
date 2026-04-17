package com.zzl.platform.common.web.interceptor;

import com.zzl.platform.common.core.constant.MdcConstants;
import com.zzl.platform.common.core.util.MdcUtils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * MDC链路追踪拦截器
 * 在HTTP请求进入时初始化MDC上下文，在请求结束时清理MDC
 */
@Component
public class MdcInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(MdcInterceptor.class);

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
     * 请求前处理：初始化MDC上下文
     */
    @Override
    public boolean preHandle(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull Object handler) {

        // 从请求头获取追踪ID，如果没有则生成新的
        String traceId = request.getHeader(TRACE_ID_HEADER);
        String parentTraceId = request.getHeader(PARENT_TRACE_ID_HEADER);

        if (parentTraceId != null && !parentTraceId.isEmpty()) {
            // 如果存在父级追踪ID，创建子追踪ID
            String newTraceId = MdcUtils.createChildTraceId(parentTraceId);
            MdcUtils.put(MdcConstants.TRACE_ID, newTraceId);
        } else if (traceId != null && !traceId.isEmpty()) {
            // 如果存在追踪ID，直接使用
            MdcUtils.init(traceId);
        } else {
            // 生成新的追踪ID
            MdcUtils.init();
        }

        // 设置用户信息
        String userId = request.getHeader(USER_ID_HEADER);
        String username = request.getHeader(USERNAME_HEADER);
        if (userId != null || username != null) {
            MdcUtils.setUser(userId, username);
        }

        // 设置租户ID
        String tenantId = request.getHeader(TENANT_ID_HEADER);
        if (tenantId != null) {
            MdcUtils.setTenantId(tenantId);
        }

        // 设置客户端IP
        String clientIp = getClientIp(request);
        MdcUtils.setClientIp(clientIp);

        // 设置请求信息
        MdcUtils.setRequest(request.getRequestURI(), request.getMethod());

        // 将追踪ID写入响应头，便于客户端获取
        response.setHeader(TRACE_ID_HEADER, MdcUtils.getTraceId());

        logger.debug("MDC上下文初始化完成: {}", MdcUtils.getContextString());

        return true;
    }

    /**
     * 请求完成后处理：清理MDC上下文
     */
    @Override
    public void afterCompletion(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull Object handler,
            Exception ex) {

        if (ex != null) {
            logger.error("请求处理异常: {}", ex.getMessage(), ex);
        }

        logger.debug("清理MDC上下文: {}", MdcUtils.getContextString());

        // 清理MDC上下文，避免内存泄漏
        MdcUtils.clear();
    }

    /**
     * 获取客户端真实IP地址
     * 处理代理和负载均衡场景
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

        // 处理多个IP的情况（X-Forwarded-For可能包含多个IP）
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }

        return ip;
    }
}
