# platform-common-redis

## 概述

`platform-common-redis` 是平台通用的Redis能力封装模块，提供完整的Redis操作能力，包括基础操作、分布式锁、限流、缓存等功能。其他业务模块只需依赖此模块即可直接使用所有Redis功能。

## 功能特性

### 1. 基础Redis操作 (RedisService)

- String操作：set/get/delete/increment/setIfAbsent等
- Hash操作：hSet/hGet/hSetAll/hGetAll/hDelete等
- List操作：lLeftPush/lRightPush/lRange/lLeftPop/lRightPop等
- Set操作：sAdd/sMembers/sIsMember/sSize等
- ZSet操作：zAdd/zRange/zRank/zScore/zRemove等
- 通用操作：exists/expire/rename等

### 2. 分布式锁 (RedisLockService)

基于Redisson实现，支持：

- 可重入锁
- 公平锁
- 读写锁
- 锁等待和持有时间配置
- 带锁的代码块执行

### 3. 限流功能 (RedisRateLimiterService)

提供多种限流算法：

- 固定窗口限流
- 令牌桶限流
- 滑动窗口限流
- 支持自定义限流策略

### 4. 缓存操作 (RedisCacheService)

- 编程式缓存操作
- 注解式缓存支持（@Cached, @CacheUpdate, @CacheEvict）
- 缓存穿透保护（getOrLoad）
- 批量缓存操作

### 5. 工具类

- RedisKeyBuilder：统一的Key命名规范和构建
- RedisProperties：可配置的Redis属性

## 快速开始

### 1. 添加依赖

```gradle
dependencies {
    api project(':platform-common-redis')
}
```

### 2. 配置Redis

参考 `redis-example.yml` 配置文件，将配置复制到你的 `application.yml` 中：

```yaml
platform:
  redis:
    enabled: true
    lock:
      wait-time: 30
      lease-time: 30
    cache:
      default-expire: 3600

spring:
  data:
    redis:
      host: localhost
      port: 6379
      database: 0
```

### 3. 使用示例

#### 基础操作

```java
@Autowired
private RedisService redisService;

// 设置值
redisService.set("key", "value");

// 获取值
String value = redisService.get("key");

// 设置带过期时间的值
redis.set("key", "value", 60, TimeUnit.SECONDS);

// 递增
Long count = redisService.increment("counter", 1);
```

#### 缓存操作

```java
@Autowired
private RedisCacheService cacheService;

// 设置缓存
cacheService.set("user:1001", user, 30, TimeUnit.MINUTES);

// 获取缓存
User user = cacheService.get("user:1001");

// 缓存穿透保护
User user = cacheService.getOrLoad("user:1001", () -> {
    return userService.getUser(1001);
}, 30, TimeUnit.MINUTES);
```

#### 分布式锁

```java
@Autowired
private RedisLockServiceService lockService;

// 自动锁管理
lockService.executeWithLock("order:1001", () -> {
    // 执行需要加锁的业务逻辑
    return processOrder();
});

// 手动锁管理
if (lockService.tryLock("order:1001", 10, 30, TimeUnit.SECONDS)) {
    try {
        // 执行业务逻辑
        processOrder();
    } finally {
        lockService.unlock("order:1001");
    }
}
```

#### 限流

```java

@Autowired
private RedisRateLimiterService rateLimiterService;

// 固定窗口限流
rateLimiterService.

rateLimit("api:login",10,60); // 每分钟最多10次

// 令牌桶限流
rateLimiterService.

tokenBucketRateLimit("api:search",100,10); // 每秒10个令牌，桶容量100

// 滑动窗口限流
rateLimiterService.

slidingWindowRateLimit("api:order",50,60); // 60秒内最多50次
```

#### Key构建

```java
// 使用RedisKeyBuilder构建规范的Key
String hotelKey = RedisKeyBuilder.OtaPrefix.HOTEL_DETAIL.formatted("hotel123");
String roomStatusKey = RedisKeyBuilder.OtaPrefix.ROOM_STATUS.formatted("hotel123", "room456");

// 自定义Key构建
String customKey = RedisKeyBuilder.build("cache:{0}:{1}", "user", "123");
```

## 模块结构

```
platform-common-redis
├── src/main/java/com/zzl/platform/common/redis
│   ├── autoconfigure
│   │   └── RedisAutoConfiguration.java    # 自动配置类
│   ├── config
│   │   └── RedisConfig.java               # Redis配置类
│   ├── exception
│   │   ├── RedisException.java             # Redis异常基类
│   │   ├── RedisLockException.java         # 锁异常
│   │   └── RedisRateLimitException.java   # 限流异常
│   ├── properties
│   │   └── RedisProperties.java           # 配置属性类
│   ├── service
│   │   ├── RedisService.java              # 基础Redis操作
│   │   ├── RedisCacheService.java          # 缓存服务
│   │   ├── RedisLockService.java          # 分布式锁服务
│   │   └── RedisRateLimiterService.java   # 限流服务
│   └── utils
│       └── RedisKeyBuilder.java           # Key构建工具
├── src/main/resources
│   ├── META-INF/spring
│   │   └── org.springframework.boot.autoconfigure.AutoConfiguration.imports
│   └── redis-example.yml                  # 配置示例
└── build.gradle                           # 构建配置
```

## 最佳实践

1. **Key命名规范**：使用 `RedisKeyBuilder` 统一管理Key，避免Key冲突
2. **缓存穿透**：使用 `getOrLoad` 方法，避免缓存击穿
3. **分布式锁**：使用 `executeWithLock` 自动管理锁，避免死锁
4. **限流保护**：对热点接口添加限流保护，防止服务过载
5. **过期时间**：根据业务特性设置合理的缓存过期时间
6. **异常处理**：捕获 `RedisException` 进行统一处理

## 技术栈

- Spring Data Redis
- Redisson (分布式锁)
- Jackson (JSON序列化，支持Java 8时间类型)
- Spring Boot Auto Configuration

## 注意事项

1. 确保Redis服务正常运行
2. 配置合理的连接池参数
3. 注意分布式锁的持有时间，避免锁超时
4. 限流阈值需要根据实际业务情况调整
5. 在生产环境中启用Redis密码保护
