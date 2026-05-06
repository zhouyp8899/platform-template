package com.zzl.platform.gw.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Gateway配置属性
 * 用于从配置文件读取Gateway相关配置
 */
@Data
@Component("platformGatewayProperties")
@ConfigurationProperties(prefix = "platform.gateway")
public class GatewayProperties {

    /**
     * 是否启用鉴权
     */
    private boolean authEnabled = true;

    /**
     * 是否启用限流
     */
    private boolean rateLimitEnabled = true;

    /**
     * 是否启用日志
     */
    private boolean logEnabled = true;

    /**
     * 是否启用黑白名单
     */
    private boolean blackWhiteListEnabled = true;

    /**
     * 白名单路径（无需鉴权）
     */
    private List<String> whitePaths = new ArrayList<>();

    /**
     * 黑名单配置（IP、用户、路径）
     * 示例：
     * - ip:192.168.1.100
     * - user:admin
     * - path:/admin
     */
    private List<String> blackPaths = new ArrayList<>();

    /**
     * 限流配置
     */
    private RateLimit rateLimit = new RateLimit();

    /**
     * 鉴权配置
     */
    @org.springframework.boot.context.properties.NestedConfigurationProperty
    private Auth auth = new Auth();

    /**
     * 日志配置
     */
    private Log log = new Log();

    /**
     * 限流配置
     */
    @Data
    public static class RateLimit {
        /**
         * 默认限流阈值（每分钟请求数）
         */
        private long defaultLimit = 100;

        /**
         * 默认时间窗口（秒）
         */
        private long defaultWindow = 60;

        /**
         * 是否启用IP限流
         */
        private boolean ipRateLimit = true;

        /**
         * IP限流阈值（每分钟请求数）
         */
        private long ipLimit = 60;

        /**
         * IP限流时间窗口（秒）
         */
        private long ipWindow = 60;

        /**
         * 自定义路径限流配置
         * key: 路径前缀
         * value: 限流配置 [limit, window]
         */
        private Map<String, List<Long>> pathLimits = new HashMap<>();
    }

    /**
     * 鉴权配置
     */
    @Data
    public static class Auth {
        /**
         * Token请求头名称
         */
        private String tokenHeader = "Authorization";

        /**
         * Token前缀
         */
        private String tokenPrefix = "Bearer ";

        /**
         * JWT密钥-管理后台（建议从Nacos配置中心读取）
         */
        private String jwtSecretAdmin = "platform-admin-jwt-secret-key-change-in-production";

        /**
         * JWT密钥-H5用户（建议从Nacos配置中心读取）
         */
        private String jwtSecretH5 = "platform-h5-jwt-secret-key-change-in-production";

        /**
         * Token过期时间（秒）
         */
        private long tokenExpire = 7200;

        /**
         * 是否跳过Token验证（开发环境）
         */
        private boolean skipTokenValidation = false;
    }

    /**
     * 日志配置
     */
    @Data
    public static class Log {
        /**
         * 是否记录请求头
         */
        private boolean logHeaders = false;

        /**
         * 是否记录请求体
         */
        private boolean logBody = false;

        /**
         * 是否记录响应体
         */
        private boolean logResponseBody = false;

        /**
         * 敏感参数列表（日志脱敏）
         */
        private List<String> sensitiveParams = List.of("password", "pwd", "secret", "token");

        /**
         * 是否脱敏敏感信息
         * （替换为***）
         */
        private boolean maskSensitive = true;
    }
}
