package com.zzl.platform.gw.config;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Gateway跨域配置
 * 处理跨域请求，支持前后端分离场景
 */
@Configuration
public class CorsConfig {

    /**
     * 跨域过滤器
     * 为所有响应添加CORS头
     */
    @Bean
    public GlobalFilter corsFilter() {
        return new GlobalFilter() {
            public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
                ServerHttpRequest request = exchange.getRequest();

                // 如果是OPTIONS预检请求，直接返回成功
                if ("OPTIONS".equals(request.getMethod().name())) {
                    ServerHttpResponse response = exchange.getResponse();
                    response.getHeaders().add("Access-Control-Allow-Origin", "*");
                    response.getHeaders().add("Access-Control-Allow-Methods", "*");
                    response.getHeaders().add("Access-Control-Allow-Headers", "*");
                    response.getHeaders().add("Access-Control-Max-Age", "7200");
                    response.getHeaders().add("Access-Control-Allow-Credentials", "true");
                    response.setStatusCode(org.springframework.http.HttpStatus.OK);
                    return response.setComplete();
                }

                return chain.filter(exchange).then(Mono.fromRunnable(() -> {
                    HttpHeaders headers = exchange.getResponse().getHeaders();
                    headers.add("Access-Control-Allow-Origin", "*");
                    headers.add("Access-Control-Allow-Methods", "*");
                    headers.add("Access-Control-Allow-Headers", "*");
                    headers.add("Access-Control-Allow-Credentials", "true");
                }));
            }

            public int getOrder() {
                // 优先级最高，确保在其他过滤器之前处理
                return Ordered.HIGHEST_PRECEDENCE;
            }
        };
    }
}
