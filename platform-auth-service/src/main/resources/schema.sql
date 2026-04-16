-- ============================================
-- 平台认证与权限管理系统数据库初始化脚本
-- 版本：v1.0.0
-- 执行前请确保MySQL版本 >= 8.0
-- ============================================

-- 创建数据库（如果不存在）
CREATE
DATABASE IF NOT EXISTS `platform` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE
`platform`;

-- ============================================
-- 1. 部门表
-- ============================================
DROP TABLE IF EXISTS `t_sys_department`;
CREATE TABLE `t_sys_department`
(
    `id`           BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '部门ID',
    `dept_code`    VARCHAR(50)  NOT NULL COMMENT '部门编码',
    `dept_name`    VARCHAR(100) NOT NULL COMMENT '部门名称',
    `parent_id`    BIGINT(20) unsigned DEFAULT 0 COMMENT '父级部门ID',
    `dept_level`   INT(10) NOT NULL DEFAULT 1 COMMENT '部门层级',
    `dept_path`    VARCHAR(500)          DEFAULT NULL COMMENT '部门路径',
    `dept_sort`    INT(10) NOT NULL DEFAULT 0 COMMENT '显示顺序',
    `leader`       VARCHAR(50)           DEFAULT NULL COMMENT '负责人',
    `leader_phone` VARCHAR(20)           DEFAULT NULL COMMENT '负责人电话',
    `email`        VARCHAR(100)          DEFAULT NULL COMMENT '部门邮箱',
    `status`       TINYINT(2) NOT NULL DEFAULT 1 COMMENT '状态',
    `remark`       VARCHAR(500)          DEFAULT NULL COMMENT '备注',
    `create_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by`    BIGINT(20) unsigned DEFAULT NULL COMMENT '创建人',
    `update_by`    BIGINT(20) unsigned DEFAULT NULL COMMENT '更新人',
    `deleted`      TINYINT(1) NOT NULL DEFAULT 0 COMMENT '0-否, 1-是',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_dept_code` (`dept_code`, `deleted`),
    KEY            `idx_parent_id` (`parent_id`, `deleted`),
    KEY            `idx_dept_path` (`dept_path`(200)),
    KEY            `idx_dept_level` (`dept_level`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='部门表';

-- ============================================
-- 2. 用户表
-- ============================================
DROP TABLE IF EXISTS `t_sys_user`;
CREATE TABLE `t_sys_user`
(
    `id`               BIGINT(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username`         VARCHAR(50)  NOT NULL COMMENT '用户名',
    `password`         VARCHAR(128) NOT NULL COMMENT '密码',
    `real_name`        VARCHAR(50)           DEFAULT NULL COMMENT '真实姓名',
    `nick_name`        VARCHAR(50)           DEFAULT NULL COMMENT '昵称',
    `phone`            VARCHAR(20)           DEFAULT NULL COMMENT '手机号',
    `email`            VARCHAR(100)          DEFAULT NULL COMMENT '邮箱',
    `avatar`           VARCHAR(500)          DEFAULT NULL COMMENT '头像URL',
    `gender`           VARCHAR(10)           DEFAULT NULL COMMENT '性别',
    `dept_id`          BIGINT(20) unsigned DEFAULT NULL COMMENT '所属部门ID',
    `user_type`        VARCHAR(20)  NOT NULL DEFAULT 'ADMIN' COMMENT '用户类型',
    `data_scope`       VARCHAR(20)           DEFAULT NULL COMMENT '数据权限范围',
    `status`           TINYINT(2) NOT NULL DEFAULT 1 COMMENT '状态',
    `last_login_time`  DATETIME              DEFAULT NULL COMMENT '最后登录时间',
    `last_login_ip`    VARCHAR(50)           DEFAULT NULL COMMENT '最后登录IP',
    `login_fail_count` INT(10) NOT NULL DEFAULT 0 COMMENT '登录失败次数',
    `lock_time`        DATETIME              DEFAULT NULL COMMENT '锁定时间',
    `remark`           VARCHAR(500)          DEFAULT NULL COMMENT '备注',
    `create_time`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by`        BIGINT(20) unsigned DEFAULT NULL COMMENT '创建人',
    `update_by`        BIGINT(20) unsigned DEFAULT NULL COMMENT '更新人',
    `deleted`          TINYINT(1) NOT NULL DEFAULT 0 COMMENT '0-否, 1-是',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`, `deleted`),
    UNIQUE KEY `uk_phone` (`phone`, `deleted`),
    KEY                `idx_dept_id` (`dept_id`, `deleted`),
    KEY                `idx_user_type` (`user_type`, `deleted`),
    KEY                `idx_status` (`status`, `deleted`),
    KEY                `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- ============================================
-- 3. 角色表
-- ============================================
DROP TABLE IF EXISTS `t_sys_role`;
CREATE TABLE `t_sys_role`
(
    `id`          BIGINT(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '角色ID',
    `role_code`   VARCHAR(50) NOT NULL COMMENT '角色编码',
    `role_name`   VARCHAR(50) NOT NULL COMMENT '角色名称',
    `role_type`   VARCHAR(20) NOT NULL DEFAULT 'BUSINESS' COMMENT '角色类型',
    `data_scope`  VARCHAR(20)          DEFAULT 'ALL' COMMENT '数据范围',
    `description` VARCHAR(500)         DEFAULT NULL COMMENT '角色描述',
    `is_system`   TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否系统内置角色',
    `status`      TINYINT(2) NOT NULL DEFAULT 1 COMMENT '状态',
    `sort`        INT(10) DEFAULT 0 COMMENT '排序',
    `create_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by`   BIGINT(20) unsigned DEFAULT NULL COMMENT '创建人',
    `update_by`   BIGINT(20) unsigned DEFAULT NULL COMMENT '更新人',
    `deleted`     TINYINT(1) NOT NULL DEFAULT 0 COMMENT '0-否, 1-是',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_code` (`role_code`, `deleted`),
    KEY           `idx_role_type` (`role_type`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- ============================================
-- 4. 权限表
-- ============================================
DROP TABLE IF EXISTS `t_sys_permission`;
CREATE TABLE `t_sys_permission`
(
    `id`               BIGINT(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '权限ID',
    `permission_code`  VARCHAR(100) NOT NULL COMMENT '权限编码',
    `permission_name`  VARCHAR(100) NOT NULL COMMENT '权限名称',
    `resource_type`    VARCHAR(20)  NOT NULL COMMENT '资源类型',
    `resource_path`    VARCHAR(200)          DEFAULT NULL COMMENT '资源路径',
    `http_method`      VARCHAR(20)           DEFAULT NULL COMMENT 'HTTP方法',
    `menu_id`          BIGINT(20) unsigned DEFAULT NULL COMMENT '关联菜单ID',
    `permission_group` VARCHAR(50)           DEFAULT NULL COMMENT '权限分组',
    `description`      VARCHAR(200)          DEFAULT NULL COMMENT '权限描述',
    `status`           TINYINT(2) NOT NULL DEFAULT 1 COMMENT '状态',
    `sort`             INT(10) DEFAULT 0 COMMENT '排序',
    `create_time`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by`        BIGINT(20) unsigned DEFAULT NULL COMMENT '创建人',
    `update_by`        BIGINT(20) unsigned DEFAULT NULL COMMENT '更新人',
    `deleted`          TINYINT(1) NOT NULL DEFAULT 0 COMMENT '0-否, 1-是',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_permission_code` (`permission_code`, `deleted`),
    KEY                `idx_resource_path` (`resource_path`, `http_method`),
    KEY                `idx_menu_id` (`menu_id`),
    KEY                `idx_permission_group` (`permission_group`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限表';

-- ============================================
-- 5. 菜单表
-- ============================================
DROP TABLE IF EXISTS `t_sys_menu`;
CREATE TABLE `t_sys_menu`
(
    `id`          BIGINT(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '菜单ID',
    `menu_name`   VARCHAR(50) NOT NULL COMMENT '菜单名称',
    `parent_id`   BIGINT(20) unsigned DEFAULT 0 COMMENT '父级菜单ID',
    `menu_type`   VARCHAR(10) NOT NULL COMMENT '菜单类型',
    `menu_icon`   VARCHAR(100)         DEFAULT NULL COMMENT '菜单图标',
    `menu_path`   VARCHAR(200)         DEFAULT NULL COMMENT '路由地址',
    `component`   VARCHAR(200)         DEFAULT NULL COMMENT '组件路径',
    `redirect`    VARCHAR(200)         DEFAULT NULL COMMENT '重定向地址',
    `is_cache`    TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否缓存',
    `is_visible`  TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否显示',
    `is_external` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否外链',
    `menu_sort`   INT(10) NOT NULL DEFAULT 0 COMMENT '显示顺序',
    `status`      TINYINT(2) NOT NULL DEFAULT 1 COMMENT '状态',
    `remark`      VARCHAR(500)         DEFAULT NULL COMMENT '备注',
    `create_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by`   BIGINT(20) unsigned DEFAULT NULL COMMENT '创建人',
    `update_by`   BIGINT(20) unsigned DEFAULT NULL COMMENT '更新人',
    `deleted`     TINYINT(1) NOT NULL DEFAULT 0 COMMENT '0-否, 1-是',
    PRIMARY KEY (`id`),
    KEY           `idx_parent_id` (`parent_id`, `deleted`),
    KEY           `idx_menu_type` (`menu_type`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜单表';

-- ============================================
-- 6. 用户角色关联表
-- ============================================
DROP TABLE IF EXISTS `t_sys_user_role`;
CREATE TABLE `t_sys_user_role`
(
    `id`          BIGINT(20) unsigned NOT NULL AUTO_INCREMENT,
    `user_id`     BIGINT(20) unsigned NOT NULL COMMENT '用户ID',
    `role_id`     BIGINT(20) unsigned NOT NULL COMMENT '角色ID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `create_by`   BIGINT(20) unsigned DEFAULT NULL COMMENT '创建人',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_role` (`user_id`, `role_id`),
    KEY           `idx_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- ============================================
-- 7. 角色权限关联表
-- ============================================
DROP TABLE IF EXISTS `t_sys_role_permission`;
CREATE TABLE `t_sys_role_permission`
(
    `id`            BIGINT(20) unsigned NOT NULL AUTO_INCREMENT,
    `role_id`       BIGINT(20) unsigned NOT NULL COMMENT '角色ID',
    `permission_id` BIGINT(20) unsigned NOT NULL COMMENT '权限ID',
    `create_time`   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `create_by`     BIGINT(20) unsigned DEFAULT NULL COMMENT '创建人',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_permission` (`role_id`, `permission_id`),
    KEY             `idx_permission_id` (`permission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限关联表';

-- ============================================
-- 8. 菜单权限关联表
-- ============================================
DROP TABLE IF EXISTS `t_sys_menu_permission`;
CREATE TABLE `t_sys_menu_permission`
(
    `id`            BIGINT(20) unsigned NOT NULL AUTO_INCREMENT,
    `menu_id`       BIGINT(20) unsigned NOT NULL COMMENT '菜单ID',
    `permission_id` BIGINT(20) unsigned NOT NULL COMMENT '权限ID',
    `create_time`   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `create_by`     BIGINT(20) unsigned DEFAULT NULL COMMENT '创建人',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_menu_permission` (`menu_id`, `permission_id`),
    KEY             `idx_permission_id` (`permission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜单权限关联表';

-- ============================================
-- 9. 用户部门关联表
-- ============================================
DROP TABLE IF EXISTS `t_sys_user_department`;
CREATE TABLE `t_sys_user_department`
(
    `id`          BIGINT(20) unsigned NOT NULL AUTO_INCREMENT,
    `user_id`     BIGINT(20) unsigned NOT NULL COMMENT '用户ID',
    `dept_id`     BIGINT(20) unsigned NOT NULL COMMENT '部门ID',
    `is_leader`   TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否部门负责人',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `create_by`   BIGINT(20) unsigned DEFAULT NULL COMMENT '创建人',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_dept` (`user_id`, `dept_id`),
    KEY           `idx_dept_id` (`dept_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户部门关联表';

-- ============================================
-- 10. 角色数据权限部门表
-- ============================================
DROP TABLE IF EXISTS `t_sys_role_dept`;
CREATE TABLE `t_sys_role_dept`
(
    `id`          BIGINT(20) unsigned NOT NULL AUTO_INCREMENT,
    `role_id`     BIGINT(20) unsigned NOT NULL COMMENT '角色ID',
    `dept_id`     BIGINT(20) unsigned NOT NULL COMMENT '部门ID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `create_by`   BIGINT(20) unsigned DEFAULT NULL COMMENT '创建人',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_dept` (`role_id`, `dept_id`),
    KEY           `idx_dept_id` (`dept_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色数据权限部门表';

-- ============================================
-- 11. 操作日志表
-- ============================================
DROP TABLE IF EXISTS `t_sys_operate_log`;
CREATE TABLE `t_sys_operate_log`
(
    `id`             BIGINT(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '日志ID',
    `module`         VARCHAR(50)       DEFAULT NULL COMMENT '模块名称',
    `business_type`  VARCHAR(20)       DEFAULT NULL COMMENT '业务类型',
    `method`         VARCHAR(100)      DEFAULT NULL COMMENT '方法名称',
    `request_method` VARCHAR(10)       DEFAULT NULL COMMENT '请求方式',
    `operator_type`  VARCHAR(20)       DEFAULT NULL COMMENT '操作人类型',
    `operator_name`  VARCHAR(50)       DEFAULT NULL COMMENT '操作人名称',
    `dept_name`      VARCHAR(50)       DEFAULT NULL COMMENT '部门名称',
    `oper_url`       VARCHAR(200)      DEFAULT NULL COMMENT '请求URL',
    `oper_ip`        VARCHAR(50)       DEFAULT NULL COMMENT '主机地址',
    `oper_location`  VARCHAR(100)      DEFAULT NULL COMMENT '操作地点',
    `oper_param`     TEXT              DEFAULT NULL COMMENT '请求参数',
    `json_result`    TEXT              DEFAULT NULL COMMENT '返回参数',
    `status`         TINYINT(2) NOT NULL DEFAULT 1 COMMENT '操作状态',
    `error_msg`      VARCHAR(500)      DEFAULT NULL COMMENT '错误消息',
    `cost_time`      INT(10) DEFAULT 0 COMMENT '消耗时间',
    `oper_time`      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    PRIMARY KEY (`id`),
    KEY              `idx_oper_time` (`oper_time`),
    KEY              `idx_operator_name` (`operator_name`),
    KEY              `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

-- ============================================
-- 12. 在线用户表
-- ============================================
DROP TABLE IF EXISTS `t_sys_online_user`;
CREATE TABLE `t_sys_online_user`
(
    `id`             BIGINT(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '会话ID',
    `user_id`        BIGINT(20) unsigned NOT NULL COMMENT '用户ID',
    `username`       VARCHAR(50)       DEFAULT NULL COMMENT '用户名',
    `real_name`      VARCHAR(50)       DEFAULT NULL COMMENT '真实姓名',
    `dept_name`      VARCHAR(50)       DEFAULT NULL COMMENT '部门名称',
    `login_ip`       VARCHAR(50)       DEFAULT NULL COMMENT '登录IP',
    `login_location` VARCHAR(100)      DEFAULT NULL COMMENT '登录地点',
    `browser`        VARCHAR(50)       DEFAULT NULL COMMENT '浏览器类型',
    `os`             VARCHAR(50)       DEFAULT NULL COMMENT '操作系统',
    `device`         VARCHAR(50)       DEFAULT NULL COMMENT '设备类型',
    `login_time`     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '登录时间',
    `expire_time`    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '过期时间',
    `status`         TINYINT(2) NOT NULL DEFAULT 1 COMMENT '会话状态',
    PRIMARY KEY (`id`),
    KEY              `idx_user_id` (`user_id`),
    KEY              `idx_expire_time` (`expire_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='在线用户表';

-- ============================================
-- 初始化数据
-- ============================================

-- 插入部门数据
INSERT INTO `t_sys_department` (`dept_code`, `dept_name`, `parent_id`, `dept_level`, `dept_path`, `dept_sort`, `status`)
VALUES ('ROOT', '总部', 0, 1, '/ROOT', 1, 1),
       ('TECH', '技术部', 1, 2, '/ROOT/TECH', 1, 1),
       ('PROD', '产品部', 1, 2, '/ROOT/PROD', 2, 1),
       ('OPS', '运营部', 1, 2, '/ROOT/OPS', 3, 1);

-- 插入角色数据
INSERT INTO `t_sys_role` (`role_code`, `role_name`, `role_type`, `data_scope`, `description`, `is_system`, `status`,
                          `sort`)
VALUES ('SUPER_ADMIN', '超级管理员', 'SYSTEM', 'ALL', '系统超级管理员，拥有所有权限', 1, 1, 1),
       ('ADMIN', '管理员', 'BUSINESS', 'DEPT', '普通管理员，拥有基础权限', 1, 1, 2),
       ('USER', '普通用户', 'BUSINESS', 'SELF', '普通用户，仅能查看自己的数据', 1, 1, 3);

-- 插入用户数据（密码：admin123，已BCrypt加密）
INSERT INTO `t_sys_user` (`username`, `password`, `real_name`, `user_type`, `status`)
VALUES ('admin', '$2a$10$N.zmdr9k7uOCQb376OUp.eyOJ2HxjSj.0jQy2Zkx8yGqzQhL1QzE2', '系统管理员', 'ADMIN', 1),
       ('test', '$2a$10$N.zmdr9k7uOCQb376OUp.eyOJ2HxjSj.0jQy2Zkx8yGqzQhL1QzE2', '测试用户', 'ADMIN', 1);

-- 给超级管理员分配超级管理员角色
INSERT INTO `t_sys_user_role` (`user_id`, `role_id`)
SELECT u.id, r.id
FROM `t_sys_user` u,
     `t_sys_role` r
WHERE u.username = 'admin'
  AND r.role_code = 'SUPER_ADMIN';

-- 给测试用户分配管理员角色
INSERT INTO `t_sys_user_role` (`user_id`, `role_id`)
SELECT u.id, r.id
FROM `t_sys_user` u,
     `t_sys_role` r
WHERE u.username = 'test'
  AND r.role_code = 'ADMIN';

-- 插入菜单数据
INSERT INTO `t_sys_menu` (`menu_name`, `parent_id`, `menu_type`, `menu_icon`, `menu_path`, `menu_sort`, `status`)
VALUES ('系统管理', 0, 'M', 'el-icon-setting', '/system', 1, 1),
       ('用户管理', 1, 'C', 'el-icon-user', '/system/user', 1, 1),
       ('角色管理', 1, 'C', 'el-icon-s-custom', '/system/role', 2, 1),
       ('菜单管理', 1, 'C', 'el-icon-menu', '/system/menu', 3, 1),
       ('部门管理', 1, 'C', 'el-icon-office-building', '/system/dept', 4, 1),
       ('监控中心', 0, 'M', 'el-icon-monitor', '/monitor', 2, 1),
       ('在线用户', 6, 'C', 'el-icon-user-solid', '/monitor/online', 1, 1),
       ('操作日志', 6, 'C', 'el-icon-document', '/monitor/log', 2, 1);

-- 插入权限数据
INSERT INTO `t_sys_permission` (`permission_code`, `permission_name`, `resource_type`, `resource_path`, `http_method`,
                                `permission_group`)
VALUES ('system:user:list', '用户查询', 'api', '/api/admin/system/user/page', 'POST', '用户管理'),
       ('system:user:add', '用户新增', 'api', '/api/admin/system/user/add', 'POST', '用户管理'),
       ('system:user:edit', '用户编辑', 'api', '/api/admin/system/user/edit', 'PUT', '用户管理'),
       ('system:user:delete', '用户删除', 'api', '/api/admin/system/user/delete', 'DELETE', '用户管理'),
       ('system:user:get', '用户详情', 'api', '/api/admin/system/user/{id}', 'GET', '用户管理'),
       ('system:user:reset', '重置密码', 'api', '/api/admin/system/user/reset-password', 'PUT', '用户管理'),
       ('system:user:grant', '用户授权', 'api', '/api/admin/system/user/grant-roles', 'POST', '用户管理'),

       ('system:role:list', '角色查询', 'api', '/api/admin/system/role/list', 'GET', '角色管理'),
       ('system:role:add', '角色新增', 'api', '/api/admin/system/role/add', 'POST', '角色管理'),
       ('system:role:edit', '角色编辑', 'api', '/api/admin/system/role/edit', 'PUT', '角色管理'),
       ('system:role:delete', '角色删除', 'api', '/api/admin/system/role/delete', 'DELETE', '角色管理'),
       ('system:role:get', '角色详情', 'api', '/api/admin/system/role/{id}', 'GET', '角色管理'),
       ('system:role:grant', '角色授权', 'api', '/api/admin/system/role/grant-permissions', 'POST', '角色管理'),

       ('system:menu:list', '菜单查询', 'api', '/api/admin/system/menu/list', 'GET', '菜单管理'),
       ('system:menu:add', '菜单新增', 'api', '/api/admin/system/menu/add', 'POST', '菜单管理'),
       ('system:menu:edit', '菜单编辑', 'api', '/api/admin/system/menu/edit', 'PUT', '菜单管理'),
       ('system:menu:delete', '菜单删除', 'api', '/api/admin/system/menu/delete', 'DELETE', '菜单管理'),
       ('system:menu:get', '菜单详情', 'api', '/api/admin/system/menu/{id}', 'GET', '菜单管理'),

       ('system:dept:list', '部门查询', 'api', '/api/admin/system/dept/list', 'GET', '部门管理'),
       ('system:dept:add', '部门新增', 'api', '/api/admin/system/dept/add', 'POST', '部门管理'),
       ('system:dept:edit', '部门编辑', 'api', '/api/admin/system/dept/edit', 'PUT', '部门管理'),
       ('system:dept:delete', '部门删除', 'api', '/api/admin/system/dept/delete', 'DELETE', '部门管理'),
       ('system:dept:get', '部门详情', 'api', '/api/admin/system/dept/{id}', 'GET', '部门管理'),

       ('monitor:online:list', '在线用户查询', 'api', '/api/admin/monitor/online/list', 'GET', '监控中心'),
       ('monitor:online:kick', '踢出用户', 'api', '/api/admin/monitor/online/kick/{id}', 'DELETE', '监控中心'),
       ('monitor:log:list', '操作日志查询', 'api', '/api/admin/monitor/log/list', 'POST', '监控中心');

-- 给超级管理员分配所有权限
INSERT INTO `t_sys_role_permission` (`role_id`, `permission_id`)
SELECT r.id, p.id
FROM `t_sys_role` r,
     `t_sys_permission` p
WHERE r.role_code = 'SUPER_ADMIN';

-- 显示初始化完成信息
SELECT '============================================' AS '';
SELECT '数据库初始化完成！' AS '';
SELECT '============================================' AS '';
SELECT '默认管理员账号：admin' AS '';
SELECT '默认管理员密码：admin123' AS '';
SELECT '测试用户账号：test' AS '';
SELECT '测试用户密码：admin123' AS '';
SELECT '============================================' AS '';
