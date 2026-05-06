package com.zzl.platform.common.core.config;

import feign.Logger;
import feign.Request;
import feign.Retryer;
import feign.codec.ErrorDecoder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Feign配置类
 * 提供Feign客户端的统一配置
 */
@Configuration
@ConditionalOnClass(feign.Feign.class)
public class FeignConfig {

    /**
     * 日志级别
     */
    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.BASIC;
    }

    /**
     * 重试策略
     * 默认重试5次，间隔100ms
     */
    @Bean
    public Retryer feignRetryer() {
        return new Retryer.Default(100, 500, 5);
    }

    /**
     * 请求超时配置
     */
    @Bean
    public Request.Options feignOptions() {
        return new Request.Options(
                5, TimeUnit.SECONDS,  // 连接超时
                30, TimeUnit.SECONDS, // 读取超时
                true                  // 是否跟随重定向
        );
    }

    /**
     * 错误解码器
     */
    @Bean
    @ConditionalOnMissingBean(ErrorDecoder.class)
    public ErrorDecoder errorDecoder() {
        return new FeignErrorDecoder();
    }
}
