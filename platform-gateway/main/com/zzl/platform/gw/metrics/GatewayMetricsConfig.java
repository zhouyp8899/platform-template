package com.zzl.platform.gw.metrics;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

/**
 * Gateway监控指标配置
 * 使用公共metrics模块的自动配置，简化配置逻辑
 * 指标采集默认启用，可通过配置关闭
 *
 * @author zhouyp
 * @since 2026-04-16
 */
@Slf4j
@Configuration
@ConditionalOnProperty(prefix = "platform.gateway", name = "metrics-enabled", havingValue = "true", matchIfMissing = true)
public class GatewayMetricsConfig {

    /**
     * GatewayMetrics由MetricsAutoConfiguration自动配置创建
     * MetricsTemplate会自动注入到GatewayMetrics中
     *
     * 配置说明：
     * - platform.gateway.metrics-enabled=false 时禁用gateway指标
     * - platform.metrics.enabled=false 时禁用整个metrics模块
     */
    public GatewayMetricsConfig() {
        log.info("Gateway metrics configuration loaded");
    }
}
