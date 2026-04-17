package com.zzl.platform.gw.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Gateway核心配置
 * 配置服务发现、路由规则、全局错误处理等
 * 注意：路由配置已迁移到application-gateway.yml，此类保留用于扩展
 */
@Slf4j
@Configuration
public class GatewayConfig {

    /**
     * 配置路由规则
     * 注意：主要路由配置已迁移到application-gateway.yml
     * 此方法仅用于演示或需要特殊逻辑的路由
     */
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        log.info("初始化Gateway路由配置");

        return builder.routes()
                // 演示路由：可以在此添加需要特殊逻辑的路由
                .route(r -> r.path("/demo/**")
                        .filters(f -> f.stripPrefix(1))
                        .uri("lb://platform-demo-service"))
                .build();
    }
}
