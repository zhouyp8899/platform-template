# Nacos Config 配置读取排查指南

## 问题描述

- Nacos Discovery 正常工作（服务已注册）
- Nacos Config 无法读取配置（redis配置使用默认值）

## 配置文件说明

### 当前配置文件

1. **bootstrap.yml** - Nacos连接配置
2. **application.yml** - 应用配置 + Redis配置占位符
3. **application-nacos.yml** - Nacos扩展配置（读取security.properties）

## 解决方案

### 方案1：检查Nacos配置中心（推荐）

#### 1.1 确认Nacos Config服务正常

访问Nacos控制台，确认配置管理功能正常：

```
访问：http://127.0.0.1:8848/nacos
登录后，进入"配置管理" -> "配置列表"
```

#### 1.2 检查security.properties是否存在

确认在正确的Group和Namespace下存在以下配置：

```
Data ID: security.properties
Group: DEFAULT_GROUP（或你的配置的group）
Namespace: public（或你的配置的namespace）

配置内容：
redis.host=127.0.0.1
redis.port=6379
redis.password=your_redis_password
```

#### 1.3 检查文件格式

- 文件类型必须是：properties
- 文件编码必须是：UTF-8
- 不要包含BOM头
- 等号两边不要有空格

**正确示例：**

```properties
redis.host=127.0.0.1
redis.port=6379
redis.password=your_redis_password
```

**错误示例：**

```properties
redis.host = 127.0.0.1    # 等号两边有空格
redis.port=6379
redis.password = your_redis_password    # 等号两边有空格
```

### 方案2：使用环境变量（快速测试）

如果Nacos配置暂时无法使用，可以临时设置环境变量测试：

```bash
export redis.host=127.0.0.1
export redis.port=6379
export redis.password=your_redis_password

./gradlew :platform-gateway:bootRun
```

或通过命令行传递：

```bash
./gradlew :platform-gateway:bootRun \
  -Dredis.host=127.0.0.1 \
  -Dredis.port=6379 \
  -Dredis.password=your_redis_password
```

### 方案3：调试Nacos Config连接

#### 3.1 测试Nacos Config API

```bash
# 测试获取配置列表
curl -X GET "http://127.0.0.1:8848/nacos/v1/cs/configs?dataId=security.properties&group=DEFAULT_GROUP" \
  -H "Authorization: Bearer $(echo -n 'nacos:nacos' | base64)"

# 测试获取配置详情
curl -X GET "http://127.0.0.1:8848/nacos/v1/cs/configs/DEFAULT_GROUP/DEFAULT_GROUP/security.properties" \
  -H "Authorization: Bearer $(echo -n 'nacos:nacos' | base64)"
```

#### 3.2 检查Nacos配置中心状态

访问Nacos控制台的"集群管理" -> "节点列表"，确认所有节点正常。

#### 3.3 检查Nacos配置数据库

- 如果使用MySQL，检查配置是否正常持久化
- 如果使用内嵌Derby，检查磁盘空间是否充足

### 方案4：检查Spring Boot日志

启动gateway时，查找以下关键日志：

#### 成功加载的日志特征：

```
【成功】Redis配置已从Nacos加载！
Nacos配置源: nacos-xxx-DEFAULT_GROUP-xxx
redis.host: 127.0.0.1
redis.port: 6379
```

#### 失败加载的日志特征：

```
【严重问题】Redis配置未从Nacos加载！
redis.host: localhost
redis.port: 6379
没有Nacos配置源（或只有application.properties源）
```

#### 查找Nacos连接错误：

```
Failed to connect to Nacos server
Nacos config service unavailable
Config server not reachable
```

### 方案5：检查防火墙和网络

```bash
# 测试Nacos端口连通性
telnet 127.0.0.1 8848

# 或使用nc命令
nc -zv 127.0.0.1 8848

# 测试HTTP访问
curl -v http://127.0.0.1:8848/nacos/v1/console/server/state
```

### 方案6：使用Nacos CLI上传配置

如果通过Nacos控制台上传配置失败，可以尝试使用Nacos CLI：

```bash
# 假设已安装nacos-cli
nacos-cli -s 127.0.0.1:8848 -u nacos -p nacos \
  import -t properties -d security.properties \
  -g DEFAULT_GROUP -n DEFAULT_GROUP \
  -c "redis.host=127.0.0.1\nredis.port=6379\nredis.password=your_redis_password"
```

## 常见错误和解决方案

### 错误1：Nacos Config服务未启动

**症状：** 日志中提示"Config server not reachable"
**解决：** 检查Nacos启动参数，确保包含配置中心模块

### 错误2：Group或Namespace不匹配

**症状：** 日志中提示配置存在但读取失败
**解决：** 确认环境变量NACOS_GROUP、NACOS_NAMESPACE与Nacos配置一致

### 错误3：配置文件权限问题

**症状：** 日志中提示"Permission denied"
**解决：** 检查Nacos数据库权限，确保Nacos用户有配置读写权限

### 错误4：配置中心数据库连接失败

**症状：** Nacos控制台无法保存配置
**解决：** 检查Nacos的数据库配置（MySQL或Derby）

## 快速验证脚本

创建一个快速验证脚本：

```bash
#!/bin/bash
echo "=== Nacos Config 验证脚本 ==="
echo ""
echo "1. 检查环境变量"
echo "NACOS_ADDR: ${NACOS_ADDR}"
echo "NACOS_GROUP: ${NACOS_GROUP}"
echo "NACOS_NAMESPACE: ${NACOS_NAMESPACE}"
echo ""
echo "2. 测试Nacos Config API"
curl -X GET "http://${NACOS_ADDR:-127.0.0.1:8848}/nacos/v1/cs/configs?dataId=security.properties&group=${NACOS_GROUP:-DEFAULT_GROUP}" \
  -s -o /dev/null -w "%{http_code}\n"
echo ""
echo "3. 检查gateway配置文件"
ls -la platform-gateway/resource/*.yml
echo ""
echo "=== 验证完成 ==="
```

## 总结

1. **确认security.properties在Nacos中存在且格式正确**
2. **确认Group和Namespace配置匹配**
3. **检查Nacos Config服务正常工作**
4. **查看启动日志，定位具体错误**
5. **必要时使用环境变量临时绕过**

如果以上步骤都正常但仍无法读取，请提供完整的启动日志以便进一步分析。
