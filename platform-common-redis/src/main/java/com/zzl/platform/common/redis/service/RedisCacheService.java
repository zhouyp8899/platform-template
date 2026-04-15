package com.zzl.platform.common.redis.service;

import com.zzl.platform.common.redis.properties.RedisProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.lang.annotation.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Redis缓存服务
 * 提供编程式缓存操作和注解式缓存支持
 */
@Slf4j
@Service
@ConditionalOnBean(RedisService.class)
public class RedisCacheService {

    private final RedisService redisService;
    private final RedisProperties redisProperties;

    public RedisCacheService(RedisService redisService, RedisProperties redisProperties) {
        this.redisService = redisService;
        this.redisProperties = redisProperties;
    }

    /**
     * 获取缓存值
     *
     * @param key 缓存key
     * @param <T> 值类型
     * @return 缓存值
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        String cacheKey = redisProperties.getCache().getKeyPrefix() + key;
        return (T) redisService.get(cacheKey);
    }

    /**
     * 设置缓存值（使用默认过期时间）
     *
     * @param key   缓存key
     * @param value 缓存值
     */
    public void set(String key, Object value) {
        set(key, value, redisProperties.getCache().getDefaultExpire(), TimeUnit.SECONDS);
    }

    /**
     * 设置缓存值
     *
     * @param key      缓存key
     * @param value    缓存值
     * @param expire   过期时间
     * @param timeUnit 时间单位
     */
    public void set(String key, Object value, long expire, TimeUnit timeUnit) {
        String cacheKey = redisProperties.getCache().getKeyPrefix() + key;
        redisService.set(cacheKey, value, expire, timeUnit);
        log.debug("Cache set: key={}, expire={} {}", cacheKey, expire, timeUnit);
    }

    /**
     * 获取缓存，如果不存在则从数据源加载并缓存
     *
     * @param key    缓存key
     * @param loader 数据加载器
     * @param <T>    值类型
     * @return 缓存值
     */
    public <T> T getOrLoad(String key, Supplier<T> loader) {
        return getOrLoad(key, loader, redisProperties.getCache().getDefaultExpire(), TimeUnit.SECONDS);
    }

    /**
     * 获取缓存，如果不存在则从数据源加载并缓存
     *
     * @param key      缓存key
     * @param loader   数据加载器
     * @param expire   过期时间
     * @param timeUnit 时间单位
     * @param <T>      值类型
     * @return 缓存值
     */
    public <T> T getOrLoad(String key, Supplier<T> loader, long expire, TimeUnit timeUnit) {
        String cacheKey = redisProperties.getCache().getKeyPrefix() + key;
        try {
            T cached = redisService.get(cacheKey);
            if (cached != null) {
                log.debug("Cache hit: key={}", cacheKey);
                return cached;
            }

            log.debug("Cache miss, loading data: key={}", cacheKey);
            T loadedValue = loader.get();
            if (loadedValue != null) {
                set(key, loadedValue, expire, timeUnit);
            }
            return loadedValue;
        } catch (Exception e) {
            log.error("Failed to load cache: key={}", cacheKey, e);
            throw e;
        }
    }

    /**
     * 删除缓存
     *
     * @param key 缓存key
     * @return 是否删除成功
     */
    public Boolean delete(String key) {
        String cacheKey = redisProperties.getCache().getKeyPrefix() + key;
        Boolean result = redisService.delete(cacheKey);
        log.debug("Cache delete: key={}, result={}", cacheKey, result);
        return result;
    }

    /**
     * 判断缓存是否存在
     *
     * @param key 缓存key
     * @return 是否存在
     */
    public Boolean exists(String key) {
        String cacheKey = redisProperties.getCache().getKeyPrefix() + key;
        return redisService.hasKey(cacheKey);
    }

    /**
     * 设置缓存过期时间
     *
     * @param key      缓存key
     * @param expire   过期时间
     * @param timeUnit 时间单位
     * @return 是否设置成功
     */
    public Boolean expire(String key, long expire, TimeUnit timeUnit) {
        String cacheKey = redisProperties.getCache().getKeyPrefix() + key;
        return redisService.expire(cacheKey, expire, timeUnit);
    }

    /**
     * 获取缓存剩余过期时间
     *
     * @param key      缓存key
     * @param timeUnit 时间单位
     * @return 剩余过期时间
     */
    public Long getExpire(String key, TimeUnit timeUnit) {
        String cacheKey = redisProperties.getCache().getKeyPrefix() + key;
        return redisService.getExpire(cacheKey, timeUnit);
    }

    /**
     * 批量获取缓存
     *
     * @param keys 缓存key列表
     * @param <T>  值类型
     * @return 缓存值列表
     */
    @SuppressWarnings("unchecked")
    public <T> List<T> multiGet(Collection<String> keys) {
        List<String> cacheKeys = keys.stream()
                .map(key -> redisProperties.getCache().getKeyPrefix() + key)
                .collect(Collectors.toList());
        List<Object> values = redisService.getRedisTemplate().opsForValue().multiGet(cacheKeys);
        List<T> result = new ArrayList<>();
        if (values != null) {
            for (Object obj : values) {
                result.add((T) obj);
            }
        }
        return result;
    }

    /**
     * 批量设置缓存
     *
     * @param map 缓存键值对
     */
    public void multiSet(Map<String, Object> map) {
        Map<String, Object> cacheMap = map.entrySet().stream()
                .collect(Collectors.toMap(
                        entry -> redisProperties.getCache().getKeyPrefix() + entry.getKey(),
                        Map.Entry::getValue
                ));
        redisService.getRedisTemplate().opsForValue().multiSet(cacheMap);
        log.debug("Batch cache set: size={}", map.size());
    }

    /**
     * 缓存注解 - 可缓存
     * 用于方法级别的缓存，支持自定义key、过期时间等
     */
    @Target({ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @Cacheable
    public @interface Cached {
        /**
         * 缓存名称
         */
        String value() default "";

        /**
         * 缓存key的SpEL表达式
         */
        String key() default "";

        /**
         * 条件表达式，满足条件才缓存
         */
        String condition() default "";

        /**
         * 排除条件表达式，满足条件则不缓存
         */
        String unless() default "";
    }

    /**
     * 缓存注解 - 缓存更新
     * 用于方法执行后更新缓存
     */
    @Target({ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @CachePut
    public @interface CacheUpdate {
        /**
         * 缓存名称
         */
        String value() default "";

        /**
         * 缓存key的SpEL表达式
         */
        String key() default "";

        /**
         * 条件表达式，满足条件才更新缓存
         */
        String condition() default "";

        /**
         * 排除条件表达式，满足条件则不更新缓存
         */
        String unless() default "";
    }

    /**
     * 缓存注解 - 缓存删除
     * 用于方法执行后删除缓存
     */
    @Target({ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @CacheEvict
    public @interface CacheClear {
        /**
         * 缓存名称
         */
        String value() default "";

        /**
         * 缓存key的SpEL表达式
         */
        String key() default "";

        /**
         * 是否删除所有缓存
         */
        boolean allEntries() default false;

        /**
         * 是否在方法执行前删除
         */
        boolean beforeInvocation() default false;

        /**
         * 条件表达式，满足条件才删除缓存
         */
        String condition() default "";
    }
}
