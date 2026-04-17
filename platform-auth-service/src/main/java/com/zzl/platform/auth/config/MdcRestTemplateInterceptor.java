package com.zzl.platform.auth.config;

import com.zzl.platform.common.core.util.MdcUtils;

import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * MDC RestTemplate拦截器
 * 在使用RestTemplate调用其他服务时，自动传递MDC链路追踪信息
 */
@Component
public class MdcRestTemplateInterceptor implements ClientHttpRequestInterceptor {

    /**
     * 追踪ID请求头
     */
    private static final String TRACE_ID_HEADER = "X-Trace-Id";

    /**
     * 父级追踪ID请求头
     */
    private static final String PARENT_TRACE_ID_HEADER = "X-Parent-Trace-Id";

    /**
     * 用户ID请求头
     */
    private static final String USER_ID_HEADER = "X-User-Id";

    /**
     * 用户名请求头
     */
    private static final String USERNAME_HEADER = "X-Username";

    /**
     * 租户ID请求头
     */
    private static final String TENANT_ID_HEADER = "X-Tenant-Id";

    @Override
    public ClientHttpResponse intercept(
            HttpRequest request,
            byte[] body,
            ClientHttpRequestExecution execution) throws IOException {

        try {
            // 获取当前追踪ID
            String traceId = MdcUtils.getTraceId();

            if (!traceId.equals("N/A")) {
                // 设置追踪ID到请求头
                request.getHeaders().set(TRACE_ID_HEADER, traceId);

                // 设置父级追踪ID
                String parentTraceId = MdcUtils.get("parentTraceId", null);
                if (parentTraceId != null) {
                    request.getHeaders().set(PARENT_TRACE_ID_HEADER, parentTraceId);
                }

                // 设置用户信息
                String userId = MdcUtils.get("userId", null);
                String username = MdcUtils.get("username", null);

                if (userId != null) {
                    request.getHeaders().set(USER_ID_HEADER, userId);
                }
                if (username != null) {
                    request.getHeaders().set(USERNAME_HEADER, username);
                }

                // 设置租户ID
                String tenantId = MdcUtils.get("tenantId", null);
                if (tenantId != null) {
                    request.getHeaders().set(TENANT_ID_HEADER, tenantId);
                }

                System.out.println("[MDC拦截器] 传递链路追踪信息: " +
                        "traceId=" + traceId +
                        ", userId=" + userId +
                        ", 请求URI=" + request.getURI());
            }

            return execution.execute(request, body);

        } catch (Exception e) {
            System.err.println("[MDC拦截器异常] " + e.getMessage());
            throw e;
        }
    }
}
