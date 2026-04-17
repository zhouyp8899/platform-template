# Gateway服务发现排查指南

## 问题描述

```
503 SERVICE_UNAVAILABLE "Unable to find instance for platform-auth-service"
```

## 代码层检查清单

### 1. 服务名称一致性检查

#### 检查点1：Auth-Service服务名称

**文件：** `platform`-auth-service/src/main/resources/bootstrap.yml`

```yaml
spring:
  application:
    name: platform-auth-service  # ← 确认此名称
```

#### 检查点2：Gateway路由配置

**文件：** `platform-gateway/resource/application-gateway.yml`

```yaml
- id: auth-route
  uri: lb://platform-auth-service  # ← 必须与上面一致
```

**验证方法：**

```bash
# 查看Auth-Service启动日志
grep "application.name" logs/platform-auth-service/all.log

# 应该看到：
# platform-auth-service服务已启动
```

### 2. Nacos注册检查

#### 检查点1：服务发现配置

**Auth-Service bootstrap.yml：**

```yaml
spring:
  cloud:
    nacos:
      discovery:
        enabled: true
        server-addr: ${NACOS_ADDR:127.0.0.1:8848}
        group: ${NACOS_GROUP:DEFAULT_GROUP}  # ←← 确认group
        namespace: ${NACOS_NAMESPACE:}  # ←← 确认namespace
```

**Gateway bootstrap.yml：**

```yaml
spring:
  cloud:
    nacos:
      discovery:
        enabled: true
        server-addr: ${NACOS_ADDR:127.0.0.1:8848}
        group: ${NACOS_GROUP:DEFAULT_GROUP}  # ←← 必须与上面一致
        namespace: ${NACOSraNamespace:}  # ←← 必须与上面一致
```

#### 检查点2：Nacos控制台验证

访问：http://127.0.0.1:8848/nacos

1. 进入"服务管理"→"服务列表"
2. 搜索`platform-auth-service`
3. 确认以下信息：
    - ✅ 服务名称：platform-auth-service
    - ✅ 实例数量：> 0
    - ✅ 健康状态：UP（如果启用了健康检查）
    - ✅ IP和端口正确

**如果服务未注册，检查：**

```bash
# 1. 确认Auth-Service启动成功
curl http://localhost:7200/actuator/health

# 2. 确认Nacos连接正常
curl http://127.0.0.1:8848/nacos/v1/ns/instance/list?serviceName=platform-auth-service

# 3. 查看Auth-Service启动日志
grep -A 10 "Nacos" logs/platform-auth-service/all.log
```

### 3. Gateway路由配置检查

#### 检查点1：路由配置加载

**文件：** `platform-gateway/resource/bootstrap.yml`

```yaml
spring:
  config:
    import:
      - classpath:application-gateway.yml  # ←← 确认路由配置被加载
```

#### 检查点2：路由配置格式

**application-gateway.yml：**

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: auth-route
          uri: lb://platform-auth-service  # ←必须使用lb://前缀
          predicates:
            - Path=/api/auth/**  # ←路径规则
```

**常见错误：**

- ❌ `uri: http://platform-auth-service` → 正确：`uri: lb://platform-auth-service`
- ❌ `predicates: - Path=/api/auth` → 正确：`predicates: - Path=/api/auth/**`
- ❌ `id: route-1` → 正确：`id: auth-route`（唯一且描述性）

#### 检查点3：路由信息查看

```bash
# 查看Gateway已加载的路由
curl http://localhost:7100/actuator/gateway/routes | jq

# 预期响应：
{
  "route_id": "auth-route",
  "route_uri": "lb://platform-auth-service",
  "predicates": [
    {
      "name": "Path",
      "args": {
        "pattern": "/api/auth/**"
      }
    }
  ]
}
```

### 4. 代码层过滤器检查

#### 检查点1：过滤器执行顺序

**MdcGatewayFilter.java：**

```java
@Override
public int getOrder() {
    return Ordered.HIGHEST_PRECEDENCE;  // 最高优先级
}
```

**AuthFilter.java：**

```java
@Override
public int getOrder() {
    return HIGHEST_PRECEDENCE - 1;  // 应该在MDC过滤器之后
}
```

#### 检查点2：过滤器是否影响路由

**潜在问题：** 如果过滤器中调用了`chain.filter(exchange)`，但传递了修改后的exchange，可能导致路由匹配失败。

**检查方法：**

```bash
# 查看Gateway日志
grep "MDC" logs/platform-gateway/all.log

# 查看路由匹配日志
grep "RoutePredicate" logs/platform-gateway/all.log
```

### 5. 网络连接检查

#### 检查点1：端口连通性

```bash
# 检查Auth-Service端口
netstat -an | grep 7200

# 测试直连Auth-Service
curl http://localhost:7200/actuator/health

# 测试通过Nacos的服务名
#（需要Nacos客户端）
```

#### 检查点2：防火墙配置

Windows防火墙检查：

```powershell
# 查看防火墙规则
Get-NetFirewallRule

# 如需要，允许端口：
New-NetFirewallRule -DisplayName "Auth Service" `
    -DirectionLocalToRemote -LocalPort7200 `
    -ProtocolTCP -ActionAllow
```

## 详细排查步骤

### 步骤1：确认服务状态

```bash
# 1. 检查Auth-Service进程
ps aux | grep "platform-auth-service"

# 2. 检查端口占用
netstat -an | grep -E ":(7200|7100)"

# 3. 检查健康状态
curl http://localhost:7200/actuator/health
curl http://localhost:7100/actuator/health
```

### 步骤2：确认Nacos注册

```bash
# 1. 查看Nacos中的所有服务
curl http://127.0.0.1:8848/nacos/v1/ns/service/list

# 2. 查看platform-auth-service的实例
curl http://127.0.0.1:8848/nacos/v1/ns/instance/list?serviceName=platform-auth-service

# 3. 预期响应：
{
  "hosts": [
    {
      "port": 7200,
      "valid": true,
      "healthy": true,
      "weight": 1.0,
      "metadata": {},
      "instanceId": "xxx",
      "ip": "xxx",
      "serviceName": "platform-auth-service"
    }
  ]
}
```

### 步骤3：确认Gateway路由

```bash
# 1. 查看所有路由
curl http://localhost:7100/actuator/gateway/routes

# 2. 查看路由处理器
curl http://localhost:7100/actuator/gateway/routepredicates

# 3. 查看全局过滤器
curl http://localhost:7100/actuator/gateway/globalfilters
```

### 步骤4：测试请求

```bash
# 1. 直连Auth-Service（跳过Gateway）
curl http://localhost:7200/api/mdc-test/basic

# 2. 通过Gateway调用
curl http://localhost:7100/api/mdc-test/basic

# 3. 查看Gateway日志
tail -f logs/platform-gateway/all.log | grep -E "traceId|Unable to find"
```

## 常见问题与解决方案

### 问题1：服务注册但Gateway找不到

**原因：** Nacos group/namespace不匹配

**解决方案：**

```yaml
# 确保两个服务使用相同的Nacos配置
spring:
  cloud:
    nacos:
      discovery:
        server-addr: 127.0.0.1:8848
        group: DEFAULT_GROUP  # ← 必须一致
        namespace:          # ← 必须一致
```

### 问题2：路由配置未生效

**原因：** application-gateway.yml未正确加载

**解决方案：**

```yaml
# bootstrap.yml中添加导入
spring:
  config:
    import:
      - classpath:application-gateway.yml
```

验证路由已加载：

```bash
curl http://localhost:7100/actuator/gateway/routes | grep "platform-auth-service"
```

### 问题3：实例已注册但不可用

**原因：** 健康检查失败或实例标记为不健康

**解决方案：**

```yaml
# application.yml中配置健康检查
spring:
  cloud:
    nacos:
      discovery:
        health-check-interval: 10000  # 健康检查间隔
        health-check-url: /actuator/health  # 健康检查URL
        health-check-timeout: 3000
```

### 问题4：Gateway与Auth-Service网络不通

**原因：** 虚拟网卡配置问题或防火墙

**解决方案：**

```bash
# 1. 测试直连
curl http://localhost:7200/actuator/health

# 2. 如果失败，检查监听地址
# 确认服务监听在 0.0.0.0 而非 127.0.0.1
```

## 调试技巧

### 1. 启用详细日志

**Gateway application.yml：**

```yaml
logging:
  level:
    org.springframework.cloud.gateway: DEBUG
    org.springframework.cloud.loadbalancer: DEBUG
    com.alibaba.nacos.client.naming: DEBUG
```

### 2. 添加调试端点

**Gateway配置类：**

```java
@Bean
public ActuatorHttpTraceRepository actuatorHttpTraceRepository() {
    return new InMemoryHttpTraceRepository();
}
```

### 3. 手动触发服务发现

```bash
# 刷新Gateway的配置
curl -X POST http://localhost:7100/actuator/refresh
```

## 验证清单

完成以下检查项：

- [ ] Auth-Service启动成功（查看日志）
- [ ] Auth-Service健康检查通过（/actuator/health）
- [ ] Auth-Service已注册到Nacos（控制台确认）
- [ ] Nacos中实例状态为healthy
- [ ] Gateway启动成功（查看日志）
- [ ] Gateway路由已加载（/actuator/gateway/routes）
- [ ] Gateway与Auth-Service网络连通（直连测试）
- [ ] 通过Gateway的MDC测试接口可访问
- [ ] 链路追踪ID在日志中正确显示

## 快速修复脚本

如果以上检查都正常，尝试以下快速修复：

```bash
# 1. 重启Gateway
curl -X POST http://localhost:7100/actuator/shutdown

# 2. 清除Nacos缓存
# 在Nacos控制台删除服务实例，等待重新注册

# 3. 使用负载均衡器直接测试
# application-gateway.yml中添加调试路由：
- id: debug-route
  uri: http://localhost:7200  # 直接IP而非lb://
  predicates:
    - Path=/debug/**
```

## 下一步

如果问题仍然存在：

1. 提供完整的错误日志
2. 提供/actuator/gateway/routes输出
3. 提供Nacos服务列表截图
4. 确认防火墙和网络配置
