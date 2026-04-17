package com.zzl.platform.common.web.config;

import com.zzl.platform.common.core.util.MdcUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.Map;
import java.util.concurrent.Executor;

/**
 * 异步MDC配置
 * 确保异步任务能够继承主线程的MDC上下文
 */
@Configuration
@EnableAsync
public class AsyncMdcConfig implements AsyncConfigurer {

    private static final Logger logger = LoggerFactory.getLogger(AsyncMdcConfig.class);

    /**
     * 配置支持MDC传递的线程池任务执行器
     */
    @Bean(name = "asyncExecutor")
    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // 核心线程数
        executor.setCorePoolSize(10);
        // 最大线程数
        executor.setMaxPoolSize(50);
        // 队列容量
        executor.setQueueCapacity(100);
        // 线程名称前缀
        executor.setThreadNamePrefix("async-pool-");
        // 线程存活时间（秒）
        executor.setKeepAliveSeconds(60);
        // 等待任务完成后再关闭线程池
        executor.setWaitForTasksToCompleteOnShutdown(true);
        // 最大等待时间
        executor.setAwaitTerminationSeconds(60);

        // 自定义任务装饰器，确保MDC上下文传递
        executor.setTaskDecorator(runnable -> {
            // 获取当前线程的MDC上下文副本
            Map<String, String> contextMap = MdcUtils.getCopyOfContextMap();
            return () -> {
                Map<String, String> oldContext = MdcUtils.getCopyOfContextMap();
                try {
                    // 设置继承的MDC上下文
                    if (contextMap != null) {
                        MdcUtils.setContextMap(contextMap);
                    }
                    // 执行任务
                    runnable.run();
                } finally {
                    // 恢复原始MDC上下文
                    if (oldContext != null) {
                        MdcUtils.setContextMap(oldContext);
                    } else {
                        MdcUtils.clear();
                    }
                }
            };
        });

        executor.initialize();
        logger.info("异步MDC线程池初始化完成");
        return executor;
    }

    /**
     * 异步任务异常处理器
     */
    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (throwable, method, params) -> {
            logger.error("异步任务执行异常: method={}, params={}, traceId={}",
                    method.getName(),
                    params,
                    MdcUtils.getTraceId(),
                    throwable);
        };
    }
}
