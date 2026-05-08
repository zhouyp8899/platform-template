package com.zzl.platform.auth.vo;

import com.zzl.platform.auth.enums.DataScopeType;
import com.zzl.platform.auth.enums.UserStatus;
import com.zzl.platform.auth.enums.UserType;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户VO
 */
@Data
public class UserVO {

    /**
     * 用户ID
     */
    private Long id;

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
     * 头像URL
     */
    private String avatar;

    /**
     * 性别
     */
    private String gender;

    /**
     * 部门ID
     */
    private Long deptId;

    /**
     * 部门名称
     */
    private String deptName;

    /**
     * 用户类型
     */
    private UserType userType;

    /**
     * 数据权限范围
     */
    private DataScopeType dataScope;

    /**
     * 状态: 1-正常, 2-禁用, 3-锁定
     */
    private UserStatus status;

    /**
     * 状态描述
     */
    private String statusDesc;

    /**
     * 角色ID列表（编辑时回显）
     */
    private List<Long> roleIds;

    /**
     * 角色编码列表
     */
    private List<String> roleCodes;

    /**
     * 角色名称列表
     */
    private List<String> roleNames;

    /**
     * 锁定时间
     */
    private LocalDateTime lockTime;

    /**
     * 登录失败次数
     */
    private Integer loginFailCount;

    /**
     * 最后登录时间
     */
    private LocalDateTime lastLoginTime;

    /**
     * 最后登录IP
     */
    private String lastLoginIp;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
