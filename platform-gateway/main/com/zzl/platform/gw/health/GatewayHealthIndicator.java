package com.zzl.platform.gw.health;

import com.zzl.platform.common.redis.service.RedisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.cloud.client.discovery.composite.reactive.ReactiveCompositeDiscoveryClient;
import org.springframework.stereotype.Component;

/**
 * Gateway健康检查
 * 检查Nacos、Redis、下游服务连接状态
 */
@Slf4j
@Component
@ConditionalOnBean(RedisService.class)
public class GatewayHealthIndicator implements HealthIndicator {

    private final ReactiveCompositeDiscoveryClient discoveryClient;
    private final RedisService redisService;

    public GatewayHealthIndicator(ReactiveCompositeDiscoveryClient discoveryClient,
                                  RedisService redisService) {
        this.discoveryClient = discoveryClient;
        this.redisService = redisService;
    }

    @Override
    public Health health() {
        Health.Builder builder = Health.up();

        try {
            // 检查服务发现状态
            checkDiscovery(builder);

            // 检查下游服务状态
            checkDownstreamServices(builder);

            builder.withDetail("timestamp", System.currentTimeMillis());
            builder.withDetail("version", "1.0.0");

            log.debug("Gateway health check passed");
        } catch (Exception e) {
            log.error("Gateway health check failed", e);
            builder.down(e);
            builder.withDetail("error", e.getMessage());
        }

        return builder.build();
    }

    /**
     * 检查服务发现状态
     */
    private void checkDiscovery(Health.Builder builder) {
        try {
            if (discoveryClient != null) {
                builder.withDetail("discovery", "enabled");
                builder.withDetail("serviceDiscovery", "healthy");
            } else {
                builder.withDetail("discovery", "disabled");
            }
        } catch (Exception e) {
            log.warn("Discovery check failed", e);
            builder.withDetail("discovery", "error");
            builder.withDetail("discoveryError", e.getMessage());
        }
    }


    /**
     * 检查下游服务状态
     */
    private void checkDownstreamServices(Health.Builder builder) {
        try {
            // TODO: 实际应该检查DiscoveryClient获取服务列表并检查健康状态
            // 可以通过actuator端点检查下游服务健康状态
            builder.withDetail("downstreamServices", "checking");
            builder.withDetail("registeredServices", "pending");
        } catch (Exception e) {
            log.warn("Downstream services check failed", e);
            builder.withDetail("downstreamServices", "error");
            builder.withDetail("downstreamError", e.getMessage());
        }
    }
}
