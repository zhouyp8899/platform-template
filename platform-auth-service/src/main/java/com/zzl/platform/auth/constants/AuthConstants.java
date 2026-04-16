package com.zzl.platform.auth.constants;

/**
 * 认证常量
 */
public class AuthConstants {

    /**
     * Token前缀
     */
    public static final String TOKEN_PREFIX = "Bearer ";

    /**
     * Token请求头
     */
    public static final String TOKEN_HEADER = "Authorization";

    /**
     * 用户ID请求头
     */
    public static final String USER_ID_HEADER = "X-User-Id";

    /**
     * 用户名请求头
     */
    public static final String USERNAME_HEADER = "X-Username";

    /**
     * Token类型：H5用户
     */
    public static final String TOKEN_TYPE_H5 = "h5";

    /**
     * Token类型：管理后台
     */
    public static final String TOKEN_TYPE_ADMIN = "admin";

    /**
     * Redis Key前缀
     */
    public static final String REDIS_KEY_PREFIX = "auth:";

    /**
     * Token存储Key
     */
    public static final String REDIS_TOKEN_KEY = REDIS_KEY_PREFIX + "token:";

    /**
     * 用户信息缓存Key
     */
    public static final String REDIS_USER_INFO_KEY = REDIS_KEY_PREFIX + "user:";

    /**
     * 权限缓存Key
     */
    public static final String REDIS_PERMISSION_KEY = REDIS_KEY_PREFIX + "permission:";

    /**
     * 角色缓存Key
     */
    public static final String REDIS_ROLE_KEY = REDIS_KEY_PREFIX + "role:";

    /**
     * 验证码Key
     */
    public static final String REDIS_CODE_KEY = REDIS_KEY_PREFIX + "code:";

    /**
     * 登录失败计数Key
     */
    public static final String REDIS_FAIL_COUNT_KEY = REDIS_KEY_PREFIX + "fail:";

    /**
     * Token黑名单Key
     */
    public static final String REDIS_TOKEN_BLACKLIST_KEY = REDIS_KEY_PREFIX + "blacklist:";

    /**
     * 在线用户Key
     */
    public static final String REDIS_ONLINE_USER_KEY = REDIS_KEY_PREFIX + "online:";

    /**
     * 默认密码（admin123）
     */
    public static final String DEFAULT_PASSWORD = "$2a$10$N.zmdr9k7uOCQb376OUp.eyOJ2HxjSj.0jQy2Zkx8yGqzQhL1QzE2";

    /**
     * JWT密钥（生产环境应从配置中心读取）
     */
    public static final String JWT_SECRET_H5 = "platform-h5-jwt-secret-key-change-in-production";
    public static final String JWT_SECRET_ADMIN = "platform-admin-jwt-secret-key-change-in-production";

    /**
     * Token过期时间（秒）
     */
    public static final long TOKEN_EXPIRE_H5 = 2592000L;      // 30天
    public static final long TOKEN_EXPIRE_ADMIN = 7200L;       // 2小时
    public static final long REFRESH_TOKEN_EXPIRE_H5 = 7776000L; // 90天
    public static final long REFRESH_TOKEN_EXPIRE_ADMIN = 604800L; // 7天

    /**
     * 验证码过期时间（秒）
     */
    public static final long CODE_EXPIRE = 300L;             // 5分钟

    /**
     * 登录失败锁定时间（秒）
     */
    public static final long LOGIN_FAIL_LOCK_TIME = 900L;     // 15分钟

    /**
     * 最大登录失败次数
     */
    public static final int MAX_LOGIN_FAIL_COUNT = 5;

    /**
     * 超级管理员角色编码
     */
    public static final String SUPER_ADMIN_ROLE = "SUPER_ADMIN";

    /**
     * 缓存过期时间（秒）
     */
    public static final long CACHE_USER_INFO_EXPIRE = 3600L;   // 1小时
    public static final long CACHE_PERMISSION_EXPIRE = 3600L;  // 1小时
    public static final long CACHE_ROLE_EXPIRE = 3600L;        // 1小时

    private AuthConstants() {
        throw new IllegalStateException("Constants class cannot be instantiated");
    }
}
