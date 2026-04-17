# MDC链路追踪使用指南

## 概述

本项目基于Logback MDC实现了完整的链路追踪系统，支持多模块、多服务场景下的统一追踪标识。

## 核心组件

### 1. platform-common-core

- **MdcConstants**: MDC键名常量定义
- **TraceIdGenerator**: 分布式追踪ID生成器（支持Snowflake、UUID、时间戳三种算法）
- **MdcUtils**: MDC操作工具类（初始化、清理、跨线程传递）

### 2. platform-common-web

- **MdcInterceptor**: HTTP请求拦截器，自动初始化和清理MDC
- **MdcWebMvcConfig**: Web MVC配置，注册MDC拦截器
- **AsyncMdcConfig**: 异步配置，确保异步任务继承MDC上下文

### 3. platform-common-db

- **MdcMyBatisInterceptor**: MyBatis拦截器，数据库操作包含链路追踪
- **MdcMyBatisConfig**: MyBatis配置，注册MDC拦截器

## 配置说明

### application.yml 配置

```yaml
spring:
  application:
    name: platform-auth-service
  profiles:
    active: dev

# MDC相关配置
mybatis:
  mdc:
    enable-sql-time: true        # 启用SQL执行时间统计
    enable-sql-params: false    # 启用SQL参数打印
    slow-sql-threshold: 1000    # 慢SQL阈值（毫秒）

# 日志配置
logging:
  config: classpath:logback-spring.xml
  level:
    com.zzl.platform: DEBUG
```

## 使用场景

### 1. HTTP请求追踪（自动处理）

```java
@RestController
@RequestMapping("/api/user")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @GetMapping("/{id}")
    public Result<User> getUser(@PathVariable Long id) {
        // MDC上下文已由MdcInterceptor自动初始化
        logger.info("查询用户信息: userId={}", id);

        User user = userService.findById(id);
        return Result.success(user);
    }
}
```

### 2. 跨服务调用追踪

```java
@Service
public class OrderService {

    @Autowired
    private RestTemplate restTemplate;

    public Order createOrder(Long userId, List<Long> productIds) {
        String traceId = MdcUtils.getTraceId();

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Trace-Id", traceId);
        headers.set("X-Parent-Trace-Id", traceId); // 设置父级追踪ID

        HttpEntity<?> entity = new HttpEntity<>(headers);
        Order order = restTemplate.postForObject(
                "http://order-service/api/order/create",
                entity,
                Order.class
        );

        return order;
    }
}
```

### 3. 异步任务追踪

```java
@Service
public class NotificationService {

    @Autowired
    private AsyncTaskExecutor asyncTaskExecutor;

    public void sendNotificationAsync(Long userId, String message) {
        // 使用MDC工具类包装异步任务
        Runnable task = MdcUtils.wrap(() -> {
            logger.info("发送异步通知: userId={}, message={}", userId, message);
            // 执行发送逻辑
        });

        asyncTaskExecutor.execute(task);
    }
}

// 或者使用@Async注解
@Service
public class EmailService {

    @Async("asyncExecutor")
    public void sendEmail(String to, String subject, String content) {
        // MDC上下文会自动传递
        logger.info("发送邮件: to={}, subject={}", to, subject);
        // 发送邮件逻辑
    }
}
```

### 4. 线程池追踪

```java
@Component
public class DataProcessor {

    private final ExecutorService executorService = Executors.newFixedThreadPool(10);

    public void processData(List<Data> dataList) {
        for (Data data : dataList) {
            // 使用MDC工具类包装任务
            executorService.submit(MdcUtils.wrap(() -> {
                logger.info("处理数据: dataId={}", data.getId());
                processData(data);
            }));
        }
    }
}
```

### 5. 自定义MDC信息

```java
@Service
public class BusinessService {

    public void processBusiness(Long businessId) {
        // 设置业务类型
        MdcUtils.setBusinessType("ORDER_PROCESS");

        // 设置租户ID
        MdcUtils.setTenantId("tenant-001");

        logger.info("处理业务: businessId={}", businessId);

        // 业务逻辑...
    }
}
```

### 6. 手动管理MDC上下文

```java
public void customMethod() {
    try {
        // 初始化MDC上下文
        MdcUtils.init();

        // 设置自定义信息
        MdcUtils.setBusinessType("CUSTOM_PROCESS");
        MdcUtils.setRequest("/api/custom", "POST");

        logger.info("执行自定义方法");

        // 执行业务逻辑...

    } finally {
        // 清理MDC上下文，避免内存泄漏
        MdcUtils.clear();
    }
}
```

## 请求头规范

跨服务调用时，以下请求头会自动传递或需要手动设置：

| 请求头               | 说明     | 自动传递 |
|-------------------|--------|------|
| X-Trace-Id        | 追踪ID   | 是    |
| X-Parent-Trace-Id | 父级追踪ID | 是    |
| X-User-Id         | 用户ID   | 是    |
| X-Username        | 用户名    | 是    |
| X-Tenant-Id       | 租户ID   | 是    |

## 日志输出格式

### 开发环境格式

```
2026-04-17 14:30:15.123 [http-nio-8080-exec-1] INFO  c.z.p.a.controller.UserController - 1a2b3c4d5e6f user-001 - 查询用户信息: userId=1
```

### 生产环境JSON格式

```
2026-04-17 14:30:15.123|1a2b3c4d5e6f|parent-001|user-001|admin|tenant-001|192.168.1.100|/api/user/1|GET|ORDER_PROCESS|platform-auth-service|prod|INFO|c.z.p.a.controller.UserController|查询用户信息: userId=1
```

## 性能优化建议

1. **异步日志输出**: 生产环境使用AsyncAppender包装文件输出
2. **日志级别控制**: 生产环境设置为INFO级别，开发环境DEBUG
3. **SQL追踪开关**: 生产环境关闭enableSqlParams，避免敏感信息泄露
4. **慢SQL监控**: 根据实际业务调整slowSqlThreshold

## 常见问题

### Q1: 异步任务中获取不到traceId

**A**: 确保使用@Async("asyncExecutor")或MdcUtils.wrap()包装异步任务。

### Q2: 数据库日志中没有traceId

**A**: 检查是否正确配置了MdcMyBatisInterceptor。

### Q3: 内存泄漏怎么办

**A**: 确保在finally块中调用MdcUtils.clear()，或者使用MdcInterceptor自动管理。

### Q4: 跨服务调用追踪ID不连续

**A**: 确保正确设置了X-Parent-Trace-Id请求头。

## 监控与告警

建议配合以下监控系统：

1. **ELK Stack**: 通过json.log文件收集日志
2. **Prometheus**: 监控慢SQL和请求响应时间
3. **Grafana**: 可视化链路追踪数据
4. **Sentry**: 错误日志追踪和告警

## 注意事项

1. **避免过度使用**: MDC信息过多会影响性能，只添加必要的追踪信息
2. **线程安全**: MDC基于ThreadLocal，务必确保在finally块中清理
3. **敏感信息**: 不要在MDC中存储密码、token等敏感信息
4. **容量控制**: 追踪ID建议控制在32字符以内，避免日志过大
