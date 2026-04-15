package com.zzl.platform.gw.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Sentinel熔断降级处理器
 * 当请求被Sentinel限流或熔断时，返回友好的降级响应
 * <p>
 * 注意：此处理器需要Sentinel Gateway Adapter依赖支持
 * 如需使用，请在build.gradle中添加：
 * implementation 'com.alibaba.cloud:spring-cloud-alibaba-sentinel-gateway'
 */
@Slf4j
@Component
public class SentinelFallbackHandler {

    /**
     * 生成TraceId
     */
    public String generateTraceId() {
        return java.util.UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 降级响应
     */
    public record FallbackResponse(int code, String message, Object data, String traceId) {
    }
}
