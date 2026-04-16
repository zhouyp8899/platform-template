# MyBatis-Plus 配置策略说明

## 问题背景

项目中存在 MyBatis-Plus 配置冲突：

- `platform-common-db/config/MybatisPlusConfig.java`：公共模块配置
- `platform-auth-service/config/MybatisPlusConfig.java`：服务特定配置

两个模块都定义了 `MybatisPlusInterceptor` Bean，可能造成配置覆盖。

## 解决方案

采用 **统一配置 + 可覆盖** 的策略：

### `platform-common-db` 统一配置

**特点**：

1. 使用 `@ConditionalOnMissingBean` 避免重复 Bean 定义
2. 通过 `@ConfigurationProperties` 支持服务层 YAML 配置覆盖
3. 统一管理分页插件和基础配置

**配置代码**：

```java
@Configuration
@ConditionalOnMissingBean(MybatisPlusInterceptor.class)  // ⭐ 关键：避免冲突
public class MybatisPlusConfig {
    
    @Bean
    @ConditionalOnMissingBean(MybatisConfiguration.class)
    public MybatisConfiguration mybatisConfiguration() {
        // 基础配置
    }
    
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor(MybatisPlusProperties properties) {
        // 分页插件配置，支持 YAML 覆盖
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        PaginationInnerInterceptor paginationInnerInterceptor = new PaginationInnerInterceptor(DbType.MYSQL);
        
        paginationInnerInterceptor.setMaxLimit(properties.getMaxLimit() > 0 ? 
            properties.getMaxLimit() : 1000L);
        paginationInnerInterceptor.setOverflow(properties.isOverflow());
        
        interceptor.addInnerInterceptor(paginationInnerInterceptor);
        return interceptor;
    }
    
    @Bean
    @ConfigurationProperties(prefix = "platform.mybatis-plus")
    public MybatisPlusProperties mybatisPlusProperties() {
        return new MybatisPlusProperties();
    }
}
```

### 服务层配置覆盖

**application.yml** 配置示例：

```yaml
# platform-auth-service/application.yml
platform:
  mybatis-plus:
    max-limit: 500          # 覆盖默认值 1000
    overflow: true          # 覆盖默认值 false
```

## 方案对比

| 特性      | 方案A：使用 common-db | 方案B：服务层独立配置 | ✅ 推荐方案           |
|---------|------------------|-------------|------------------|
| 配置统一性   | ✅ 统一管理           | ❌ 分散各服务     | ✅ 统一配置，支持覆盖      |
| 维护成本    | ✅ 低，一处修改         | ❌ 高，多处修改    | ✅ 低              |
| 扩展性     | ⚠️ 需要修改代码        | `✅ 直接修改     | ✅ 通过 YAML 配置扩展   |
| Bean 冲突 | ✅ 使用条件注解避免       | ⚠️ 需要明确优先级  | ✅ 条件注解 + YAML 配置 |
| 个性化能力   | �️️ 需要修改代码       | ✅ 灵活        | ✅ YAML 配置        |

## 配置说明

### 公共模块配置属性

| 属性                         | 默认值   | 说明            | 是否可覆盖  |
|----------------------------|-------|---------------|--------|
| `maxLimit`                 | 1000  | 单页最大限制数量      | ✅      |
| `overflow`                 | false | 溢出总页数后是否进行处理  | ✅      |
| `safeSql`                  | true  | 是否启用安全 SQL 检查 | ✅      |
| `mapUnderscoreToCamelCase` | true  | 驼峰下划线映射       | ❌ 固定配置 |

### 服务层配置示例

#### 场景1：大分页查询服务

```yaml
# platform-order-service/application.yml
platform:
  mybatis-plus:
    max-limit: 10000  # 订单服务允许更大的分页
```

#### 场景2：需要溢出处理

```yaml
# platform-report-service/application.yml
platform:
  mybatis-plus:
    overflow: true  # 报表服务允许分页溢出
```

## 优势

### 1. 避免 Bean 冲突

使用 `@ConditionalOnMissingBean` 注解：

- 如果子模块定义了同名 Bean，使用子模块的
- 如果子模块未定义，使用公共模块的
- 配置优先级明确

### 2. 灵活的配置覆盖

通过 YAML 配置覆盖：

- 无需修改代码
- 支持不同环境配置（dev/test/prod）
- 便于运维调整

### 3. 统一维护

公共配置统一在 `platform-common-db`：

- 升级 MyBatis-Plus 时只需修改一处
- 所有服务自动继承新配置
- 降低维护成本

## 最佳实践

### 1. 新增数据库服务时

```java
// platform-xxx-service/src/main/java/com/zzl/platform/xxx/XxxApplication.java
@SpringBootApplication
@MapperScan("com.zzl.platform.xxx.mapper")  // ⭐ 保持 MapperScan
public class XxxApplication {
    public static void main(String[] args) {
        SpringApplication.run(XxxApplication.class, args);
    }
}
```

### 2. 需要特殊配置时

```yaml
# platform-xxx-service/application.yml
platform:
  mybatis-plus:
    max-limit: 2000  # 根据服务需求调整
```

### 3. 多数据源场景

```java
// 如果服务需要多数据源，可以在服务层定义自己的配置
@Configuration
public class DynamicDataSourceConfig {
    // 主数据源配置（继承 common-db）
    
    @Bean
    @ConfigurationProperties(prefix = "platform.mybatis-plus")
    public MybatisPlusProperties properties() {
        return new MybatisPlusProperties();
    }
    
    // 从数据源配置...
}
```

## 注意事项

### ⚠️ 不要在服务层重复定义 Bean

错误做法：

```java
// platform-xxx-service/config/MyBatisPlusConfig.java
@Configuration
public class MyBatisPlusConfig {
    @Bean  // ❌ 与 common-db 冲突
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        // ...
    }
}
```

正确做法：

```yaml
# platform-xxx-service/application.yml
platform:
  mybatis-plus:
    max-limit: 2000  # ✅ 通过 YAML 配置
```

### ⚠️ MapperScan 保持在启动类

错误做法：

```java
// common-db/config/MybatisPlusConfig.java
@Configuration
@MapperScan("com.zzl.platform.xxx.mapper")  // ❌ 不确定扫描路径
```

正确做法：

```java
// platform-xxx-service/XxxApplication.java
@SpringBootApplication
@MapperScan("com.zzl.platform.xxx.mapper")  // ✅ 明确指定扫描路径
public class XxxApplication { }
```

## 总结

| 项目                    | 使用方案                                |
|-----------------------|-------------------------------------|
| platform-common-db    | ✅ 统一配置（`@ConditionalOnMissingBean`） |
| platform-auth-service | ✅ YAML 配置覆盖                         |
| 其他数据库服务               | ✅ 直接使用，需要时 YAML �配置                 |

**核心原则**：

1. 公共配置集中在 `platform-common-db`
2. 服务层通过 YAML 配置覆盖参数
3. `MapperScan` 保持在各服务的启动类
4. 使用条件注解避免 Bean 冲突
