package com.zzl.platform.auth.constants;

/**
 * 系统常量
 */
public class SystemConstants {

    /**
     * 默认分页大小
     */
    public static final int DEFAULT_PAGE_SIZE = 10;

    /**
     * 最大分页大小
     */
    public static final int MAX_PAGE_SIZE = 100;

    /**
     * 默认父级ID
     */
    public static final long DEFAULT_PARENT_ID = 0L;

    /**
     * 根部门ID
     */
    public static final long ROOT_DEPT_ID = 1L;

    /**
     * 根菜单ID
     */
    public static final long ROOT_MENU_ID = 1L;

    /**
     * 成功状态码
     */
    public static final int SUCCESS_CODE = 200;

    /**
     * 失败状态码
     */
    public static final int ERROR_CODE = 500;

    /**
     * 未授权状态码
     */
    public static final int UNAUTHORIZED_CODE = 401;

    /**
     * 无权限状态码
     */
    public static final int FORBIDDEN_CODE = 403;

    /**
     * 参数错误状态码
     */
    public static final int BAD_REQUEST_CODE = 400;

    private SystemConstants() {
        throw new IllegalStateException("Constants class cannot be instantiated");
    }
}
