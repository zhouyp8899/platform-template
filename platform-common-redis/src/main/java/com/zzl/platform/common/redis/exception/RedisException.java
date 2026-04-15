package com.zzl.platform.common.redis.exception;

import lombok.Getter;

/**
 * Redis操作异常基类
 */
@Getter
public class RedisException extends RuntimeException {

    /**
     * 错误码
     */
    private final String errorCode;

    public RedisException(String message) {
        super(message);
        this.errorCode = "REDIS_ERROR";
    }

    public RedisException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public RedisException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "REDIS_ERROR";
    }

    public RedisException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
}
