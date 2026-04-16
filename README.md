# 本项目主要用于搭建多模块的项目模版，对于新项目的时候，可以直接使用本模版。

## 模块划分
- `platform-template` //根目录
- - common-core //基础封装
- - common-db   //数据库相关
- - common-web  //web相关（gateway排除）异常、响应结构封装
- - gateway //网关服务
  - auth-service //认证服务相关 通过数据库交互、业务处理、缓存校验 会与gateway结合起来形成接口安全认证
  - xxx-service 以上为基础必备模块，接下来的服务则根据业务需求新增即可

基本版本：
- jdk:21
- spring boot :3.x
- spring cloud:2023.x
- spring cloud aliba:2023.x

注意事项：
- nacos配置的读取方式：

```yaml
spring:
  config:
    import:
      # 一行一个配置文件
      # 从Nacos加载主配置文件（application.yml可被覆盖）
      - optional:nacos:${spring.application.name}.yaml?refreshEnabled=true
      # 从Nacos加载security.properties（包含Redis等敏感配置）
      - optional:nacos:security.properties?!refreshEnabled=true
```
