package com.zzl.platform.common.redis.exception;

/**
 * Redis限流异常
 */
public class RedisRateLimitException extends RedisException {

    public RedisRateLimitException(String message) {
        super("REDIS_RATE_LIMIT_ERROR", message);
    }

    public RedisRateLimitException(String message, Throwable cause) {
        super("REDIS_RATE_LIMIT_ERROR", message, cause);
    }
}
