package com.zzl.platform.common.redis.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Redis基础操作服务
 * 封装RedisTemplate的常用操作，提供类型安全的API
 */
@Service
public class RedisService {

    private final RedisTemplate<String, Object> redisTemplate;

    public RedisService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // ==================== String操作 ====================

    /**
     * 设置key-value
     *
     * @param key   Redis key
     * @param value 值
     */
    public void set(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }

    /**
     * 设置key-value，带过期时间
     *
     * @param key      Redis key
     * @param value    值
     * @param timeout  过期时间
     * @param timeUnit 时间单位
     */
    public void set(String key, Object value, long timeout, TimeUnit timeUnit) {
        redisTemplate.opsForValue().set(key, value, timeout, timeUnit);
    }

    /**
     * 获取key对应的值
     *
     * @param key Redis key
     * @return 值
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        return (T) redisTemplate.opsForValue().get(key);
    }

    /**
     * 删除key
     *
     * @param key Redis key
     * @return 是否删除成功
     */
    public Boolean delete(String key) {
        return redisTemplate.delete(key);
    }

    /**
     * 批量删除key
     *
     * @param keys Redis keys
     * @return 删除的数量
     */
    public Long delete(Collection<String> keys) {
        return redisTemplate.delete(keys);
    }

    /**
     * 判断key是否存在
     *
     * @param key Redis key
     * @return 是否存在
     */
    public Boolean hasKey(String key) {
        return redisTemplate.hasKey(key);
    }

    /**
     * 设置key的过期时间
     *
     * @param key      Redis key
     * @param timeout  过期时间
     * @param timeUnit 时间单位
     * @return 是否设置成功
     */
    public Boolean expire(String key, long timeout, TimeUnit timeUnit) {
        return redisTemplate.expire(key, timeout, timeUnit);
    }

    /**
     * 获取key的过期时间
     *
     * @param key      Redis key
     * @param timeUnit 时间单位
     * @return 剩余过期时间，-1表示永不过期，-2表示key不存在
     */
    public Long getExpire(String key, TimeUnit timeUnit) {
        return redisTemplate.getExpire(key, timeUnit);
    }

    /**
     * 递增
     *
     * @param key   Redis key
     * @param delta 增量
     * @return 递增后的值
     */
    public Long increment(String key, long delta) {
        return redisTemplate.opsForValue().increment(key, delta);
    }

    /**
     * 递增（浮点数）
     *
     * @param key   Redis key
     * @param delta 增量
     * @return 递增后的值
     */
    public Double increment(String key, double delta) {
        return redisTemplate.opsForValue().increment(key, delta);
    }

    /**
     * 原子操作：只有当key不存在时才设置
     *
     * @param key   Redis key
     * @param value 值
     * @return 是否设置成功
     */
    public Boolean setIfAbsent(String key, Object value) {
        return redisTemplate.opsForValue().setIfAbsent(key, value);
    }

    /**
     * 原子操作：只有当key不存在时才设置，并设置过期时间
     *
     * @param key      Redis key
     * @param value    值
     * @param timeout  过期时间
     * @param timeUnit 时间单位
     * @return 是否设置成功
     */
    public Boolean setIfAbsent(String key, Object value, long timeout, TimeUnit timeUnit) {
        return redisTemplate.opsForValue().setIfAbsent(key, value, timeout, timeUnit);
    }

    // ==================== Hash操作 ====================

    /**
     * 设置hash字段
     *
     * @param key   Redis key
     * @param field 字段名
     * @param value 值
     */
    public void hSet(String key, String field, Object value) {
        redisTemplate.opsForHash().put(key, field, value);
    }

    /**
     * 获取hash字段值
     *
     * @param key   Redis key
     * @param field 字段名
     * @return 值
     */
    @SuppressWarnings("unchecked")
    public <T> T hGet(String key, String field) {
        return (T) redisTemplate.opsForHash().get(key, field);
    }

    /**
     * 批量设置hash字段
     *
     * @param key Redis key
     * @param map 字段值映射
     */
    public void hSetAll(String key, Map<String, Object> map) {
        redisTemplate.opsForHash().putAll(key, map);
    }

    /**
     * 获取所有hash字段值
     *
     * @param key Redis key
     * @return 所有字段值映射
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public Map<String, Object> hGetAll(String key) {
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(key);
        Map<String, Object> result = new java.util.HashMap<>();
        for (Map.Entry<Object, Object> entry : entries.entrySet()) {
            if (entry.getKey() != null) {
                result.put(entry.getKey().toString(), entry.getValue());
            }
        }
        return result;
    }

    /**
     * 删除hash字段
     *
     * @param key    Redis key
     * @param fields 字段名
     * @return 删除的字段数量
     */
    public Long hDelete(String key, Object... fields) {
        return redisTemplate.opsForHash().delete(key, fields);
    }

    /**
     * 判断hash字段是否存在
     *
     * @param key   Redis key
     * @param field 字段名
     * @return 是否存在
     */
    public Boolean hHasKey(String key, String field) {
        return redisTemplate.opsForHash().hasKey(key, field);
    }

    /**
     * 获取hash所有字段名
     *
     * @param key Redis key
     * @return 字段名集合
     */
    public Set<Object> hKeys(String key) {
        return redisTemplate.opsForHash().keys(key);
    }

    /**
     * 获取hash所有字段值
     *
     * @param key Redis key
     * @return 字段值列表
     */
    public List<Object> hValues(String key) {
        return redisTemplate.opsForHash().values(key);
    }

    /**
     * 获取hash大小
     *
     * @param key Redis key
     * @return hash大小
     */
    public Long hSize(String key) {
        return redisTemplate.opsForHash().size(key);
    }

    // ==================== List操作 ====================

    /**
     * 将值插入到列表左侧
     *
     * @param key   Redis key
     * @param value 值
     * @return 列表长度
     */
    public Long lLeftPush(String key, Object value) {
        return redisTemplate.opsForList().leftPush(key, value);
    }

    /**
     * 批量将值插入到列表左侧
     *
     * @param key    Redis key
     * @param values 值列表
     * @return 列表长度
     */
    public Long lLeftPushAll(String key, Collection<Object> values) {
        return redisTemplate.opsForList().leftPushAll(key, values);
    }

    /**
     * 将值插入到列表右侧
     *
     * @param key   Redis key
     * @param value 值
     * @return 列表长度
     */
    public Long lRightPush(String key, Object value) {
        return redisTemplate.opsForList().rightPush(key, value);
    }

    /**
     * 批量将值插入到列表右侧
     *
     * @param key    Redis key
     * @param values 值列表
     * @return 列表长度
     */
    public Long lRightPushAll(String key, Collection<Object> values) {
        return redisTemplate.opsForList().rightPushAll(key, values);
    }

    /**
     * 获取列表指定范围的元素
     *
     * @param key   Redis key
     * @param start 开始位置
     * @param end   结束位置
     * @return 元素列表
     */
    @SuppressWarnings("unchecked")
    public <T> List<T> lRange(String key, long start, long end) {
        return (List<T>) redisTemplate.opsForList().range(key, start, end);
    }

    /**
     * 弹出列表左侧元素
     *
     * @param key Redis key
     * @return 左侧元素
     */
    @SuppressWarnings("unchecked")
    public <T> T lLeftPop(String key) {
        return (T) redisTemplate.opsForList().leftPop(key);
    }

    /**
     * 弹出列表右侧元素
     *
     * @param key Redis key
     * @return 右侧元素
     */
    @SuppressWarnings("unchecked")
    public <T> T lRightPop(String key) {
        return (T) redisTemplate.opsForList().rightPop(key);
    }

    /**
     * 获取列表长度
     *
     * @param key Redis key
     * @return 列表长度
     */
    public Long lSize(String key) {
        return redisTemplate.opsForList().size(key);
    }

    // ==================== Set操作 ====================

    /**
     * 向集合添加元素
     *
     * @param key    Redis key
     * @param values 元素
     * @return 添加的元素数量
     */
    public Long sAdd(String key, Object... values) {
        return redisTemplate.opsForSet().add(key, values);
    }

    /**
     * 获取集合所有元素
     *
     * @param key Redis key
     * @return 元素集合
     */
    @SuppressWarnings("unchecked")
    public <T> Set<T> sMembers(String key) {
        return (Set<T>) redisTemplate.opsForSet().members(key);
    }

    /**
     * 判断集合是否包含元素
     *
     * @param key   Redis key
     * @param value 元素
     * @return 是否包含
     */
    public Boolean sIsMember(String key, Object value) {
        return redisTemplate.opsForSet().isMember(key, value);
    }

    /**
     * 获取集合大小
     *
     * @param key Redis key
     * @return 集合大小
     */
    public Long sSize(String key) {
        return redisTemplate.opsForSet().size(key);
    }

    /**
     * 删除集合元素
     *
     * @param key    Redis key
     * @param values 元素
     * @return 删除的元素数量
     */
    public Long sRemove(String key, Object... values) {
        return redisTemplate.opsForSet().remove(key, values);
    }

    // ==================== ZSet操作 ====================

    /**
     * 向有序集合添加元素
     *
     * @param key   Redis key
     * @param value 元素
     * @param score 分数
     * @return 是否添加成功
     */
    public Boolean zAdd(String key, Object value, double score) {
        return redisTemplate.opsForZSet().add(key, value, score);
    }

    /**
     * 获取有序集合指定范围的元素
     *
     * @param key   Redis key
     * @param start 开始位置
     * @param end   结束位置
     * @return 元素集合
     */
    @SuppressWarnings("unchecked")
    public <T> Set<T> zRange(String key, long start, long end) {
        return (Set<T>) redisTemplate.opsForZSet().range(key, start, end);
    }

    /**
     * 获取有序集合指定分数范围的元素
     *
     * @param key Redis key
     * @param min 最小分数
     * @param max 最大分数
     * @return 元素集合
     */
    @SuppressWarnings("unchecked")
    public <T> Set<T> zRangeByScore(String key, double min, double max) {
        return (Set<T>) redisTemplate.opsForZSet().rangeByScore(key, min, max);
    }

    /**
     * 获取元素排名
     *
     * @param key   Redis key
     * @param value 元素
     * @return 排名
     */
    public Long zRank(String key, Object value) {
        return redisTemplate.opsForZSet().rank(key, value);
    }

    /**
     * 获取元素分数
     *
     * @param key   Redis key
     * @param value 元素
     * @return 分数
     */
    public Double zScore(String key, Object value) {
        return redisTemplate.opsForZSet().score(key, value);
    }

    /**
     * 移除有序集合元素
     *
     * @param key    Redis key
     * @param values 元素
     * @return 移除的元素数量
     */
    public Long zRemove(String key, Object... values) {
        return redisTemplate.opsForZSet().remove(key, values);
    }

    // ==================== 其他操作 ====================

    /**
     * 重命名key
     *
     * @param oldKey 旧key
     * @param newKey 新key
     */
    public void rename(String oldKey, String newKey) {
        redisTemplate.rename(oldKey, newKey);
    }

    /**
     * 获取RedisTemplate对象，用于复杂操作
     *
     * @return RedisTemplate
     */
    public RedisTemplate<String, Object> getRedisTemplate() {
        return redisTemplate;
    }
}
