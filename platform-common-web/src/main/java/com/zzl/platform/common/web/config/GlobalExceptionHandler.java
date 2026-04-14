package com.zzl.platform.common.web.config;

import com.zzl.platform.common.core.res.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler(value = Exception.class)
    public Result<?> handleException(Exception e) {
        log.error("Exception", e);
        return Result.fail(e.getMessage());
    }

    @ExceptionHandler(value = IllegalArgumentException.class)
    public Result<?> handleIllegalArgumentException(Exception e) {
        log.error("IllegalArgumentException", e);
        return Result.fail(e.getMessage());
    }
}
