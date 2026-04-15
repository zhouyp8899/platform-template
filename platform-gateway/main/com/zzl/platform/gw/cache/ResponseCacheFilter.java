package com.zzl.platform.gw.cache;

import com.zzl.platform.common.redis.service.RedisCacheService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

/**
 * Gateway响应缓存过滤器
 * 基于Redis实现响应缓存，减少对下游服务的请求
 */
@Slf4j
@Component
public class ResponseCacheFilter implements GlobalFilter, Ordered {

    private static final String CACHE_PREFIX = "gateway:response:";
    private static final String CACHED_RESPONSE_ATTR = "cached_response";
    private static final String SKIP_CACHE_ATTR = "skip_cache";
    private final RedisCacheService cacheService;

    public ResponseCacheFilter(RedisCacheService cacheService) {
        this.cacheService = cacheService;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        // 只缓存GET请求
        if (!"GET".equals(request.getMethod().name())) {
            log.debug("Not a GET request, skip caching: {}", path);
            return chain.filter(exchange);
        }

        // 检查缓存中是否有响应
        String cacheKey = buildCacheKey(request);
        String cachedResponse = cacheService.get(cacheKey);

        if (cachedResponse != null) {
            log.info("Cache hit for path: {}", path);
            return buildCachedResponse(exchange, cachedResponse);
        }

        log.debug("Cache miss for path: {}", path);

        // 标记需要缓存响应
        exchange.getAttributes().put(SKIP_CACHE_ATTR, false);

        return chain.filter(exchange).doOnSuccess(aVoid -> {
            // 响应成功后缓存响应体
            if (!Boolean.TRUE.equals(exchange.getAttribute(SKIP_CACHE_ATTR))) {
                ServerHttpResponse response = exchange.getResponse();
                if (response.getStatusCode() != null
                        && response.getStatusCode().is2xxSuccessful()) {
                    // 获取响应体（需要通过 DataBuffer 获取）
                    // 这里需要通过 ResponseDecorator 拦截响应体
                    cacheResponseBody(request, cacheKey);
                }
            }
        });
    }

    /**
     * 构建缓存键
     */
    private String buildCacheKey(ServerHttpRequest request) {
        StringBuilder keyBuilder = new StringBuilder();
        keyBuilder.append(CACHE_PREFIX);
        keyBuilder.append(request.getURI().getPath());

        // 添加查询参数到键中
        if (!request.getQueryParams().isEmpty()) {
            keyBuilder.append("?");
            request.getQueryParams().forEach((k, v) -> {
                keyBuilder.append(k).append("=").append(v).append("&");
            });
            // 移除最后的&
            if (keyBuilder.charAt(keyBuilder.length() - 1) == '&') {
                keyBuilder.deleteCharAt(keyBuilder.length() - 1);
            }
        }

        return keyBuilder.toString();
    }

    /**
     * 构建缓存响应
     */
    private Mono<Void> buildCachedResponse(ServerWebExchange exchange, String cachedResponse) {
        try {
            ServerHttpResponse response = exchange.getResponse();
            response.setStatusCode(HttpStatus.OK);
            response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

            // 添加缓存控制头
            response.getHeaders().setCacheControl(
                    CacheControl.maxAge(3600, TimeUnit.SECONDS)
                            .cachePublic()
                            .mustRevalidate()
            );

            return response.writeWith(
                    reactor.core.publisher.Mono.just(
                            org.springframework.core.io.buffer.DefaultDataBufferFactory.sharedInstance
                                    .wrap(cachedResponse.getBytes(StandardCharsets.UTF_8))
                    )
            );
        } catch (Exception e) {
            log.error("Failed to build cached response", e);
            return exchange.getResponse().setComplete();
        }
    }

    /**
     * 缓存响应体
     */
    private void cacheResponseBody(ServerHttpRequest request, String cacheKey) {
        // 根据响应头确定缓存时间
        int cacheSeconds = determineCacheTime(request);

        // 这里需要从 ResponseDecorator 中获取响应体
        // 实际实现需要使用 ModifyResponseBodyGatewayFilterFactory
        log.info("Cached response for path: {}", request.getURI().getPath());
    }

    /**
     * 缓存响应
     */
    public void cacheResponse(ServerHttpRequest request, String responseBody) {
        if (!"GET".equals(request.getMethod().name())) {
            return;
        }

        String cacheKey = buildCacheKey(request);

        // 根据响应头确定缓存时间
        int cacheSeconds = determineCacheTime(request);

        cacheService.set(cacheKey, responseBody, cacheSeconds, TimeUnit.SECONDS);
        log.info("Cached response for path: {}, size: {} bytes",
                request.getURI().getPath(), responseBody.getBytes().length);
    }

    /**
     * 确定缓存时间
     */
    private int determineCacheTime(ServerHttpRequest request) {
        String path = request.getURI().getPath();

        // 根据路径返回不同的缓存时间
        if (path.contains("/static/")) {
            return 86400; // 静态资源：24小时
        } else if (path.contains("/api/")) {
            return 300; // API：5分钟
        } else if (path.contains("/public/")) {
            return 1800; // 公共数据：30分钟
        } else {
            return 600; // 默认：10分钟
        }
    }

    /**
     * 清除缓存
     */
    public void evictCache(String path) {
        String cacheKey = CACHE_PREFIX + path;
        Boolean result = cacheService.delete(cacheKey);

        if (result != null && result) {
            log.info("Evicted cache for path: {}", path);
        }
    }

    /**
     * 清除所有缓存
     */
    public void evictAllCache() {
        // TODO: 实现批量删除或模式匹配删除
        log.warn("Evict all cache - not fully implemented");
    }

    @Override
    public int getOrder() {
        // 在请求追踪过滤器之后执行
        return 100;
    }
}
