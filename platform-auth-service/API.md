# Platform Auth Service API 接口文档

## 基础信息

- 服务地址：`http://localhost:7200`
- API前缀：
    - 管理端：`/api/admin`
    - H5端：`/api/h5`
- 认证方式：JWT Bearer Token
- 请求头：
    - `Authorization: Bearer {token}`
    - `X-User-Id: {userId}`

---

## 1. 认证相关接口

### 1.1 管理员登录

- **接口地址**：`POST /api/admin/auth/login`
- **请求参数**：
  ```json
  {
    "username": "admin",
    "password": "123456"
  }
  ```
- **响应数据**：
  ```json
  {
    "code": 200,
    "message": "成功",
    "data": {
      "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
      "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
      "expireIn": 7200,
      "userInfo": {
        "userId": 1,
        "username": "admin",
        "realName": "系统管理员",
        "phone": "13800138000",
        "email": "admin@example.com",
        "roles": ["super_admin"],
        "permissions": ["*:*:*"]
      }
    }
  }
  ```

### 1.2 刷新Token

- **接口地址**：`POST /api/admin/auth/refresh`
- **请求参数**：
  ```json
  {
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "admin"
  }
  ```

### 1.3 登出

- **接口地址**：`POST /api/admin/auth/logout`
- **请求头**：
    - `X-User-Id: {userId}`
    - `Authorization: Bearer {token}`

### 1.4 获取当前用户信息

- **接口地址**：`GET /api/admin/auth/current`
- **请求头**：
    - `X-User-Id: {userId}`

### 1.5 修改当前用户密码

- **接口地址**：`PUT /api/admin/auth/current/change-password`
- **请求头**：
    - `X-User-Id: {userId}`
- **请求参数**：
  ```json
  {
    "oldPassword": "123456",
    "newPassword": "654321",
    "confirmPassword": "654321"
  }
  ```

---

## 2. H5 认证接口

### 2.1 发送验证码

- **接口地址**：`POST /api/h5/auth/send-code`
- **请求参数**：
  ```json
  {
    "phone": "13800138000"
  }
  ```

### 2.2 手机号登录

- **接口地址**：`POST /api/h5/auth/login/login-phone`
- **请求参数**：
  ```json
  {
    "phone": "13800138000",
    "code": "123456"
  }
  ```

### 2.3 刷新Token

- **接口地址**：`POST /api/h5/auth/refresh`
- **请求参数**：
  ```json
  {
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "h5"
  }
  ```

### 2.4 登出

- **接口接口**：`POST /api/h5/auth/logout`
- **请求头**：
    - `X-User-Id: {userId}`
    - `Authorization: Bearer {token}`

---

## 3. 用户管理接口

### 3.1 分页查询用户

- **接口地址**：`POST /api/admin/system/user/page`
- **权限**：`system:user:list`
- **请求参数**：
  ```json
  {
    "pageNum": 1,
    "pageSize": 10,
    "param": {
      "username": "admin",
      "realName": "系统管理员",
      "phone": "13800138000",
      "deptId": 1,
      "status": 1,
      "userType": "ADMIN"
    }
  }
  ```
- **响应数据**：
  ```json
  {
    "code": 200,
    "message": "成功",
    "data": {
      "total": 100,
      "list": [
        {
          "id": 1,
          "username": "admin",
          "realName": "系统管理员",
          "nickName": "管理员",
          "phone": "13800138000",
          "email": "admin@example.com",
          "avatar": "https://example.com/avatar.jpg",
          "gender": 1,
          "deptId": 1,
          "userType": "ADMIN",
          "dataScope": "ALL",
          "status": 1,
          "roleCodes": ["super_admin"],
          "createTime": "2024-01-01 00:00:00"
        }
      ],
      "pageNum": 1,
      "pageSize": 10
    }
  }
  ```

### 3.2 查询用户详情

- **接口地址**：`GET /api/admin/system/user/{id}`
- **权限**：`system:user:get`

### 3.3 新增用户

- **接口地址**：`POST /api/admin/system/user/add`
- **权限**：`system:user:add`
- **请求头**：
    - `X-User-Id: {userId}`
- **请求参数**：
  ```json
  {
    "username": "test_user",
    "password": "123456",
    "realName": "测试用户",
    "nickName": "测试",
    "phone": "13800138001",
    "email": "test@example.com",
    "gender": 1,
    "deptId": 1,
    "roleIds": [1, 2],
    "remark": "测试用户"
  }
  ```

### 3.4 编辑用户

- **接口地址**：`PUT /api/admin/system/user/edit`
- **权限**：`system:user:edit`
- **请求头**：
    - `X-User-Id: {userId}`
- **请求参数**：
  ```json
  {
    "id": 1,
    "username": "test_user",
    "realName": "测试用户",
    "nickName": "测试",
    "phone": "13800138001",
    "email": "test@example.com",
    "gender": 1,
    "deptId": 1,
    "roleIds": [1, 2],
    "remark": "测试用户"
  }
  ```

### 3.5 删除用户

- **接口地址**`DELETE /api/admin/system/user/delete/{id}`
- **权限**：`system:user:delete`
- **请求头**：
    - `X-User-Id: {userId}`

### 3.6 批量删除用户

- **接口地址**：`DELETE /api/admin/system/user/batch-delete`
- **权限**：`system:user:delete`
- **请求头**：
    - `X-User-Id: {userId}`
- **请求参数**：
  ```json
  [1, 2, 3]
  ```

### 3.7 重置用户密码

- **接口地址**：`PUT /api/admin/system/user/reset-password`
- **权限**：`system:user:reset`
- **请求头**：
    - `X-User-Id: {userId}`
- **请求参数**：
  ```json
  1
  ```

### 3.8 修改用户状态

- **接口地址**：`PUT /api/admin/system/user/change-status`
- **权限**：`system:user:edit`
- **请求头**：
    - `X-User-Id: {userId}`
- **请求参数**：
  ```json
  {
    "userId": 1,
    "status": 1
  }
  ```

### 3.9 为用户分配角色

- **接口地址**：`POST /api/admin/system/user/grant-roles`
- **权限**：`system:user:grant`
- **请求头**：
    - `X-User-Id: {userId}`
- **请求参数**：
  ```json
  {
    "userId": 1,
    "roleIds": [1, 2, 3]
  }
  ```

### 3.10 获取用户的角色列表

- **接口地址**：`GET /api/admin/system/user/{id}/roles`
- **权限**：`system:user:get`

---

## 4. 角色管理接口

### 4.1 分页查询角色

- **接口地址**：`POST /api/admin/system/role/page`
- **权限**：`system:role:list`
- **请求参数**：
  ```json
  {
    "pageNum": 1,
    "pageSize": 10,
    "keyword": "管理员"
  }
  ```

### 4.2 查询所有角色

- **接口地址**：`GET /api/admin/system/role/list`
- **权限**：`system:role:list`

### 4.3 查询角色详情

- **接口地址**：`GET /api/admin/system/role/{id}`
- **权限**：`system:role:get`

### 4.4 新增角色

- **接口地址**：`POST /api/admin/system/role/add`
- **权限**：`system:role:add`
- **请求头**：
    - `X-User-Id: {userId}`
- **请求参数**：
  ```json
  {
    "roleCode": "test_role",
    "roleName": "测试角色",
    "roleType": "BUSINESS",
    "dataScope": "CUSTOM",
    "description": "测试角色描述",
    "status": 1,
    "sort": 0,
    "permissionIds": [1, 2, 3]
  }
  ```

### 4.5 编辑角色

- **接口地址**：`PUT /api/admin/system/role/edit`
- **权限**：`system:role:edit`
- **请求头**：
    - `X-User-Id: {userId}`
- **请求参数**：
  ```json
  {
    "id": 1,
    "roleCode": "test_role",
    "roleName": "测试角色",
    "roleType": "BUSINESS",
    "dataScope": "CUSTOM",
    "description": "测试角色描述",
    "status": 1,
    "sort": 0,
    "permissionIds": [1, 2, 3]
  }
  ```

### 4.6 删除角色

- **接口地址**：`DELETE /api/admin/system/role/delete/{id}`
- **权限**：`system:role:delete`
- **请求头**：
    - `X-User-Id: {userId}`

### 4.7 为角色分配权限

- **接口地址**：`POST /api/admin/system/role/grant-permissions`
- **权限**：`system:role:grant`
- **请求头**：
    - `X-User-Id: {userId}`
- **请求参数**：
  ```json
  {
    "roleId": 1,
    "permissionIds": [1, 2, 3]
  }
  ```

### 4.8 获取角色的权限列表

- **接口地址**：`GET /api/admin/system/role/{id}/permissions`
- **权限**：`system:role:get`

---

## 5. 权限管理接口

### 5.1 分页查询权限

- **接口地址**：`POST /api/admin/system/permission/page`
- **权限**：`system:permission:list`
- **请求参数**：
  ```json
  {
    "pageNum": 1,
    "pageSize": 10,
    "keyword": "用户"
  }
  ```

### 5.2 查询所有权限

- **接口地址**：`GET /api/admin/system/permission/list`
- **权限**：`system:permission:list`

### 5.3 查询权限详情

- **接口地址**：`GET /api/admin/system/permission/{id}`
- **权限**：`system:permission:get`

### 5.4 新增权限

- **接口地址**：`POST /api/admin/system/permission/add`
- **权限**：`system:permission:add`
- **请求头**：
    - `X-User-Id: {userId}`
- **请求参数**：
  ```json
  {
    "permissionCode": "system:user:add",
    "permissionName": "用户新增",
    "resourceType": "API",
    "resourcePath": "/api/admin/system/user/add",
    "httpMethod": "POST",
    "menuId": 1,
    "permissionGroup": "用户管理",
    "description": "新增用户",
    "status": 1,
    "sort": 0
  }
  ```

### 5.5 编辑权限

- **接口地址**：`PUT /api/admin/system/permission/edit`
- **权限**：`system:permission:edit`
- **请求头**：
    - `X-User-Id: {userId}`
- **请求参数**：
  ```json
  {
    "id": 1,
    "permissionCode": "system:user:add",
    "permissionName": "用户新增",
    "resourceType": "API",
    "resourcePath": "/api/admin/system/user/add",
    "httpMethod": "POST",
    "menuId": 1,
    "permissionGroup": "用户管理",
    "description": "新增用户",
    "status": 1,
    "sort": 0
  }
  ```

### 5.6 删除权限

- **接口地址**：`DELETE /`api/admin/system/permission/delete/{id}`
- **权限**：`system:permission:delete`
- **请求头**：
    - `X-User-Id: {userId}`

---

## 6. 菜单管理

### 6.1 查询所有菜单树

- **接口地址**：`GET /api/admin/system/menu/tree/all`
- **权限**：`system:menu:list`

### 6.2 查询当前用户可见菜单树

- **接口地址**：`GET /api/admin/system/menu/tree`
- **权限**：`system:menu:list`
- **请求头**：
    - `X-User-Id: {userId}`

### 6.3 查询菜单详情

- **接口地址**：`GET /api/admin/system/menu/{id}`
- **权限**：`system:menu:get`

### 6.4 新增菜单

- **接口地址**：`POST /api/admin/system/menu/add`
- **权限**：`system:menu:add`
- **请求头**：
    - `X-User-Id: {userId}`
- **请求参数**：
  ```json
  {
    "parentId": 0,
    "menuName": "用户管理",
    "menuType": "DIRECTORY",
    "menuCode": "user",
    "icon": "user",
    "path": "/system/user",
    "component": "system/user/index",
    "isFrame": 0,
    "isCache": 1,
    "isVisible": 1,
    "status": 1,
    "sort": 1,
    "permissionCode": "system:user:list",
    "remark": "用户管理菜单"
  }
  ```

### 6.5 编辑菜单

- **接口地址**：`PUT /api/admin/system/menu/edit`
- **权限**：`system:menu:edit`
- **请求头**：
    - `X-User-Id: {userId}`
- **请求参数**：
  ```json
  {
    "id": 1,
    "parentId": 0,
    "menuName": "用户管理",
    "menuType": "DIRECTORY",
    "menuCode": "user",
    "icon": "user",
    "path": "/system/user",
    "component": "system/user/index",
    "isFrame": 0,
    "isCache": 1,
    "isVisible": 1,
    "status": 1,
    "sort": 1,
    "permissionCode": "system:user:list",
    "remark": "用户管理菜单"
  }
  ```

### 6.6 删除菜单

- **接口地址**：`DELETE /api/admin/system/menu/delete/{id}`
- **权限**：`system:menu:delete`
- **请求头**：
    - `X-User-Id: {userId}`

---

## 7. 部门管理

### 7.1 查询部门树

- **接口地址**：`GET /api/admin/system/dept/tree`
- **权限**：`system:dept:list`

### 7.2 查询部门详情

- **接口地址**：`GET /api/admin/system/dept/{id}`
- **权限**：`system:dept:get`

### 7.3 新增部门

- **接口地址**：`POST /api/admin/system/dept/add`
- **权限**：`system:dept:add`
- **请求头**：
    - `X-User-Id: {userId}`
- **请求参数**：
  ```json
  {
    "parentId": 0,
    "deptName": "技术部",
    "deptCode": "TECH",
    "leader": "张三",
    "phone": "13800138000",
    "email": "tech@example.com",
    "sort": 1,
    "status": 1,
    "remark": "技术部门"
  }
  ```

### 7.4 编辑部门

- **接口地址**：`PUT /api/admin/system/dept/edit`
- **权限**：`system:dept:edit`
- **请求头**：
    - `X-User-Id: {userId}`
- **请求参数**：
  ```json
  {
    "id": 1,
    "parentId": 0,
    "deptName": "技术部",
    "deptCode": "TECH",
    "leader": "张三",
    "phone": "13800138000",
    "email": "tech@example.com",
    "sort": 1,
    "status": 1,
    "remark": "技术部门"
  }
  ```

### 7.5 删除部门

- **接口地址**：`DELETE /api/admin/system/dept/delete/{id}`
- **权限**：`system:dept:delete`
- **请求头**：
    - `X-User-Id: {userId}`

---

## 8. 在线用户监控

### 8.1 查询在线用户列表

- **接口地址**：`GET /api/admin/monitor/online/list`
- **权限**：`monitor:online:list`

### 8.2 踢出用户

- **接口地址**：`DELETE /api/admin/monitor/online/kick/{id}`
- **权限**：`monitor:online:kick`

---

## 通用响应格式

所有接口统一返回以下格式：

```json
{
  "code": 200,
  "message": "成功",
  "data": {}
}
```

**状态码说明**：

- `200`：成功
- `400`：请求参数错误
- `401`：未认证
- `403`：无权限
- `500`：服务器错误

---

## 枚举值说明

### 用户类型（UserType）

- `ADMIN`：管理员
- `H5`：H5用户

### 用户状态（UserStatus）

- `NORMAL`：正常
- `LOCKED`：锁定
- `DISABLED`：禁用

### 角色类型（RoleType）

- `SYSTEM`：系统角色
- `BUSINESS`：业务角色

### 数据范围（DataScopeType）

- `ALL`：全部数据
- `CUSTOM`：自定义数据
- `DEPT`：本部门数据
- `DEPT_AND_CHILD`：本部门及子部门数据
- `SELF`：仅本人数据

### 菜单类型（MenuType）

- `DIRECTORY`：目录
- `MENU`：菜单
- `BUTTON`：按钮

### 资源类型（ResourceType）

- `API`：接口
- `MENU`：菜单

---

## 注意事项

1. **Token有效期**：
    - 管理端Token：2小时
    - H5端Token：24小时
    - 刷新Token：30天

2. **密码重置**：
    - 重置后的默认密码：`123456`
    - 建议用户首次登录后修改密码

3. **验证码**：
    - 验证码有效期：5分钟
    - 同一个验证码最多使用3次

4. **登录失败锁定**：
    - 连续失败5次后锁定账户
    - 锁定时间：30分钟

5. **权限控制**：
    - 超级管理员（super_admin）拥有所有权限
    - 其他用户需要根据分配的角色和权限访问相应接口

---

## 测试账号

### 管理员账号

- 用户名：`admin`
- 密码：`123456`
- 角色：超级管理员
- 权限：所有权限

### H5测试账号

- 手机号：`13800138000`
- 验证码：测试环境见日志
- 角色：H5用户

---

## 更新日志

- **2026-04-16**：初始版本发布
    - 完成所有基础功能接口
    - 支持JWT认证
    - 完成RBAC权限控制
