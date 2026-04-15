package com.zzl.platform.gw.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Gateway核心配置
 * 配置服务发现、路由规则、全局错误处理等
 */
@Slf4j
@Configuration
public class GatewayConfig {

    /**
     * 配置路由规则
     * 基于Nacos服务发现动态路由
     */
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // 认证服务路由
                .route(r -> r.path("/auth-service/**")
                        .filters(f -> f
                                // 移除路径前缀
                                .stripPrefix(1)
                        )
                        .uri("lb://platform-auth-service"))
                // 演示路由：可以在此添加更多服务路由
                .route(r -> r.path("/demo/**")
                        .filters(f -> f.stripPrefix(1))
                        .uri("lb://platform-demo-service"))
                .build();
    }
}
