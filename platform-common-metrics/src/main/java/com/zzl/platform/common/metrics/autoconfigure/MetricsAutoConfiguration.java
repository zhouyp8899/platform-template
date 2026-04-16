package com.zzl.platform.common.metrics.autoconfigure;

import com.zzl.platform.common.metrics.MetricsTemplate;
import com.zzl.platform.common.metrics.properties.MetricsProperties;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 指标自动配置类
 * 根据配置自动创建MetricsTemplate实例
 *
 * @author zhouyp
 * @since 2026-04-16
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(MetricsProperties.class)
@ConditionalOnProperty(prefix = "platform.metrics", name = "enabled", havingValue = "true", matchIfMissing = true)
public class MetricsAutoConfiguration {

    /**
     * 创建默认MetricsTemplate Bean
     */
    @Bean
    @ConditionalOnBean(MeterRegistry.class)
    public MetricsTemplate metricsTemplate(MeterRegistry meterRegistry, MetricsProperties metricsProperties) {
        log.info("Initializing MetricsTemplate with prefix: {}, detailed: {}",
                metricsProperties.getPrefix(), metricsProperties.isDetailedEnabled());
        return new MetricsTemplate(meterRegistry, metricsProperties.getPrefix());
    }
}
