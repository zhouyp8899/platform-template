package com.zzl.platform.auth.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.zzl.platform.auth.enums.DataScopeType;
import com.zzl.platform.auth.enums.RoleType;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统角色实体
 */
@Data
@TableName("t_sys_role")
public class SysRole {

    /**
     * 角色ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 角色编码
     */
    private String roleCode;

    /**
     * 角色名称
     */
    private String roleName;

    /**
     * 角色类型
     */
    private RoleType roleType;

    /**
     * 数据范围
     */
    private DataScopeType dataScope;

    /**
     * 角色描述
     */
    private String description;

    /**
     * 是否系统内置角色
     */
    private Integer isSystem;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 排序
     */
    private Integer sort;

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
