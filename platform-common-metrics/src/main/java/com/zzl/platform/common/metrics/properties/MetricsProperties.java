package com.zzl.platform.common.metrics.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 指标配置属性
 *
 * @author zhouyp
 * @since 2026-04-16
 */
@Data
@ConfigurationProperties(prefix = "platform.metrics")
public class MetricsProperties {

    /**
     * 是否启用指标采集
     */
    private boolean enabled = true;

    /**
     * 指标前缀
     */
    private String prefix = "";

    /**
     * 是否启用详细指标（包含路径、方法等标签）
     * 注意：开启详细指标可能会导致基数爆炸
     */
    private boolean detailedEnabled = false;

    /**
     * 标签最大长度
     */
    private int tagMaxLength = 50;

    /**
     * 是否启用百分位直方图
     */
    private boolean percentileHistogram = true;
}
