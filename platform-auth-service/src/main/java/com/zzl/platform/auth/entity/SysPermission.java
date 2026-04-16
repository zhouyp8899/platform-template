package com.zzl.platform.auth.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.zzl.platform.auth.enums.ResourceType;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统权限实体
 */
@Data
@TableName("t_sys_permission")
public class SysPermission {

    /**
     * 权限ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 权限编码
     */
    private String permissionCode;

    /**
     * 权限名称
     */
    private String permissionName;

    /**
     * 资源类型
     */
    private ResourceType resourceType;

    /**
     * 资源路径
     */
    private String resourcePath;

    /**
     * HTTP方法
     */
    private String httpMethod;

    /**
     * 关联菜单ID
     */
    private Long menuId;

    /**
     * 权限分组
     */
    private String permissionGroup;

    /**
     * 权限描述
     */
    private String description;

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
