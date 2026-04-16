package com.zzl.platform.auth.vo;

import lombok.Data;

/**
 * 登录响应VO
 */
@Data
public class LoginResponse {

    /**
     * 访问Token
     */
    private String accessToken;

    /**
     * 刷新Token
     */
    private String refreshToken;

    /**
     * Token过期时间（秒）
     */
    private Long expireIn;

    /**
     * 用户信息
     */
    private UserInfo userInfo;

    @Data
    public static class UserInfo {
        /**
         * 用户ID
         */
        private Long userId;

        /**
         * 用户名
         */
        private String username;

        /**
         * 真实姓名
         */
        private String realName;

        /**
         * 昵称
         */
        private String nickName;

        /**
         * 手机号
         */
        private String phone;

        /**
         * 邮箱
         */
        private String email;

        /**
         * 头像
         */
        private String avatar;

        /**
         * 部门ID
         */
        private Long deptId;

        /**
         * 部门名称
         */
        private String deptName;

        /**
         * 角色编码列表
         */
        private java.util.List<String> roles;

        /**
         * 权限编码列表
         */
        private java.util.Set<String> permissions;
    }
}
