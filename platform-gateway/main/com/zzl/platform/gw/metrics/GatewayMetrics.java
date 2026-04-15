package com.zzl.platform.gw.metrics;

import io.micrometer.core.instrument.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Gateway监控指标
 * 提供请求量、响应时间、错误率等监控指标
 */
@Slf4j
@Component
public class GatewayMetrics {

    private final MeterRegistry meterRegistry;

    // 请求计数器
    private final Counter requestCounter;
    private final Counter successCounter;
    private final Counter errorCounter;
    private final Counter timeoutCounter;
    private final Counter authErrorCounter;
    private final Counter rateLimitCounter;

    // 响应时间统计
    private final AtomicLong totalResponseTime = new AtomicLong(0);
    private final AtomicLong totalRequests = new AtomicLong(0);
    private final AtomicReference<Long> maxResponseTime = new AtomicReference<>(0L);

    public GatewayMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;

        // 初始化计数器
        this.requestCounter = Counter.builder("gateway.requests.total")
                .description("Total number of requests")
                .register(meterRegistry);
        this.successCounter = Counter.builder("gateway.requests.success")
                .description("Number of successful requests")
                .register(meterRegistry);
        this.errorCounter = Counter.builder("gateway.requests.error")
                .description("Number of error requests")
                .register(meterRegistry);
        this.timeoutCounter = Counter.builder("gateway.requests.timeout")
                .description("Number of timeout requests")
                .register(meterRegistry);
        this.authErrorCounter = Counter.builder("gateway.requests.auth_error")
                .description("Number of authentication errors")
                .register(meterRegistry);
        this.rateLimitCounter = Counter.builder("gateway.requests.rate_limited")
                .description("Number of rate limited requests")
                .register(meterRegistry);

        // 注册 Gauge 指标
        Gauge.builder("gateway.response.time.avg", totalResponseTime,
                        t -> totalRequests.get() > 0 ? t.get() / totalRequests.get() : 0)
                .description("Average response time in milliseconds")
                .register(meterRegistry);

        Gauge.builder("gateway.response.time.max", maxResponseTime, AtomicReference::get)
                .description("Maximum response time in milliseconds")
                .register(meterRegistry);

        log.info("Gateway metrics initialized");
    }

    /**
     * 记录请求
     */
    public void recordRequest(String path, String method) {
        Tags tags = Tags.of(
                Tag.of("path", sanitizeTag(path)),
                Tag.of("method", method)
        );
        Counter.builder("gateway.requests.total")
                .tags(tags)
                .register(meterRegistry)
                .increment();
        totalRequests.incrementAndGet();
    }

    /**
     * 记录成功请求
     */
    public void recordSuccess(String path, String method) {
        Tags tags = Tags.of(
                Tag.of("path", sanitizeTag(path)),
                Tag.of("method", method)
        );
        Counter.builder("gateway.requests.success")
                .tags(tags)
                .register(meterRegistry)
                .increment();
    }

    /**
     * 记录错误请求
     */
    public void recordError(String path, String method, String errorType) {
        Tags tags = Tags.of(
                Tag.of("path", sanitizeTag(path)),
                Tag.of("method", method),
                Tag.of("error_type", errorType)
        );
        Counter.builder("gateway.requests.error")
                .tags(tags)
                .register(meterRegistry)
                .increment();
    }

    /**
     * 记录超时请求
     */
    public void recordTimeout(String path, String method) {
        Tags tags = Tags.of(
                Tag.of("path", sanitizeTag(path)),
                Tag.of("method", method)
        );
        Counter.builder("gateway.requests.timeout")
                .tags(tags)
                .register(meterRegistry)
                .increment();
    }

    /**
     * 记录鉴权错误
     */
    public void recordAuthError(String path, String reason) {
        Tags tags = Tags.of(
                Tag.of("path", sanitizeTag(path)),
                Tag.of("reason", reason)
        );
        Counter.builder("gateway.requests.auth_error")
                .tags(tags)
                .register(meterRegistry)
                .increment();
    }

    /**
     * 记录限流
     */
    public void recordRateLimit(String path, String limitType) {
        Tags tags = Tags.of(
                Tag.of("path", sanitizeTag(path)),
                Tag.of("limit_type", limitType)
        );
        Counter.builder("gateway.requests.rate_limited")
                .tags(tags)
                .register(meterRegistry)
                .increment();
    }

    /**
     * 记录响应时间
     */
    public void recordResponseTime(String path, String method, long responseTimeMs) {
        totalResponseTime.addAndGet(responseTimeMs);

        // 更新最大响应时间
        maxResponseTime.updateAndGet(current -> Math.max(current, responseTimeMs));

        log.debug("Recorded response time: {}ms for path: {}", responseTimeMs, path);
    }

    /**
     * 清理标签（防止标签值过长或包含特殊字符）
     */
    private String sanitizeTag(String tag) {
        if (tag == null) {
            return "unknown";
        }

        // 限制标签长度
        if (tag.length() > 50) {
            return tag.substring(0, 50);
        }

        // 替换特殊字符
        return tag.replaceAll("[^a-zA-Z0-9_-]", "_");
    }

    /**
     * 获取指标摘要
     */
    public MetricsSummary getSummary() {
        return new MetricsSummary(
                totalRequests.get(),
                successCounter.count(),
                errorCounter.count(),
                timeoutCounter.count(),
                authErrorCounter.count(),
                rateLimitCounter.count(),
                totalRequests.get() > 0 ?
                        totalResponseTime.get() / totalRequests.get() : 0
        );
    }

    /**
     * 指标摘要
     */
    public record MetricsSummary(
            long totalRequests,
            double successRate,
            double errorRate,
            double timeoutRate,
            double authErrorRate,
            double rateLimitLimitRate,
            double avgResponseTime
    ) {
    }
}
