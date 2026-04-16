package com.zzl.platform.auth.vo;

import com.zzl.platform.auth.enums.ResourceType;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 权限VO
 */
@Data
public class PermissionVO {

    /**
     * 权限ID
     */
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
     * 菜单名称
     */
    private String menuName;

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
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
