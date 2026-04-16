package com.zzl.platform.auth.dto;

import lombok.Data;

/**
 * 用户查询请求DTO
 */
@Data
public class UserQueryRequest {

    /**
     * 用户名
     */
    private String username;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 部门ID
     */
    private Long deptId;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 用户类型
     */
    private String userType;
}
