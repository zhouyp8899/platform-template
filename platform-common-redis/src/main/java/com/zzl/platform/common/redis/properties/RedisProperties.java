package com.zzl.platform.common.redis.properties;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.validation.annotation.Validated;

/**
 * Redis自定义配置属性
 * 用于配置Redis连接池、缓存、分布式锁等参数
 */
@Data
@Validated
@ConfigurationProperties(prefix = "platform.redis")
@RefreshScope
public class RedisProperties {

    /**
     * 是否启用Redis
     */
    private Boolean enabled = true;

    /**
     * 锁配置
     */
    private Lock lock = new Lock();

    /**
     * 缓存配置
     */
    private Cache cache = new Cache();

    /**
     * 限流配置
     */
    private RateLimiter rateLimiter = new RateLimiter();

    @Data
    public static class Lock {
        /**
         * 锁等待时间(秒)，默认30秒
         */
        @Min(value = 1, message = "锁等待时间不能小于1秒")
        private Long waitTime = 30L;

        /**
         * 锁持有时间(秒)，默认30秒
         */
        @Min(value = 1, message = "锁持有时间不能小于1秒")
        private Long leaseTime = 30L;

        /**
         * 锁key前缀，默认"lock:"
         */
        @NotBlank(message = "锁key前缀不能为空")
        private String keyPrefix = "lock:";
    }

    @Data
    public static class Cache {
        /**
         * 缓存key前缀，默认"cache:"
         */
        @NotBlank(message = "缓存key前缀不能为空")
        private String keyPrefix = "cache:";

        /**
         * 默认过期时间(秒)，默认3600秒(1小时)
         */
        @Min(value = 1, message = "缓存过期时间不能小于1秒")
        private Long defaultExpire = 3600L;
    }

    @Data
    public static class RateLimiter {
        /**
         * 限流key前缀，默认"rate:"
         */
        @NotBlank(message = "限流key前缀不能为空")
        private String keyPrefix = "rate:";

        /**
         * 默认限流窗口时间(秒)，默认60秒
         */
        @Min(value = 1, message = "限流窗口时间不能小于1秒")
        private Long defaultWindow = 60L;
    }
}
