package com.zzl.platform.common.core.util;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.Optional;

/**
 * HTTP客户端工具类
 * 提供便捷的HTTP请求封装
 */
@Slf4j
public class HttpClientUtils {

    private HttpClientUtils() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * GET请求
     *
     * @param url 请求地址
     * @return 响应体字符串
     */
    public static String get(String url) {
        return get(url, null, null);
    }

    /**
     * GET请求
     *
     * @param url    请求地址
     * @param params 请求参数
     * @return 响应体字符串
     */
    public static String get(String url, Map<String, ?> params) {
        return get(url, params, null);
    }

    /**
     * GET请求
     *
     * @param url     请求地址
     * @param params  请求参数
     * @param headers 请求头
     * @return 响应体字符串
     */
    public static String get(String url, Map<String, ?> params, Map<String, String> headers) {
        try {
            RestTemplate restTemplate = getRestTemplate();
            String fullUrl = buildUrl(url, params);
            HttpEntity<String> entity = new HttpEntity<>(buildHeaders(headers));
            ResponseEntity<String> response = restTemplate.exchange(fullUrl, HttpMethod.GET, entity, String.class);
            return response.getBody();
        } catch (RestClientException e) {
            log.error("GET请求失败: url={}, error={}", url, e.getMessage());
            throw new HttpClientException("GET请求失败: " + url, e);
        }
    }

    /**
     * POST请求
     *
     * @param url  请求地址
     * @param body 请求体
     * @return 响应体字符串
     */
    public static String post(String url, Object body) {
        return post(url, body, null);
    }

    /**
     * POST请求
     *
     * @param url     请求地址
     * @param body    请求体
     * @param headers 请求头
     * @return 响应体字符串
     */
    public static String post(String url, Object body, Map<String, String> headers) {
        try {
            RestTemplate restTemplate = getRestTemplate();
            HttpEntity<Object> entity = new HttpEntity<>(body, buildHeaders(headers));
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            return response.getBody();
        } catch (RestClientException e) {
            log.error("POST请求失败: url={}, error={}", url, e.getMessage());
            throw new HttpClientException("POST请求失败: " + url, e);
        }
    }

    /**
     * PUT请求
     *
     * @param url  请求地址
     * @param body 请求体
     * @return 响应体字符串
     */
    public static String put(String url, Object body) {
        return put(url, body, null);
    }

    /**
     * PUT请求
     *
     * @param url     请求地址
     * @param body    请求体
     * @param headers 请求头
     * @return 响应体字符串
     */
    public static String put(String url, Object body, Map<String, String> headers) {
        try {
            RestTemplate restTemplate = getRestTemplate();
            HttpEntity<Object> entity = new HttpEntity<>(body, buildHeaders(headers));
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.PUT, entity, String.class);
            return response.getBody();
        } catch (RestClientException e) {
            log.error("PUT请求失败: url={}, error={}", url, e.getMessage());
            throw new HttpClientException("PUT请求失败: " + url, e);
        }
    }

    /**
     * DELETE请求
     *
     * @param url 请求地址
     * @return 响应体字符串
     */
    public static String delete(String url) {
        return delete(url, null, null);
    }

    /**
     * DELETE请求
     *
     * @param url     请求地址
     * @param params  请求参数
     * @param headers 请求头
     * @return 响应体字符串
     */
    public static String delete(String url, Map<String, ?> params, Map<String, String> headers) {
        try {
            RestTemplate restTemplate = getRestTemplate();
            String fullUrl = buildUrl(url, params);
            HttpEntity<String> entity = new HttpEntity<>(buildHeaders(headers));
            ResponseEntity<String> response = restTemplate.exchange(fullUrl, HttpMethod.DELETE, entity, String.class);
            return response.getBody();
        } catch (RestClientException e) {
            log.error("DELETE请求失败: url={}, error={}", url, e.getMessage());
            throw new HttpClientException("DELETE请求失败: " + url, e);
        }
    }

    /**
     * 获取RestTemplate实例
     */
    private static RestTemplate getRestTemplate() {
        return Optional.ofNullable(SpringContextUtils.getBean(RestTemplate.class))
                .orElseThrow(() -> new HttpClientException("RestTemplate未配置，请确保已引入RestTemplateConfig"));
    }

    /**
     * 构建完整URL
     */
    private static String buildUrl(String url, Map<String, ?> params) {
        if (params == null || params.isEmpty()) {
            return url;
        }
        StringBuilder sb = new StringBuilder(url);
        if (!url.contains("?")) {
            sb.append("?");
        } else {
            sb.append("&");
        }
        params.forEach((key, value) -> {
            if (value != null) {
                sb.append(key).append("=").append(value.toString()).append("&");
            }
        });
        String result = sb.toString();
        return result.endsWith("&") ? result.substring(0, result.length() - 1) : result;
    }

    /**
     * 构建请求头
     */
    private static HttpHeaders buildHeaders(Map<String, String> headers) {
        HttpHeaders httpHeaders = new HttpHeaders();
        if (headers != null) {
            headers.forEach((key, value) -> {
                if (StrUtil.isNotBlank(key) && StrUtil.isNotBlank(value)) {
                    httpHeaders.add(key, value);
                }
            });
        }
        return httpHeaders;
    }

    /**
     * HTTP客户端异常
     */
    public static class HttpClientException extends RuntimeException {
        public HttpClientException(String message) {
            super(message);
        }

        public HttpClientException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
