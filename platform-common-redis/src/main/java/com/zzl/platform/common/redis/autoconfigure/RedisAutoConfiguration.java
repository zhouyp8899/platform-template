package com.zzl.platform.common.redis.autoconfigure;

import com.zzl.platform.common.redis.config.RedisConfig;
import com.zzl.platform.common.redis.properties.RedisProperties;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;

/**
 * Redis模块自动配置类
 * 通过Spring Boot的自动配置机制，确保引入依赖后自动加载所有Redis相关组件
 *
 * 注意：添加了@ConditionalOnProperty确保Redis配置就绪后再加载
 */
@AutoConfiguration
@ConditionalOnClass({RedissonClient.class, com.zzl.platform.common.redis.service.RedisService.class})
@ConditionalOnProperty(prefix = "platform.redis", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(RedisProperties.class)
@Import(RedisConfig.class)
public class RedisAutoConfiguration {

    /**
     * 自动配置会通过以下方式自动注册Bean：
     * 1. RedisConfig -> RedisTemplate, StringRedisTemplate, RedisCacheConfiguration
     * 2. RedisService -> 提供基础Redis操作
     * 3. RedisCacheService -> 提供缓存操作（依赖RedisService）
     * 4. RedisLockService -> 提供分布式锁（依赖RedissonClient）
     * 5. RedisRateLimiterService -> 提供限流功能（依赖RedisService）
     *
     * 所有服务类都使用@Service注解，会被Spring自动扫描并注册
     *
     * 注意：添加了@ConditionalOnProperty确保在Nacos配置加载后才初始化
     */
}
