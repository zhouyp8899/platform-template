package com.zzl.platform.gw.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

/**
 * 统一返回格式过滤器
 * 包装下游服务的响应，统一返回格式
 */
@Slf4j
@Component
public class ResponseWrapperWrapperFilter extends AbstractGatewayFilter {

    @Override
    protected String getFilterName() {
        return "ResponseWrapperFilter";
    }

    @Override
    protected Mono<Void> doFilter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpResponseDecorator decoratedResponse = new ServerHttpResponseDecorator(exchange.getResponse()) {
            @Override
            public Mono<Void> writeWith(org.reactivestreams.Publisher<? extends DataBuffer> body) {
                if (body instanceof Flux) {
                    Flux<? extends DataBuffer> fluxBody = (Flux<? extends DataBuffer>) body;

                    return super.writeWith(fluxBody.buffer().map(dataBuffers -> {
                        // 合并所有DataBuffer
                        DataBufferFactory bufferFactory = exchange.getResponse().bufferFactory();
                        DataBuffer join = bufferFactory.join(dataBuffers);
                        byte[] content = new byte[join.readableByteCount()];
                        join.read(content);
                        DataBufferUtils.release(join);

                        String originalResponse = new String(content, StandardCharsets.UTF_8);

                        // 包装响应
                        String wrappedResponse = wrapResponse(exchange, originalResponse);

                        log.debug("Wrapped response: {}", wrappedResponse);

                        return bufferFactory.wrap(wrappedResponse.getBytes(StandardCharsets.UTF_8));
                    }));
                }
                return super.writeWith(body);
            }
        };

        return chain.filter(exchange.mutate().response(decoratedResponse).build());
    }

    /**
     * 包装响应
     */
    private String wrapResponse(ServerWebExchange exchange, String originalResponse) {
        // 如果已经是标准格式，直接返回
        if (isStandardResponse(originalResponse)) {
            return originalResponse;
        }

        String traceId = getTraceId(exchange);

        try {
            // 尝试解析为JSON
            if (originalResponse.trim().startsWith("{")) {
                // 包装为标准格式
                return String.format(
                        "{\"code\":200,\"message\":\"success\",\"data\":%s,\"traceId\":\"%s\"}",
                        originalResponse,
                        traceId
                );
            } else {
                // 非JSON响应，直接返回（不包装）
                return originalResponse;
            }
        } catch (Exception e) {
            log.error("Failed to wrap response, returning original", e);
            // 包装失败时返回原始响应，避免丢失下游服务的真实响应
            return originalResponse;
        }
    }

    /**
     * 判断是否为标准响应格式
     */
    private boolean isStandardResponse(String response) {
        if (response == null || response.isEmpty()) {
            return false;
        }
        return response.contains("\"code\":") && response.contains("\"message\":");
    }

    @Override
    public int getOrder() {
        return 300;
    }
}
