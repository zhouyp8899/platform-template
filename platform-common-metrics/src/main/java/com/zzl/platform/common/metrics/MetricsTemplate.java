package com.zzl.platform.common.metrics;

import io.micrometer.core.instrument.*;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.LongAdder;

/**
 * 指标模板类
 * 提供统一的指标采集能力，支持计数器、仪表盘、响应时间统计等
 * 使用 LongAdder 优化高并发场景下的计数性能
 *
 * @author zhouyp
 * @since 2026-04-16
 */
@Slf4j
public class MetricsTemplate {

    private final MeterRegistry meterRegistry;
    private final String metricsPrefix;

    // 计数器缓存（避免重复创建）
    private final ConcurrentMap<String, Counter> counterCache = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, DistributionSummary> summaryCache = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Gauge> gaugeCache = new ConcurrentHashMap<>();

    /**
     * 构造函数
     *
     * @param meterRegistry Micrometer注册中心
     * @param metricsPrefix 指标前缀，用于区分不同模块的指标
     */
    public MetricsTemplate(MeterRegistry meterRegistry, String metricsPrefix) {
        this.meterRegistry = meterRegistry;
        this.metricsPrefix = metricsPrefix != null ? metricsPrefix : "";
        log.info("MetricsTemplate initialized with prefix: {}", this.metricsPrefix);
    }

    /**
     * 构造函数（无前缀）
     */
    public MetricsTemplate(MeterRegistry meterRegistry) {
        this(meterRegistry, "");
    }

    /**
     * 清理标签（防止标签值过长或包含特殊字符）
     *
     * @param tag 原始标签值
     * @return 清理后的标签值
     */
    public static String sanitizeTag(String tag) {
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
     * 创建或获取计数器
     *
     * @param name        指标名称
     * @param description 指标描述
     * @param tags        标签
     * @return Counter实例
     */
    public Counter getCounter(String name, String description, Iterable<Tag> tags) {
        String cacheKey = buildCacheKey(name, tags);
        return counterCache.computeIfAbsent(cacheKey, k ->
                Counter.builder(buildMetricName(name))
                        .description(description)
                        .tags(tags)
                        .register(meterRegistry)
        );
    }

    /**
     * 创建或获取计数器（无标签）
     */
    public Counter getCounter(String name, String description) {
        return counterCache.computeIfAbsent(name, k ->
                Counter.builder(buildMetricName(name))
                        .description(description)
                        .register(meterRegistry)
        );
    }

    /**
     * 记录计数器增量
     *
     * @param name        指标名称
     * @param description 指标描述
     * @param tags        标签
     * @param amount      增量值
     */
    public void incrementCounter(String name, String description, Iterable<Tag> tags, double amount) {
        getCounter(name, description, tags).increment(amount);
    }

    /**
     * 记录计数器增量（默认增1）
     */
    public void incrementCounter(String name, String description, Iterable<Tag> tags) {
        incrementCounter(name, description, tags, 1.0);
    }

    /**
     * 创建或获取分布摘要（用于统计响应时间等）
     *
     * @param name        指标名称
     * @param description 指标描述
     * @param tags        标签
     * @return DistributionSummary实例
     */
    public DistributionSummary getDistributionSummary(String name, String description, Iterable<Tag> tags) {
        String cacheKey = buildCacheKey(name, tags);
        return summaryCache.computeIfAbsent(cacheKey, k ->
                DistributionSummary.builder(buildMetricName(name))
                        .description(description)
                        .tags(tags)
                        .publishPercentileHistogram()
                        .register(meterRegistry)
        );
    }

    /**
     * 记录分布值（如响应时间）
     *
     * @param name        指标名称
     * @param description 指标描述
     * @param tags        标签
     * @param value       要记录的值
     */
    public void recordDistribution(String name, String description, Iterable<Tag> tags, double value) {
        getDistributionSummary(name, description, tags).record(value);
    }

    /**
     * 基于LongAdder注册仪表盘指标
     */
    public Gauge registerGauge(String name, String description, Iterable<Tag> tags, LongAdder longAdder) {
        String cacheKey = buildCacheKey(name, tags);
        return gaugeCache.computeIfAbsent(cacheKey, k ->
                Gauge.builder(buildMetricName(name), longAdder, LongAdder::sum)
                        .description(description)
                        .tags(tags)
                        .register(meterRegistry)
        );
    }

    /**
     * 基于AtomicReference注册仪表盘指标
     */
    public <T extends Number> Gauge registerGauge(String name, String description, Iterable<Tag> tags, AtomicReference<T> reference) {
        String cacheKey = buildCacheKey(name, tags);
        return gaugeCache.computeIfAbsent(cacheKey, k ->
                Gauge.builder(buildMetricName(name), reference, ref ->
                                ref.get() != null ? ref.get().doubleValue() : 0)
                        .description(description)
                        .tags(tags)
                        .register(meterRegistry)
        );
    }

    /**
     * 注册平均值仪表盘指标（基于两个LongAdder：总值和计数）
     */
    public Gauge registerAverageGauge(String name, String description, Iterable<Tag> tags,
                                      LongAdder totalValue, LongAdder count) {
        String cacheKey = buildCacheKey(name, tags);
        return gaugeCache.computeIfAbsent(cacheKey, k ->
                Gauge.builder(buildMetricName(name), new Object() {
                            public double getAverage() {
                                long cnt = count.sum();
                                return cnt > 0 ? totalValue.sum() / cnt : 0;
                            }
                        }, obj -> obj.getAverage())
                        .description(description)
                        .tags(tags)
                        .register(meterRegistry)
        );
    }

    /**
     * 构建完整的指标名称（带前缀）
     */
    private String buildMetricName(String name) {
        return metricsPrefix.isEmpty() ? name : metricsPrefix + "." + name;
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
     * 获取MeterRegistry实例
     */
    public MeterRegistry getMeterRegistry() {
        return meterRegistry;
    }

    /**
     * 清除缓存（用于测试或重置）
     */
    public void clearCache() {
        counterCache.clear();
        summaryCache.clear();
        gaugeCache.clear();
        log.info("MetricsTemplate cache cleared");
    }
}
