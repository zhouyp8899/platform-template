package com.zzl.platform.gw.metrics;

import com.zzl.platform.common.metrics.MetricsTemplate;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.LongAdder;

/**
 * Gateway监控指标
 * 提供请求量、响应时间、错误率等监控指标
 * 基于MetricsTemplate构建，消除重复代码，提高可维护性
 *
 * @author zhouyp
 * @since 2026-04-16
 */
@Slf4j
@Component
public class GatewayMetrics {

    private final MetricsTemplate metricsTemplate;

    // 响应时间统计（使用 LongAdder 优化高并发性能）
    private final LongAdder totalResponseTime = new LongAdder();
    private final LongAdder totalRequests = new LongAdder();
    private final AtomicReference<Long> maxResponseTime = new AtomicReference<>(0L);

    /**
     * 构造函数，注入MetricsTemplate
     */
    @Autowired
    public GatewayMetrics(MetricsTemplate metricsTemplate) {
        this.metricsTemplate = metricsTemplate;

        // 注册响应时间相关的Gauge指标
        metricsTemplate.registerAverageGauge("response.time.avg", "Average response time in milliseconds",
                Tags.empty(), totalResponseTime, totalRequests);
        metricsTemplate.registerGauge("response.time.max", "Maximum response time in milliseconds",
                Tags.empty(), maxResponseTime);
        metricsTemplate.registerGauge("requests.count", "Total request count",
                Tags.empty(), totalRequests);

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
        metricsTemplate.incrementCounter("requests.total", "Total number of requests", tags);
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
        metricsTemplate.incrementCounter("requests.success", "Number of successful requests", tags);
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
        metricsTemplate.incrementCounter("requests.error", "Number of error requests", tags);
    }

    /**
     * 记录超时请求
     */
    public void recordTimeout(String path, String method) {
        Tags tags = Tags.of(
                Tag.of("path", sanitizeTag(path)),
                Tag.of("method", method)
        );
        metricsTemplate.incrementCounter("requests.timeout", "Number of timeout requests", tags);
    }

    /**
     * 记录鉴权错误
     */
    public void recordAuthError(String path, String reason) {
        Tags tags = Tags.of(
                Tag.of("path", sanitizeTag(path)),
                Tag.of("reason", reason)
        );
        metricsTemplate.incrementCounter("requests.auth_error", "Number of authentication errors", tags);
    }

    /**
     * 记录限流

     */
    public void recordRateLimit(String path, String limitType) {
        Tags tags = Tags.of(
                Tag.of("path", sanitizeTag(path)),
                Tag.of("limit_type", limitType)
        );
        metricsTemplate.incrementCounter("requests.rate_limited", "Number of rate limited requests", tags);
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
     * 清理标签（使用MetricsTemplate的静态方法）
     */
    private String sanitizeTag(String tag) {
        return MetricsTemplate.sanitizeTag(tag);
    }

    /**
     * 获取指标摘要
     * 注意：由于使用了带标签的计数器，这里只返回基础统计信息
     */
    public MetricsSummary getSummary() {
        double reqSum = totalRequests.sum();
        return new MetricsSummary(
                (long) reqSum,
                0.0,  // success count stored in tagged counters
                0.0,  // error count stored in tagged counters
                0.0,  // timeout count stored in tagged counters
                0.0,  // auth error count stored in tagged counters
                0.0,  // rate limit count stored in tagged counters
                reqSum > 0 ? totalResponseTime.sum() / reqSum : 0
        );
    }

    /**
     * 获取MetricsTemplate实例（用于扩展）
     */
    public MetricsTemplate getMetricsTemplate() {
        return metricsTemplate;
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
