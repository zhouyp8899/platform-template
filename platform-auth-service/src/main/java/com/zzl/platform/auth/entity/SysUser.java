package com.zzl.platform.auth.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.zzl.platform.auth.enums.DataScopeType;
import com.zzl.platform.auth.enums.UserStatus;
import com.zzl.platform.auth.enums.UserType;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统用户实体
 */
@Data
@TableName("t_sys_user")
public class SysUser {

    /**
     * 用户ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码（BCrypt加密）
     */
    private String password;

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
     * 性别: MALE-男, FEMALE-女, UNKNOWN-未知
     */
    private String gender;

    /**
     * 所属部门ID
     */
    private Long deptId;

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
     * 最后登录时间
     */
    private LocalDateTime lastLoginTime;

    /**
     * 最后登录IP
     */
    private String lastLoginIp;

    /**
     * 登录失败次数
     */
    private Integer loginFailCount;

    /**
     * 锁定时间
     */
    private LocalDateTime lockTime;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 创建人
     */
    @TableField(fill = FieldFill.INSERT)
    private Long createBy;

    /**
     * 更新人
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateBy;

    /**
     * 是否删除
     */
    @TableLogic
    private Integer deleted;
}
