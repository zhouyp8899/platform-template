package com.zzl.platform.common.web.config;

import com.zzl.platform.common.core.util.MdcUtils;

import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 支持MDC传递的Future包装器
 * 用于@Async注解返回Future的场景
 */
class MdcFutureDecorator {

    /**
     * 装饰Future对象，确保MDC上下文在完成时可用
     *
     * @param future 原始Future
     * @param <T>    返回类型
     * @return 装饰后的Future
     */
    public static <T> Future<T> decorate(Future<T> future) {
        Map<String, String> contextMap = MdcUtils.getCopyOfContextMap();
        return new Future<T>() {
            @Override
            public boolean cancel(boolean mayInterruptIfRunning) {
                return future.cancel(mayInterruptIfRunning);
            }

            @Override
            public boolean isCancelled() {
                return future.isCancelled();
            }

            @Override
            public boolean isDone() {
                return future.isDone();
            }

            @Override
            public T get() throws InterruptedException, ExecutionException {
                Map<String, String> oldContext = MdcUtils.getCopyOfContextMap();
                try {
                    if (contextMap != null) {
                        MdcUtils.setContextMap(contextMap);
                    }
                    return future.get();
                } finally {
                    if (oldContext != null) {
                        MdcUtils.setContextMap(oldContext);
                    } else {
                        MdcUtils.clear();
                    }
                }
            }

            @Override
            public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
                Map<String, String> oldContext = MdcUtils.getCopyOfContextMap();
                try {
                    if (contextMap != null) {
                        MdcUtils.setContextMap(contextMap);
                    }
                    return future.get(timeout, unit);
                } finally {
                    if (oldContext != null) {
                        MdcUtils.setContextMap(oldContext);
                    } else {
                        MdcUtils.clear();
                    }
                }
            }
        };
    }
}
