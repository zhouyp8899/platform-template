package com.zzl.platform.common.redis.service;

import com.zzl.platform.common.redis.exception.RedisLockException;
import com.zzl.platform.common.redis.properties.RedisProperties;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Redis分布式锁服务
 * 基于Redisson实现分布式锁，支持可重入锁、公平锁、读写锁等
 */
@Slf4j
@Service
@ConditionalOnBean(RedissonClient.class)
public class RedisLockService {

    private final RedissonClient redissonClient;
    private final RedisProperties redisProperties;

    public RedisLockService(RedissonClient redissonClient, RedisProperties redisProperties) {
        this.redissonClient = redissonClient;
        this.redisProperties = redisProperties;
    }

    /**
     * 获取锁对象
     *
     * @param lockKey 锁的key
     * @return RLock对象
     */
    public RLock getLock(String lockKey) {
        String key = redisProperties.getLock().getKeyPrefix() + lockKey;
        return redissonClient.getLock(key);
    }

    /**
     * 获取公平锁对象
     *
     * @param lockKey 锁的key
     * @return RLock对象
     */
    public RLock getFairLock(String lockKey) {
        String key = redisProperties.getLock().getKeyPrefix() + lockKey;
        return redissonClient.getFairLock(key);
    }

    /**
     * 获取读写锁的写锁
     *
     * @param lockKey 锁的key
     * @return RLock对象
     */
    public RLock getReadWriteLockWrite(String lockKey) {
        String key = redisProperties.getLock().getKeyPrefix() + lockKey;
        return redissonClient.getReadWriteLock(key).writeLock();
    }

    /**
     * 获取读写锁的读锁
     *
     * @param lockKey 锁的key
     * @return RLock对象
     */
    public RLock getReadWriteLockRead(String lockKey) {
        String key = redisProperties.getLock().getKeyPrefix() + lockKey;
        return redissonClient.getReadWriteLock(key).readLock();
    }

    /**
     * 尝试获取锁（使用默认配置）
     *
     * @param lockKey 锁的key
     * @return 是否获取成功
     */
    public boolean tryLock(String lockKey) {
        return tryLock(lockKey,
                redisProperties.getLock().getWaitTime(),
                redisProperties.getLock().getLeaseTime(),
                TimeUnit.SECONDS);
    }

    /**
     * 尝试获取锁
     *
     * @param lockKey   锁的key
     * @param waitTime  等待获取锁的最长时间
     * @param leaseTime 锁持有时间（超过此时间自动释放）
     * @param timeUnit  时间单位
     * @return 是否获取成功
     */
    public boolean tryLock(String lockKey, long waitTime, long leaseTime, TimeUnit timeUnit) {
        RLock lock = getLock(lockKey);
        try {
            boolean acquired = lock.tryLock(waitTime, leaseTime, timeUnit);
            if (acquired) {
                log.debug("Successfully acquired lock: {}", lockKey);
            } else {
                log.warn("Failed to acquire lock: {}", lockKey);
            }
            return acquired;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RedisLockException("Interrupted while acquiring lock: " + lockKey, e);
        }
    }

    /**
     * 释放锁
     *
     * @param lockKey 锁的key
     */
    public void unlock(String lockKey) {
        RLock lock = getLock(lockKey);
        if (lock.isHeldByCurrentThread()) {
            lock.unlock();
            log.debug("Successfully released lock: {}", lockKey);
        } else {
            log.warn("Attempted to unlock a lock not held by current thread: {}", lockKey);
        }
    }

    /**
     * 执行带锁的代码块（使用默认配置）
     *
     * @param lockKey  锁的key
     * @param supplier 要执行的代码块
     * @param <T>      返回值类型
     * @return 执行结果
     */
    public <T> T executeWithLock(String lockKey, Supplier<T> supplier) {
        return executeWithLock(lockKey,
                redisProperties.getLock().getWaitTime(),
                redisProperties.getLock().getLeaseTime(),
                TimeUnit.SECONDS,
                supplier);
    }

    /**
     * 执行带锁的代码块
     *
     * @param lockKey   锁的key
     * @param waitTime  等待获取锁的最长时间
     * @param leaseTime 锁持有时间
     * @param timeUnit  时间单位
     * @param supplier  要执行的代码块
     * @param <T>       返回值类型
     * @return 执行结果
     */
    public <T> T executeWithLock(String lockKey, long waitTime, long leaseTime, TimeUnit timeUnit, Supplier<T> supplier) {
        RLock lock = getLock(lockKey);
        try {
            if (lock.tryLock(waitTime, leaseTime, timeUnit)) {
                try {
                    log.debug("Executing with lock: {}", lockKey);
                    return supplier.get();
                } finally {
                    lock.unlock();
                    log.debug("Released lock after execution: {}", lockKey);
                }
            } else {
                throw new RedisLockException("Failed to acquire lock within timeout: " + lockKey);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RedisLockException("Interrupted while acquiring lock: " + lockKey, e);
        }
    }

    /**
     * 执行带锁的代码块（无返回值）
     *
     * @param lockKey  锁的key
     * @param runnable 要执行的代码块
     */
    public void executeWithLock(String lockKey, Runnable runnable) {
        executeWithLock(lockKey, () -> {
            runnable.run();
            return null;
        });
    }

    /**
     * 判断锁是否被当前线程持有
     *
     * @param lockKey 锁的key
     * @return 是否被持有
     */
    public boolean isHeldByCurrentThread(String lockKey) {
        RLock lock = getLock(lockKey);
        return lock.isHeldByCurrentThread();
    }

    /**
     * 判断锁是否存在
     *
     * @param lockKey 锁的key
     * @return 是否存在
     */
    public boolean isLocked(String lockKey) {
        RLock lock = getLock(lockKey);
        return lock.isLocked();
    }
}
