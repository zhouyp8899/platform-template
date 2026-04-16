package com.zzl.platform.gw.metrics;

import io.micrometer.core.instrument.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.LongAdder;

/**
 * Gateway监控指标
 * 提供请求量、响应时间、错误率等监控指标
 * 使用 LongAdder 优化高并发场景下的计数性能
 */
@Slf4j
@Component
public class GatewayMetrics {

    private final MeterRegistry meterRegistry;

    // 请求计数器（使用 Counter.Builder 缓存避免重复创建）
    private final ConcurrentMap<String, Counter> counterCache = new ConcurrentHashMap<>();
    private final Counter requestCounter;
    private final Counter successCounter;
    private final Counter errorCounter;
    private final Counter timeoutCounter;
    private final Counter authErrorCounter;
    private final Counter rateLimitCounter;

    // 响应时间统计（使用 LongAdder 优化高并发性能）
    private final LongAdder totalResponseTime = new LongAdder();
    private final LongAdder totalRequests = new LongAdder();
    private final AtomicReference<Long> maxResponseTime = new AtomicReference<>(0L);

    public GatewayMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;

        // 初始化基础计数器
        this.requestCounter = registerCounter("gateway.requests.total", "Total number of requests");
        this.successCounter = registerCounter("gateway.requests.success", "Number of successful requests");
        this.errorCounter = registerCounter("gateway.requests.error", "Number of error requests");
        this.timeoutCounter = registerCounter("gateway.requests.timeout", "Number of timeout requests");
        this.authErrorCounter = registerCounter("gateway.requests.auth_error", "Number of authentication errors");
        this.rateLimitCounter = registerCounter("gateway.requests.rate_limited", "Number of rate limited requests");

        // 注册 Gauge 指标
        Gauge.builder("gateway.response.time.avg", totalResponseTime,
                        t -> totalRequests.sum() > 0 ? t.sum() / totalRequests.sum() : 0)
                .description("Average response time in milliseconds")
                .register(meterRegistry);

        Gauge.builder("gateway.response.time.max", maxResponseTime, AtomicReference::get)
                .description("Maximum response time in milliseconds")
                .register(meterRegistry);

        Gauge.builder("gateway.requests.count", totalRequests, LongAdder::sum)
                .description("Total request count")
                .register(meterRegistry);

        log.info("Gateway metrics initialized");
    }

    /**
     * 注册计数器（带缓存）
     */
    private Counter registerCounter(String name, String description) {
        return Counter.builder(name)
                .description(description)
                .register(meterRegistry);
    }

    /**
     * 获取或创建带标签的计数器
     */
    private Counter getOrCreateCounter(String name, String description, Iterable<Tag> tags) {
        String cacheKey = buildCacheKey(name, tags);
        return counterCache.computeIfAbsent(cacheKey, k ->
                Counter.builder(name)
                        .description(description)
                        .tags(tags)
                        .register(meterRegistry)
        );
    }

    /**
     * 构建缓存键
     */
    private String buildCacheKey(String name, Iterable<Tag> tags) {
        StringBuilder keyBuilder = new StringBuilder(name);
        for (Tag tag : tags) {
            keyBuilder.append("|").append(tag.getKey()).append("=").append(tag.getValue());
        }
        return keyBuilder.toString();
    }

    /**
     * 记录请求
     */
    public void recordRequest(String path, String method) {
        Tags tags = Tags.of(
                Tag.of("path", sanitizeTag(path)),
                Tag.of("method", method)
        );
        getOrCreateCounter("gateway.requests.total", "Total number of requests", tags).increment();
        totalRequests.increment();
    }

    /**
     * 记录成功请求
     */
    public void recordSuccess(String path, String method) {
        Tags tags = Tags.of(
                Tag.of("path", sanitizeTag(path)),
                Tag.of("method", method)
        );
        getOrCreateCounter("gateway.requests.success", "Number of successful requests", tags).increment();
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
        getOrCreateCounter("gateway.requests.error", "Number of error requests", tags).increment();
    }

    /**
     * 记录超时请求
     */
    public void recordTimeout(String path, String method) {
        Tags tags = Tags.of(
                Tag.of("path", sanitizeTag(path)),
                Tag.of("method", method)
        );
        getOrCreateCounter("gateway.requests.timeout", "Number of timeout requests", tags).increment();
    }

    /**
     * 记录鉴权错误
     */
    public void recordAuthError(String path, String reason) {
        Tags tags = Tags.of(
                Tag.of("path", sanitizeTag(path)),
                Tag.of("reason", reason)
        );
        getOrCreateCounter("gateway.requests.auth_error", "Number of authentication errors", tags).increment();
    }

    /**
     * 记录限流
     */
    public void recordRateLimit(String path, String limitType) {
        Tags tags = Tags.of(
                Tag.of("path", sanitizeTag(path)),
                Tag.of("limit_type", limitType)
        );
        getOrCreateCounter("gateway.requests.rate_limited", "Number of rate limited requests", tags).increment();
    }

    /**
     * 记录响应时间
     */
    public void recordResponseTime(String path, String method, long responseTimeMs) {
        totalResponseTime.add(responseTimeMs);
        totalRequests.increment();

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
        double reqSum = totalRequests.sum();
        return new MetricsSummary(
                (long) reqSum,
                successCounter.count(),
                errorCounter.count(),
                timeoutCounter.count(),
                authErrorCounter.count(),
                rateLimitCounter.count(),
                reqSum > 0 ? totalResponseTime.sum() / reqSum : 0
        );
    }

    /**
     * 清除计数器缓存（用于测试或重置）
     */
    public void clearCounterCache() {
        counterCache.clear();
        log.info("Counter cache cleared");
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
