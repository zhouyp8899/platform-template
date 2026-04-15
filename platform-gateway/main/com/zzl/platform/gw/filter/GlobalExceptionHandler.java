package com.zzl.platform.gw.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Gateway全局异常处理器
 * 捕获所有异常并返回统一的错误格式
 */
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@Component
public class GlobalExceptionHandler implements ErrorWebExceptionHandler {

    private static final DateTimeFormatter TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        String message = "Internal server error";
        String errorCode = "500";
        String traceId = generateTraceId();

        log.error("Gateway error, traceId: {}, path: {}, error: {}",
                traceId, exchange.getRequest().getURI().getPath(), ex.getMessage(), ex);

        // 根据异常类型返回不同的状态码
        if (ex instanceof ResponseStatusException rse) {
            status = (HttpStatus) rse.getStatusCode();
            message = rse.getReason() != null ? rse.getReason() : status.getReasonPhrase();
            errorCode = String.valueOf(status.value());
        } else if (ex instanceof org.springframework.cloud.gateway.support.NotFoundException) {
            status = HttpStatus.NOT_FOUND;
            message = "Service not found";
            errorCode = "404";
        } else if (ex instanceof java.util.concurrent.TimeoutException) {
            status = HttpStatus.GATEWAY_TIMEOUT;
            message = "Gateway timeout";
            errorCode = "504";
        } else if (ex instanceof org.springframework.web.reactive.function.client.WebClientRequestException) {
            status = HttpStatus.BAD_GATEWAY;
            message = "Bad gateway - failed to connect to downstream service";
            errorCode = "502";
        } else if (ex instanceof org.springframework.web.reactive.function.client.WebClientResponseException wcre) {
            status = HttpStatus.valueOf(wcre.getStatusCode().value());
            message = wcre.getMessage();
            errorCode = String.valueOf(wcre.getStatusCode().value());
        } else if (ex instanceof org.springframework.web.server.ServerWebInputException) {
            status = HttpStatus.BAD_REQUEST;
            message = "Invalid request";
            errorCode = "400";
        } else if (ex instanceof org.springframework.web.server.MethodNotAllowedException) {
            status = HttpStatus.METHOD_NOT_ALLOWED;
            message = "Method not allowed";
            errorCode = "405";
        }

        // 构造错误响应
        ErrorResponse errorResponse = new ErrorResponse(
                Integer.parseInt(errorCode),
                message,
                null,
                traceId,
                LocalDateTime.now().format(TIME_FORMATTER)
        );

        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        try {
            String body = objectMapper.writeValueAsString(errorResponse);
            DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(body.getBytes());
            return exchange.getResponse().writeWith(Mono.just(buffer));
        } catch (Exception e) {
            log.error("Failed to write error response", e);
            return Mono.empty();
        }
    }

    /**
     * 生成TraceId
     */
    private String generateTraceId() {
        return java.util.UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 错误响应
     */
    private record ErrorResponse(
            int code,
            String message,
            Object data,
            String traceId,
            String timestamp) {
    }
}
