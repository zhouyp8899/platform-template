package com.zzl.platform.common.core.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zzl.platform.common.core.exception.BusinessException;
import com.zzl.platform.common.core.res.Result;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Feign统一错误解码器
 * 将服务端的错误响应转换为统一的异常处理
 */
@Slf4j
public class FeignErrorDecoder implements ErrorDecoder {

    private final ObjectMapper objectMapper;

    public FeignErrorDecoder() {
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public Exception decode(String methodKey, Response response) {
        String body = readBody(response);

        // 尝试解析为Result格式
        try {
            Result<?> result = objectMapper.readValue(body, Result.class);
            if (result != null && result.getCode() != null) {
                return new BusinessException(result.getCode(), result.getMessage());
            }
        } catch (Exception ignored) {
            // 无法解析为Result，使用默认处理
        }

        // 根据HTTP状态码构建异常
        switch (response.status()) {
            case 400:
                return new BusinessException(400, "请求参数错误: " + body);
            case 401:
                return new BusinessException(401, "未授权: " + body);
            case 403:
                return new BusinessException(403, "禁止访问: " + body);
            case 404:
                return new BusinessException(404, "资源不存在: " + body);
            case 500:
                return new BusinessException(500, "服务器内部错误: " + body);
            case 502:
                return new BusinessException(502, "网关错误: " + body);
            case 503:
                return new BusinessException(503, "服务不可用: " + body);
            default:
                return new BusinessException(response.status(), "远程调用失败: " + body);
        }
    }

    /**
     * 读取响应体
     */
    private String readBody(Response response) {
        try {
            if (response.body() != null) {
                return new String(response.body().asInputStream().readAllBytes(), StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            log.warn("读取Feign响应体失败", e);
        }
        return "Unknown error";
    }
}
