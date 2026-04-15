package com.zzl.platform.common.redis.exception;

/**
 * Redis分布式锁异常
 */
public class RedisLockException extends RedisException {

    public RedisLockException(String message) {
        super("REDIS_LOCK_ERROR", message);
    }

    public RedisLockException(String message, Throwable cause) {
        super("REDIS_LOCK_ERROR", message, cause);
    }
}
