package com.zzl.platform.common.core.feign;

import com.zzl.platform.common.core.util.MdcUtils;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

/**
 * Feign客户端上下文拦截器
 * 自动传递MDC链路追踪信息到下游服务
 */
public class PlatformFeignClient implements RequestInterceptor {

    private static final Logger log = LoggerFactory.getLogger(PlatformFeignClient.class);

    @Override
    public void apply(RequestTemplate template) {
        // 传递链路追踪ID
        String traceId = MdcUtils.getTraceId();
        if (traceId != null && !"N/A".equals(traceId)) {
            template.header("X-Trace-Id", traceId);
        }

        // 传递用户ID (存放在MDC的userId key)
        String userId = MDC.get("userId");
        if (userId != null) {
            template.header("X-User-Id", userId);
        }

        // 传递用户名 (存放在MDC的username key)
        String username = MDC.get("username");
        if (username != null) {
            template.header("X-Username", username);
        }

        // 传递租户ID (存放在MDC的tenantId key)
        String tenantId = MDC.get("tenantId");
        if (tenantId != null) {
            template.header("X-Tenant-Id", tenantId);
        }

        log.debug("Feign请求传递上下文: traceId={}, userId={}, tenantId={}", traceId, userId, tenantId);
    }
}
