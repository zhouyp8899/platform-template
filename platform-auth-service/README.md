# 平台认证服务 (platform-auth-service)

## 概述

platform-auth-service 是酒店OTA平台的统一认证与权限管理服务，提供完整的RBAC权限管理系统。

## 架构设计

### 技术栈

- **Spring Boot 3.x** - 应用框架
- **MyBatis Plus** - ORM框架
- **Redis** - 缓存与分布式存储
- **JWT** - Token认证
- **Nacos** - 服务发现与配置中心

### 模块结构

```
platform-auth-service/
├── src/main/java/com/zzl/platform/auth/
│   ├── entity/          # 实体类 (12个实体类)
│   ├── mapper/          # MyBatis Mapper接口 (11个Mapper)
│   ├── service/         # 服务接口 (7个服务接口)
│   ├── service/impl/      # 服务实现 (6个服务实现)
│   ├── controller/      # 控制器 (5个控制器)
│   ├── dto/             # 数据传输对象 (17个DTO)
│   ├── vo/              # 视图对象 (10个VO)
│   ├── enums/           # 枚举类 (5个枚举)
│   ├── constants/       # 常量类 (2个常量类)
│   ├── aspect/          # AOP切面 (4个切面/注解)
│   ├── config/          # 配置类 (2个配置类)
│   └── security/        # 安全相关
└── src/main/resources/
    ├── mapper/           # MyBatis XML映射文件 (2个XML)
    └── schema.sql          # 数据库初始化脚本
    └── application.yml       # 应用配置
```

## 核心功能

### 1. 双链路认证

#### H5用户认证

- 路由前缀：`/api/h5/`
- Token类型：长期Token（30天）
- 认证方式：手机号+验证码
- 功能：
    - 发送验证码
    - 手机号登录
    - 刷新Token
    - 登出

#### 管理后台认证

- 路由前缀：`/api/admin/`
- Token类型：短期Token（2小时）
- 认证方式：用户名+密码
- 功能：
    - 账号密码登录
    - 刷新Token
    - 登出
    - 修改密码

### 2. RBAC权限管理

#### 用户管理

- 用户CRUD操作
- 用户角色分配
- 用户状态管理
- 重置密码
- 批量删除操作
- 登录失败锁定机制
- 数据权限配置

#### 角色管理

- 角色CRUD操作
- 角色权限分配
- 数据权限范围配置
- 系统角色保护

#### 权限管理

- 权限CRUD操作
- 权限分组管理
- 资源类型区分（接口/菜单/按钮）
- HTTP方法绑定

#### 菜单管理

- 菜单树形结构
- 菜单权限绑定
- 动态菜单渲染
- 菜单类型（目录/菜单/按钮）

#### 部门管理

- 部门树形结构
- 部门层级管理
- 用户部门关联

### 3. 权限校验机制

#### 注解式权限校验

```java
// 权限校验
@RequiresPermission(value = "system:user:add", desc = "用户新增")
public void addUser(UserAddRequest request) { }

// 角色校验
@RequiresRole(value = "ADMIN", desc = "管理员")
public void adminOperation() { }

// 数据权限
@DataScope(deptAlias = "d", userAlias = "u")
public PageResponse<UserVO> pageUsers(PageRequest request) { }
```

#### 权限校验切面

- 自动拦截带有权限注解的方法
- 从请求头获取用户信息
- 检查用户权限/角色
- 支持超级管理员跳过校验

### 4. 数据权限控制

支持多种数据权限范围：

- **ALL** - 全部数据
- **DEPT** - 本部门及子部门
- **DEPT_ONLY** - 仅本部门
- **SELF** - 仅本人数据
- **CUSTOM** - 自定义部门集合

### 5. 在线用户管理

- 在线用户列表
- 强制踢出用户
- 会话状态管理
- 自动清理过期会话（定时任务）

### 6. 操作审计

- 操作日志记录
- 审计信息跟踪（模块、业务类型、操作人、IP、地点）
- 性能监控（耗时统计）

## 数据库设计

### 核心表结构

| 表名                    | 说明        | 关键字段                                              |
|-----------------------|-----------|---------------------------------------------------|
| t_sys_user            | 用户表       | id, username, password, status                    |
| t_sys_role            | 角色表       | id, role_code, role_name, is_system               |
| t_sys_permission      | 权限表       | id, permission_code, resource_type, resource_path |
| t_sys_menu            | 菜单表       | id, menu_name, parent_id, menu_type               |
| t_sys_department      | 部门表       | id, dept_code, dept_name, parent_id               |
| t_sys_user_role       | 用户角色关联表   | user_id, role_id                                  |
| t_sys_role_permission | 角色权限关联表   | role_id, permission_id                            |
| t_sys_menu_permission | 菜单权限关联表   | menu_id, permission_id                            |
| t_sys_user_department | 用户部门关联表   | user_id, dept_id                                  |
| t_sys_role_dept       | 角色数据权限部门表 | role_id, dept_id                                  |
| t_sys_online_user     | 在线用户表     | user_id, login_time, expire_time                  |
| t_sys_operate_log     | 操作日志表     | module, oper_url, operator_name                   |

### 初始化数据

- 超级管理员角色（SUPER_ADMIN, ADMIN, USER）
- 超级管理员用户（admin/test，密码：admin123）
- 系统管理菜单
- 系统管理权限
- 部门数据（总部、技术部、产品部、运营部）

## API接口

### 认证接口

| 接口      | 方法   | 路由                                        | 说明        |
|---------|------|-------------------------------------------|-----------|
| 管理员登录   | POST | `/api/admin/auth/login`                   | 管理后台登录    |
| 刷新Token | POST | `/api/admin/auth/refresh`                 | 刷新访问Token |
| 登出      | POST | `/api/admin/auth/logout`                  | 退出登录      |
| 获取当前用户  | GET  | `/api/admin/auth/current`                 | 当前用户信息    |
| 修改密码    | PUT  | `/api/admin/auth/current/change-password` | 修改当前用户密码  |

| 发送验证码 | POST | `/api/h5/auth/send-code` | 发送手机验证码 |
| 手机号登录 | POST | `/api/h5/auth/login-phone` | H5验证码登录 |
| 刷新Token | POST | `/api/h5/auth/refresh` | 刷新访问Token |
| 登出 | POST | `/api/h5/auth/logout` | 退出登录 |

### 用户管理接口

| 接口       | 方法     | 路由                                      | 权限                 |
|----------|--------|-----------------------------------------|--------------------|
| 用户列表（分页） | POST   | `/api/admin/system/user/page`           | system:user:list   |
| 用户详情     | GET    | `/api/admin/system/user/{id}`           | system:user:get    |
| 用户新增     | POST   | `/api/admin/system/user/add`            | system:user:add    |
| 用户编辑     | PUT    | `/api/admin/system/user/edit`           | system:user:edit   |
| 用户删除     | DELETE | `/api/admin/system/user/delete/{id}`    | system:user:delete |
| 批量删除     | DELETE | `/api/admin/system/user/batch-delete`   | system:user:delete |
| 重置密码     | PUT    | `/api/admin/system/user/reset-password` | system:user:reset  |
| 修改状态     | PUT    | `/api/admin/system/user/change-status`  | system:user:edit   |
| 分配角色     | POST   | `/api/admin/system/user/grant-roles`    | system:user:grant  |
| 获取用户角色   | GET    | `/api/admin/system/user/{id}/roles`     | system:user:get    |

### 角色管理接口

| 接口       | 方法     | 路由                                         | 权限                 |
|----------|--------|--------------------------------------------|--------------------|
| 角色列表（分页） | POST   | `/api/admin/system/role/page`              | system:role:list   |
| 角色列表（全部） | GET    | `/api/admin/system/role/list`              | system:role:list   |
| 角色详情     | GET    | `/api/admin/system/role/{id}`              | system:role:get    |
| 角色新增     | POST   | `/api/admin/system/role/add`               | system:role:add    |
| 角色编辑     | PUT    | `/api/admin/system/role/edit`              | system:role:edit   |
| 角色删除     | DELETE | `/api/admin/system/role/delete/{id}`       | system:role:delete |
| 分配权限     | POST   | `/api/admin/system/role/grant-permissions` | system:role:grant  |
| 获取角色权限   | GET    | `/api/admin/system/role/{id}/permissions`  | system:role:get    |

### 菜单管理接口

| 接口        | 方法     | 路由                                   | 权限                 |
|-----------|--------|--------------------------------------|--------------------|
| 菜单树（全部）   | GET    | `/api/admin/system/menu/tree/all`    | system:menu:list   |
| 菜单树（当前用户） | GET    | `/api/admin/system/menu/tree`        | system:menu:list   |
| 菜单详情      | GET    | `/api/admin/system/menu/{id}`        | system:menu:get    |
| 菜单新增      | POST   | `/api/admin/system/menu/add`         | system:menu:add    |
| 菜单编辑      | PUT    | `/api/admin/system/menu/edit`        | system:menu:edit   |
| 菜单删除      | DELETE | `/api/admin/system/menu/delete/{id}` | system:menu:delete |

### 部门管理接口

| 接口   | 方法     | 路由                                   | 权限                 |
|------|--------|--------------------------------------|--------------------|
| 部门树  | GET    | `/api/admin/system/dept/tree`        | system:dept:list   |
| 部门详情 | GET    | `/api/admin/system/dept/{id}`        | system:dept:get    |
| 部门新增 | POST   | `/api/admin/system/dept/add`         | system:dept:add    |
| 部门编辑 | PUT    | `/api/admin/system/dept/edit`        | system:dept:edit   |
| 部门删除 | DELETE | `/api/admin/system/dept/delete/{id}` | system:dept:delete |

### 权限管理接口

| 接口       | 方法     | 路由                                         | 权限                       |
|----------|--------|--------------------------------------------|--------------------------|
| 权限列表（分页） | POST   | `/api/admin/system/permission/page`        | system:permission:list   |
| 权限列表（全部） | GET    | `/api/admin/system/permission/list`        | system:permission:list   |
| 权限详情     | GET    | `/api/admin/system/permission/{id}`        | system:permission:get    |
| 权限新增     | POST   | `/api/admin/system/permission/add`         | system:permission:add    |
| 权限编辑     | PUT    | `/api/admin/system/permission/edit`        | system:permission:edit   |
| 权限删除     | DELETE | `/api/admin/system/permission/delete/{id}` | system:permission:delete |

### 在线用户接口

| 接口     | 方法     | �权                                    | 权限                  |
|--------|--------|---------------------------------------|---------------------|
| 在线用户列表 | GET    | `/api/admin/monitor/online/list`      | monitor:online:list |
| 踐出用户   | DELETE | `/api/admin/monitor/online/kick/{id}` | monitor:online:kick |

## Redis数据结构

### Token存储

```
Key: auth:token:{tokenType}:{tokenSignature}
TTL: 根据tokenType（H5:30天, Admin:2小时）
Value: {"userId":123,"username":"admin","roles":["ADMIN"]}
```

### 用户信息缓存

```
Key: auth:user:{userId}
TTL: 1小时
Value: 用户基本信息
```

### 权限缓存

```
Key: auth:permission:{userId}
TTL: 1小时
Value: ["system:user:list","system:user:add",...]
```

### 验证码

```
Key: auth:code:{phone}
TTL: 5分钟
Value: {"code":"1234","useCount":0}
```

### 登录失败计数

```
Key: auth:fail:count:{username}
TTL: 15分钟
Value: 3
```

### Token黑名单

```
Key: auth:blacklist:{tokenSignature}
TTL: Token过期时间
Value: true
```

### 在线用户

```
Key: auth:online:user:{sessionId}
TTL: 会话过期时间
Value: 在线用户信息
```

## 安全设计

### Token安全

- JWT签名验证（HS256）
- 双密钥机制（H5/Admin分离）
- Token黑名单机制
- Token过期自动失效
- 刷新Token独立管理

### 密码安全

- BCrypt加密存储（工作因子10）
- 密码强度校验（6-20位，必须包含字母和数字）
- 密码重置功能

### 登录安全

- 登录失败计数（最多5次）
- 账户自动锁定（15分钟）
- 登录失败间隔冷却
- 登录IP和User-Agent记录

### 权限安全

- RBAC模型（用户-角色-权限）
- 权限注解校验（编译期检查）
- 超级管理员特权（跳过校验）
- 资源类型验证（API/Menu/Button）
- HTTP方法绑定验证

### 数据权限安全

- 部门级数据隔离
- 数据权限AOP拦截（SQL注入防护）
- 用户自定义权限覆盖角色权限

## 性能优化

### 缓存策略

- Redis分布式缓存
- 权限信息缓存（1小时TTL）
- 用户信息缓存（1小时TTL）
- 菜单树全局缓存（按权限过滤）
- Token自动过期清理

### 查询优化

- 数据库索引（用户名、手机号、部门、状态等）
- 分页查询优化
- 关联查询优化
- 树形结构优化（dept_path快速查询子部门）

### 性能预估

| 操作      | 预计耗时                      |
|---------|---------------------------|
| 权限校验    | < 1ms（缓存命中）               |
| 权限查询    | ~ 10-20ms（DB查询 + Redis缓存） |
| 登录验证    | < 5ms                     |
| Token生成 | < 3ms                     |

## 配置说明

### Nacos配置项

```yaml
# Redis配置
spring:
  data:
    redis:
      host: ${redis.host:localhost}
      port: ${redis.port:6379}
      password: ${redis.password:}
      database: 1

# MySQL配置
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://${mysql.host:localhost}:${mysql.port:3306}/${mysql.database:platform}
    username: ${mysql.username:root}
    password: ${mysql.password:}
    type: com.alibaba.druid.pool.DruidDataSource

# JWT配置
jwt:
  h5:
    secret: ${JWT_SECRET_H5}
    token-expire: 2592000
    refresh-expire: 7776000
  admin:
    secret: ${JWT_SECRET_ADMIN}
    token-expire: 7200
    refresh-expire: 604800
```

### application.yml配置

```yaml
server:
  port: 7200

spring:
  application:
    name: platform-auth-service

mybatis-plus:
  mapper-locations: classpath:mapper/**/*.xml
  type-aliases-package: com.zzl.platform.auth.entity
  configuration:
    map-underscore-to-camel-case: true

# 日志配置
logging:
  level:
    com.zzl.platform.auth: DEBUG
```

## 部署说明

### 环境要求

- JDK 17+
- MySQL 8.0+
- Redis 5.0+
- Nacos 2.0+

### 启动步骤

1. 执行数据库初始化脚本（schema.sql）
   ```sql
   mysql -u root -p password platform < schema.sql
   ```

2. 配置Nacos连接信息
3. 配置Redis连接信息
4. 配置MySQL连接信息
5. 修改JWT密钥（生产环境）
6. 启动服务：`java -jar platform-auth-service.jar`

### Docker部署（可选）

```docker
docker run -d \
  -p 7200:7200 \
  -e REDIS_HOST=localhost \
  -e MYSQL_HOST=localhost \
  -e MYSQL_USER=root \
  -e MYSQL_PASSWORD=123456 \
  platform-auth-service:latest
```

## 监控与运维

### 监控指标

- 请求响应时间（Prometheus）
- 登录失败率
- Token刷新频率
- 在线用户数量
- 权限缓存命中率

### 日志级别

- DEBUG：开发调试
- INFO：正常运行
- WARN：警告信息
- ERROR：错误信息

### 健康检查

- 数据库连接池状态
- Redis连接状态
- JWT密钥有效性检查
- 内存使用情况
- 线程创建

### 备份恢复

- 数据库定期备份
- Redis数据持久化
- 配置中心配置备份

## 注意事项

1. **生产环境**务必修改JWT密钥
2. 建议开启HTTPS加密传输
3. 定期清理过期的在线用户和Token
4. 建议配置Redis持久化（AOF/RDB）
5. 数据库密码使用环境变量或密钥管理服务
6. 开启操作审计日志（记录重要操作）
7. 系统内置角色和用户不建议删除
8. 修改密码后强制用户重新登录（清理会话）

## 扩展建议

### 后续优化方向

1. **数据权限增强**：支持更复杂的数据权限规则
2. **多租户支持**：支持多租户隔离
3. **SSO登录集成**：支持微信、支付宝等第三方登录
4. **消息通知**：集成邮件、短信、站内信
5. **权限可视化配置**：前端权限配置界面
6. **API文档**：自动生成Swagger/OpenAPI文档

### 技术债务

- [ ] 实现数据权限拦截器AOP
- [ ] 完善操作日志切面
- [ ] 实善验证码发送服务
- [ ] 添加单元测试和集成测试
- [ ] 性能压测和基准测试
- [ ] 完善API文档和使用示例

## 联系方式

如有问题，请联系：

- 技术支持：hotel.backend.zhouyaoping
- 项目地址：D:\ai\backend\platform-template
