package com.zzl.platform.common.redis.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Redis自定义配置属性
 * 用于配置Redis连接池、缓存、分布式锁等参数
 */
@Data
@ConfigurationProperties(prefix = "platform.redis")
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
        private Long waitTime = 30L;

        /**
         * 锁持有时间(秒)，默认30秒
         */
        private Long leaseTime = 30L;

        /**
         * 锁key前缀，默认"lock:"
         */
        private String keyPrefix = "lock:";
    }

    @Data
    public static class Cache {
        /**
         * 缓存key前缀，默认"cache:"
         */
        private String keyPrefix = "cache:";

        /**
         * 默认过期时间(秒)，默认3600秒(1小时)
         */
        private Long defaultExpire = 3600L;
    }

    @Data
    public static class RateLimiter {
        /**
         * 限流key前缀，默认"rate:"
         */
        private String keyPrefix = "rate:";

        /**
         * 默认限流窗口时间(秒)，默认60秒
         */
        private Long defaultWindow = 60L;
    }
}
