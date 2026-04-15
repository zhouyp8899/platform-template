package com.zzl.platform.gw.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Gateway监控指标配置
 * 集成Micrometer，提供请求量、响应时间、错误率等指标
 */
@Slf4j
@Configuration
@ConditionalOnProperty(prefix = "platform.gateway", name = "metrics-enabled", havingValue = "true", matchIfMissing = true)
public class GatewayMetricsConfig {

    /**
     * 创建自定义指标
     */
    @Bean
    public GatewayMetrics gatewayMetrics(MeterRegistry meterRegistry) {
        return new GatewayMetrics(meterRegistry);
    }
}
