package com.zzl.platform.common.redis.service;

import com.zzl.platform.common.redis.exception.RedisRateLimitException;
import com.zzl.platform.common.redis.properties.RedisProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

/**
 * Redis限流服务
 * 提供多种限流算法：固定窗口、令牌桶、滑动窗口
 * 使用Lua脚本保证原子性
 */
@Slf4j
@Service
@ConditionalOnBean(RedisService.class)
public class RedisRateLimiterService {

    private final RedisService redisService;
    private final RedisProperties redisProperties;
    private final StringRedisTemplate stringRedisTemplate;

    public RedisRateLimiterService(RedisService redisService, RedisProperties redisProperties,
                                   StringRedisTemplate stringRedisTemplate) {
        this.redisService = redisService;
        this.redisProperties = redisProperties;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    /**
     * 固定窗口限流（原子版本）
     *
     * @param key    限流key
     * @param limit  限流阈值
     * @param window 时间窗口（秒）
     * @return 是否允许通过
     */
    public boolean rateLimit(String key, long limit, long window) {
        return rateLimit(key, limit, window, true);
    }

    /**
     * 固定窗口限流（原子版本）
     * 使用Lua脚本保证"检查-递增"的原子性
     * <p>
     * Lua脚本逻辑：
     * 1. 获取当前计数
     * 2. 检查是否超限
     * 3. 如果未超限，递增计数并设置过期时间
     * 4. 返回结果（1=允许，0=拒绝）
     *
     * @param key         限流key
     * @param limit       限流阈值
     * @param window      时间窗口（秒）
     * @param throwExceed 是否超限抛异常
     * @return 是否允许通过
     */
    public boolean rateLimit(String key, long limit, long window, boolean throwExceed) {
        String rateKey = redisProperties.getRateLimiter().getKeyPrefix().concat(key);

        // Lua脚本保证原子性
        String luaScript =
                "local current = redis.call('GET', KEYS[1]) " +
                        "if current == false then " +
                        "  current = 0 " +
                        "else " +
                        "  current = tonumber(current) " +
                        "end " +
                        "if current < tonumber(ARGV[1]) then " +
                        "  local newValue = redis.call('INCR', KEYS[1]) " +
                        "  if newValue == 1 then " +
                        "    redis.call('EXPIRE', KEYS[1], tonumber(ARGV[2])) " +
                        "  end " +
                        "  return 1 " +
                        "else " +
                        "  return 0 " +
                        "end";

        Long result = executeLuaScript(rateKey, luaScript,
                String.valueOf(limit),
                String.valueOf(window)
        );

        boolean allowed = result != null && result == 1;
        if (!allowed) {
            log.warn("Rate limit exceeded for key: {}, limit: {}, window: {}s", key, limit, window);
            if (throwExceed) {
                throw new RedisRateLimitException("Rate limit exceeded for: " + key);
            }
        }

        return allowed;
    }

    /**
     * 令牌桶限流（原子版本）
     *
     * @param key        限流key
     * @param capacity   令牌桶容量
     * @param refillRate 令牌补充速率（个/秒）
     * @return 是否允许通过
     */
    public boolean tokenBucketRateLimit(String key, long capacity, long refillRate) {
        return tokenBucketRateLimit(key, capacity, refillRate, true);
    }

    /**
     * 令牌桶限流（原子版本）
     * 使用Lua脚本实现精确的令牌桶算法
     * <p>
     * Lua脚本逻辑：
     * 1. 获取当前令牌数和上次填充时间
     * 2. 如果是首次访问，初始化为容量
     * 3. 计算需要补充的令牌数（基于时间差）
     * 4. 限制令牌数不超过容量
     * 5. 如果有令牌，消耗一个并返回成功
     * 6. 否则返回失败
     *
     * @param key         限流key
     * @param capacity    令牌桶容量
     * @param refillRate  令牌补充速率（个/秒）
     * @param throwExceed 是否超限抛异常
     * @return 是否允许通过
     */
    public boolean tokenBucketRateLimit(String key, long capacity, long refillRate, boolean throwExceed) {
        String bucketKey = redisProperties.getRateLimiter().getKeyPrefix().concat("bucket:").concat(key);

        // Lua脚本实现令牌桶算法
        String luaScript =
                "local key = KEYS[1] " +
                        "local now = tonumber(ARGV[1]) " +
                        "local capacity = tonumber(ARGV[2]) " +
                        "local interval = tonumber(ARGV[3]) " +
                        "local info = redis.call('HMGET', key, 'tokens', 'last_refill') " +
                        "local tokens = tonumber(info[1]) " +
                        "local lastRefill = tonumber(info[2]) " +
                        "if tokens == nil then " +
                        "  tokens = capacity " +
                        "  lastRefill = now " +
                        "  redis.call('HMSET', key, 'tokens', tokens, 'last_refill', lastRefill) " +
                        "else " +
                        "  local elapsed = now - lastRefill " +
                        "  local refill = math.floor(elapsed / interval) " +
                        "  tokens = math.min(capacity, tokens + refill) " +
                        "  if refill > 0 then " +
                        "    lastRefill = now " +
                        "    redis.call('HMSET', key, 'tokens', tokens, 'last_refill', lastRefill) " +
                        "  end " +
                        "end " +
                        "redis.call('EXPIRE', key, math.ceil(capacity / refillRate) + 10) " +
                        "if tokens > 0 then " +
                        "  tokens = tokens - 1 " +
                        "  redis.call('HMSET', key, 'tokens', tokens) " +
                        "  return 1 " +
                        "else " +
                        "  return 0 " +
                        "end";

        long now = System.currentTimeMillis();
        long interval = 1000 / refillRate; // 每个令牌的间隔时间（毫秒）

        Long result = executeLuaScript(bucketKey, luaScript,
                String.valueOf(now),
                String.valueOf(capacity),
                String.valueOf(interval)
        );

        boolean allowed = result != null && result == 1;
        if (!allowed) {
            log.warn("Token bucket rate limit exceeded for key: {}, capacity: {}, rate: {}", key, capacity, refillRate);
            if (throwExceed) {
                throw new RedisRateLimitException("Token bucket rate limit exceeded for: " + key);
            }
        }

        return allowed;
    }

    /**
     * 滑动窗口限流（原子版本）
     *
     * @param key    限流key
     * @param limit  限流阈值
     * @param window 时间窗口（秒）
     * @return 是否允许通过
     */
    public boolean slidingWindowRateLimit(String key, long limit, long window) {
        return slidingWindowRateLimit(key, limit, window, true);
    }

    /**
     * 滑动窗口限流（原子版本）
     * 使用Lua脚本和ZSet实现精确的滑动窗口
     * <p>
     * Lua脚本逻辑：
     * 1. 删除窗口外的旧数据（使用ZSet按分数范围删除）
     * 2. 获取当前窗口内的请求数
     * 3. 如果未超限，添加当前请求并返回成功
     * 4. 否则返回失败
     * <p>
     * ZSet结构：key = [(score, member), ...]，其中score和member都是时间戳
     * 通过score（时间戳）范围可以快速删除窗口外的数据
     *
     * @param key         限流key
     * @param limit       限流阈值
     * @param window      时间窗口（秒）
     * @param throwExceed 是否超限抛异常
     * @return 是否允许通过
     */
    public boolean slidingWindowRateLimit(String key, long limit, long window, boolean throwExceed) {
        String windowKey = redisProperties.getRateLimiter().getKeyPrefix().concat("window:").concat(key);

        // Lua脚本实现滑动窗口算法
        String luaScript =
                "local key = KEYS[1] " +
                        "local now = tonumber(ARGV[1]) " +
                        "local windowStart = tonumber(ARGV[2]) " +
                        "local limit = tonumber(ARGV[3]) " +
                        "local windowTime = tonumber(ARGV[4]) " +
                        "redis.call('ZREMRANGEBYSCORE', key, 0, windowStart) " +
                        "local count = redis.call('ZCARD', key) " +
                        "if count < limit then " +
                        "  redis.call('ZADD', key, now, now) " +
                        "  redis.call('EXPIRE', key, windowTime) " +
                        "  return 1 " +
                        "else " +
                        "  return 0 " +
                        "end";

        long now = System.currentTimeMillis();
        long windowStart = now - window * 1000;

        Long result = executeLuaScript(windowKey, luaScript,
                String.valueOf(now),
                String.valueOf(windowStart),
                String.valueOf(limit),
                String.valueOf(window + 10)
        );

        boolean allowed = result != null && result == 1;
        if (!allowed) {
            log.warn("Sliding window rate limit exceeded for key: {}, limit: {}, window: {}s", key, limit, window);
            if (throwExceed) {
                throw new RedisRateLimitException("Sliding window rate limit exceeded for: " + key);
            }
        }

        return allowed;
    }

    /**
     * 批量限流检查
     * 用于需要同时检查多个限流策略的场景
     *
     * @param limits 限流配置数组 [key, limit, window, key, limit, window, ...]
     * @return 是否所有限流都通过
     */
    public boolean multiRateLimit(long... limits) {
        if (limits == null || limits.length % 3 != 0) {
            throw new IllegalArgumentException("limits must be multiple of 3: [key, limit, window, ...]");
        }

        try {
            for (int i = 0; i < limits.length; i += 3) {
                String key = String.valueOf(limits[i]);
                long limit = limits[i + 1];
                long window = limits[i + 2];

                if (!rateLimit(key, limit, window, false)) {
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            log.error("Failed to execute multi-rate-limit", e);
            return false;
        }
    }

    /**
     * 执行Lua脚本
     * 使用RedisCallback保证脚本执行的原子性
     */
    private Long executeLuaScript(String key, String script, String... args) {
        return stringRedisTemplate.execute((RedisCallback<Long>) connection -> {
            byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
            byte[] scriptBytes = script.getBytes(StandardCharsets.UTF_8);

            byte[][] argsBytes = new byte[args.length][];
            for (int i = 0; i < args.length; i++) {
                argsBytes[i] = args[i].getBytes(StandardCharsets.UTF_8);
            }
            if (argsBytes.length == 2) {
                return connection.eval(
                        scriptBytes,
                        org.springframework.data.redis.connection.ReturnType.INTEGER,
                        1,
                        keyBytes,
                        argsBytes[0],
                        argsBytes[1]
                );
            }
            if (argsBytes.length == 3) {
                return connection.eval(
                        scriptBytes,
                        org.springframework.data.redis.connection.ReturnType.INTEGER,
                        1,
                        keyBytes,
                        argsBytes[0],
                        argsBytes[1]
                );
            }
            if (argsBytes.length == 4) {
                return connection.eval(
                        scriptBytes,
                        org.springframework.data.redis.connection.ReturnType.INTEGER,
                        1,
                        keyBytes,
                        argsBytes[0],
                        argsBytes[1]
                );
            }
            return connection.eval(
                    scriptBytes,
                    org.springframework.data.redis.connection.ReturnType.INTEGER,
                    1,
                    keyBytes,
                    argsBytes[0]
            );
        });
    }
}
