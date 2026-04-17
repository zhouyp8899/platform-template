# MDC链路追踪集成指南

## 概述

本文档说明如何将MDC链路追踪系统集成到Gateway和Auth-Service模块，实现完整的服务间链路传递和内存泄漏防护。

## 架构说明

### 链路传递流程

```
客户端请求
    ↓
Gateway层 (MdcGatewayFilter)
    ↓ 生成/接收TraceId
    ↓ 传递X-Trace-Id、X-Parent-Trace-Id等请求头
下游服务 (MdcInterceptor)
    ↓ 接收TraceId
    ↓ 设置到MDC
    ↓ Controller → Service → Mapper
    ↓ 数据库操作 (MdcMyBatisInterceptor)
    ↓ MdcInterceptor清理MDC
    ↓ Gateway清理MDC
返回响应
```

### 核心组件

| 模块           | 组件                         | 功能              |
|--------------|----------------------------|-----------------|
| Gateway      | MdcGatewayFilter           | 响应式MDC上下文管理     |
| Gateway      | ReactiveMdcContext         | 响应式MDC工具类       |
| Gateway      | RequestTraceFilter         | 请求追踪过滤器         |
| Auth-Service | MdcInterceptor             | HTTP拦截器         |
| Auth-Service | MdcRestTemplateInterceptor | RestTemplate拦截器 |
| Auth-Service | MdcTestController          | 测试控制器           |

## Gateway集成

### 1. 已完成的工作

#### 文件清单

```
platform-gateway/
├── main/com/zzl/platform/gw/
│   ├── reactive/
│   │   └── ReactiveMdcContext.java           # 响应式MDC上下文管理
│   └── filter/
│       └── MdcGatewayFilter.java              # Gateway MDC过滤器
├── resource/
│   ├── logback-spring.xml                      # 日志配置
│   └── application.yml                         # 应用配置（已更新）
```

#### 配置说明

**build.gradle**

```gradle
dependencies {
    // 新增依赖
    implementation project(':platform-common-web')
    // ...其他依赖
}
```

**application.yml**

```yaml
logging:
  config: classpath:logback-spring.xml  # 新增日志配置
```

### 2. 功能特性

#### 2.1 自动链路追踪

Gateway自动为每个请求生成或接收TraceId，并传递给下游服务：

```java
// Gateway自动处理
客户端请求 → Gateway生成TraceId → 传递给下游服务
```

#### 2.2 响应式上下文管理

专门为Gateway的Reactor非阻塞模式设计：

```java
// ReactiveMdcContext提供响应式场景的MDC管理
public class ReactiveMdcContext {
    public static String init(String traceId);
    public static Map<String, String> capture();
    public static void restore(Map<String, String> contextMap);
    public static void clear();
}
```

#### 2.3 内存泄漏防护

**关键设计点**：

1. `doFinally()`确保请求完成后清理MDC
2. `Mono.defer()`确保上下文正确传递
3. 使用`contextWrite()`传递上下文到Reactor流

```java
return chain.filter(exchange)
    .doFinally(signalType -> {
        // finally中清理MDC，防止内存泄漏
        MdcUtils.clear();
    });
```

## Auth-Service集成

### 1. 已完成的工作

#### 文件清单

```
platform-auth-service/
├── src/main/java/com/zzl/platform/auth/
│   ├── config/
│   │   ├── MdcApplicationConfig.java         # MDC应用配置
│   │   ├── MdcRestTemplateInterceptor.java     # RestTemplate拦截器
│   │   ├── RestTemplateConfig.java            # RestTemplate配置
│   │   └── MyBatisConfig.java               # MyBatis配置
│   └── controller/
│       └── MdcTestController.java            # MDC测试控制器
└── src/main/resources/
    ├── logback-spring.xml                    # 日志配置
    └── application.yml                       # 应用配置（已更新）
```

### 2. 功能特性

#### 2.1 HTTP请求拦截

`MdcInterceptor`自动拦截HTTP请求，管理MDC生命周期：

```java
@Component
public class MdcInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(...) {
        // 初始化MDC上下文
        MdcUtils.init();
        // 设置用户信息、客户端IP等
        return true;
    }

    @Override
    public void afterCompletion(...) {
        // 清理MDC上下文，防止内存泄漏
        MdcUtils.clear();
    }
}
```

#### 2.2 服务间调用传递

`MdcRestTemplateInterceptor`自动传递MDC信息到下游服务：

```java
@Component
public class MdcRestTemplateInterceptor implements ClientHttpRequestInterceptor {
    @Override
    public ClientHttpResponse intercept(...) {
        // 获取当前追踪ID
        String traceId = MdcUtils.getTraceId();

        // 设置请求头传递给下游
        request.getHeaders().set("X-Trace-Id", traceId);
        request.getHeaders().set("X-User-Id", userId);
        // ...

        return execution.execute(request, body);
    }
}
```

**使用示例**：

```java
@Service
public class OrderService {
    @Autowired
    private RestTemplate restTemplate;

    public Order createOrder(...) {
        // MDC信息会自动传递
        Order order = restTemplate.postForObject(
            "http://order-service/api/order/create",
            request,
            Order.class
        );
        return order;
    }
}
```

#### 2.3 数据库操作追踪

`MdcMyBatisInterceptor`确保数据库操作包含链路追踪信息：

```java
@Intercepts({
    @Signature(type = Executor.class, method = "update", ...),
    @Signature(type = Executor.class, method = "query", ...)
})
public class MdcMyBatisInterceptor implements Interceptor {
    @Override
    public Object intercept(Invocation invocation) {
        // 确保MDC上下文存在
        if (!MdcUtils.hasTraceId()) {
            MdcUtils.init();
        }

        // 执行SQL
        Object result = invocation.proceed();

        return result;
    }
}
```

#### 2.4 异步任务追踪

通过`MdcUtils.wrap()`确保异步任务继承MDC上下文：

```java
@Service
public class NotificationService {
    private final ExecutorService executor = Executors.newFixedThreadPool(4);

    public void sendNotificationAsync(...) {
        // 使用MdcUtils.wrap()包装异步任务
        executor.submit(MdcUtils.wrap(() -> {
            logger.info("发送异步通知"); // MDC信息会自动包含
            // 业务逻辑...
        }));
    }
}
```

## 使用指南

### 1. 基础使用

#### 1.1 Controller层（自动处理）

```java
@RestController
@RequestMapping("/api/user")
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @GetMapping("/{id}")
    public Result<User> getUser(@PathVariable Long id) {
        // MDC已由MdcInterceptor自动初始化
        logger.info("查询用户: userId={}", id);

        User user = userService.findById(id);
        return Result.success(user);
    }
}
```

#### 1.2 Service层（自动继承）

```java
@Service
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    public User findById(Long id) {
        logger.info("Service层查询: id={}", id); // MDC信息自动包含
        return userMapper.selectById(id);
    }
}
```

### 2. 高级使用

#### 2.1 设置自定义MDC字段

```java
public void processBusiness() {
    // 设置业务类型
    MdcUtils.setBusinessType("ORDER_PROCESS");

    // 设置租户ID
    MdcUtils.setTenantId("tenant-001");

    logger.info("处理业务逻辑");
}
```

#### 2.2 创建子追踪ID

```java
public void callDownstreamService() {
    String currentTraceId = MdcUtils.getTraceId();

    // 创建子追踪ID
    String childTraceId = MdcUtils.createChildTraceId(currentTraceId);

    logger.info("调用下游服务: childTraceId={}", childTraceId);

    // ...调用逻辑...

    // 恢复父级追踪ID
    MdcUtils.restoreParentTraceId();
}
```

#### 2.3 手动管理MDC

```java
public void customMethod() {
    try {
        // 初始化MDC
        MdcUtils.init();

        // 设置自定义信息
        MdcUtils.setBusinessType("CUSTOM");
        MdcUtils.setRequest("/api/custom", "POST");

        logger.info("执行自定义方法");

    } finally {
        // 清理MDC（重要！防止内存泄漏）
        MdcUtils.clear();
    }
}
```

## 测试验证

### 1. 启动服务

```bash
# 启动Gateway
cd platform-gateway
./gradlew bootRun

# 启动Auth-Service
cd platform-auth-service
./gradlew bootRun
```

### 2. 测试接口

#### 2.1 基础测试

```bash
curl http://localhost:7100/api/mdc-test/basic
```

**预期输出**：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "traceId": "1a2b3c4d...",
    "userId": "N/A",
    "clientIp": "127.0.0.1",
    "requestUri": "/api/mdc-test/basic",
    "requestMethod": "GET"
  }
}
```

#### 2.2 异步任务测试

```bash
curl http://localhost:7100/api/mdc-test/async
```

**预期输出**：所有异步任务共享同一个traceId

#### 2.3 嵌套调用测试

```bash
curl http://localhost:7100/api/mdc-test/nested
```

**预期输出**：每一级调用都有自己的traceId，通过parentTraceId关联

#### 2.4 服务间调用测试

```bash
curl http://localhost:7100/api/mdc-test/service-call?targetUrl=http://localhost:7200
```

**预期输出**：上游和下游服务的traceId相同

### 3. 日志验证

查看日志文件：

```bash
# Gateway日志
tail -f logs/platform-gateway/all.log

# Auth-Service日志
tail -f logs/platform-auth-service/all.log
```

**预期日志格式**：

```
2026-04-17 14:30:15.123 [http-nio-8080-exec-1] INFO  c.z.p.a.controller.UserController 45 - traceId=1a2b3c4d, userId=user-001, clientIp=192.168.1.100, uri=/api/user/1 - 查询用户: userId=1
```

## 内存泄漏防护

### 1. 防护措施

| 组件                    | 防护机制                    |
|-----------------------|-------------------------|
| MdcInterceptor        | afterCompletion()中清理MDC |
| AsyncMdcConfig        | TaskDecorator确保清理       |
| MdcGatewayFilter      | doFinally()中清理MDC       |
| MdcMyBatisInterceptor | try-finally块清理          |

### 2. 最佳实践

#### 2.1 总是使用try-finally

```java
// ✅ 正确
try {
    MdcUtils.init();
    // 业务逻辑
} finally {
    MdcUtils.clear();
}

// ❌ 错误
MdcUtils.init();
// 业务逻辑（如果抛出异常，MDC不会清理）
```

#### 2.2 使用MdcUtils.wrap()包装异步任务

```java
// ✅ 正确
executor.submit(MdcUtils.wrap(() -> {
    // 业务逻辑
}));

// ❌ 错误
executor.submit(() -> {
    // 业务逻辑（MDC上下文不会传递）
});
```

#### 2.3 避免在finally中再次抛出异常

```java
try {
    MdcUtils.init();
    // 业务逻辑
} finally {
    try {
        MdcUtils.clear();
    } catch (Exception e) {
        // 记录但不抛出，确保finally块完成
        log.error("清理MDC失败", e);
    }
}
```

### 3. 内存泄漏检测

使用以下方法检测潜在的内存泄漏：

#### 3.1 使用VisualVM

```bash
# 启动应用时添加JVM参数
jvisualvm
```

检查ThreadLocal的使用情况，确保MDC没有累积。

#### 3.2 监控MDC上下文数量

```java
// 添加监控端点
@GetMapping("/actuator/mdc-stats")
public Map<String, Object> getMdcStats() {
    Map<String, Object> stats = new HashMap<>();
    stats.put("threadCount", Thread.activeCount());
    stats.put("mdcContextSize", MdcUtils.getCopyOfContextMap() != null ?
        MdcUtils.getCopyOfContextMap().size() : 0);
    return stats;
}
```

## 性能优化

### 1. 日志级别控制

生产环境使用INFO级别：

```yaml
logging:
  level:
    root: INFO
    com.zzl.platform: INFO
    org.springframework: WARN
```

### 2. 异步日志输出

使用AsyncAppender提高性能：

```xml
<appender name="ASYNC_FILE_ALL" class="ch.qos.logback.classic.AsyncAppender">
    <appender-ref ref="FILE_ALL"/>
    <queueSize>512</queueSize>
    <discardingThreshold>0</discardingThreshold>
</appender>
```

### 3. SQL追踪优化

生产环境关闭SQL参数打印：

```yaml
mybatis:
  mdc:
    enable-sql-time: true
    enable-sql-params: false    # 生产环境关闭
```

## 故障排查

### 1. TraceId丢失

**症状**：日志中显示traceId=N/A

**排查步骤**：

1. 检查MdcInterceptor是否正确注册
2. 检查请求头是否正确传递
3. 检查是否在异步线程中未使用MdcUtils.wrap()

### 2. 内存持续增长

**症状**：应用长时间运行后内存增长

**排查步骤**：

1. 使用VisualVM检查ThreadLocal
2. 检查是否有未清理的MDC上下文
3. 检查线程池配置是否合理

### 3. 下游服务TraceId不一致

**症状**：Gateway和下游服务的TraceId不同

**排查步骤**：

1. 检查Gateway是否正确设置X-Trace-Id请求头
2. 检查下游服务MdcInterceptor是否正确接收
3. 检查网络代理是否修改了请求头

## 总结

### 已完成的集成

✅ Gateway层MDC集成
✅ Auth-Response层MDC集成
✅ 服务间链路传递
✅ 异步任务MDC传递
✅ 数据库操作追踪
✅ 内存泄漏防护
✅ 测试接口和文档

### 下一步工作

- [ ] 集成其他服务模块
- [ ] 添加性能监控指标
- [ ] 集成OpenTelemetry
- [ ] 添加链路追踪UI
- [ ] 完善单元测试

### 关键要点

1. **链路传递**'通过HTTP请求头自动传递，无需手动处理
2. **内存防护**：所有拦截器都有finally清理机制
3. **异步支持**'使用MdcUtils.wrap()确保上下文传递
4. **配置简单**：添加依赖和配置即可使用
