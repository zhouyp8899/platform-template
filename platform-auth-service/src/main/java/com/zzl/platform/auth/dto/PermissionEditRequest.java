package com.zzl.platform.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 权限编辑请求DTO
 */
@Data
public class PermissionEditRequest {

    /**
     * 权限ID
     */
    @NotNull(message = "权限ID不能为空")
    private Long id;

    /**
     * 权限编码
     */
    @NotBlank(message = "权限编码不能为空")
    @Size(max = 100, message = "权限编码长度不能超过100")
    private String permissionCode;

    /**
     * 权限名称
     */
    @NotBlank(message = "权限名称不能为空")
    @Size(max = 100, message = "权限名称长度不能超过100")
    private String permissionName;

    /**
     * 资源类型
     */
    @NotBlank(message = "资源类型不能为空")
    private String resourceType;

    /**
     * 资源路径
     */
    @Size(max = 200, message = "资源路径长度不能超过200")
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
    @Size(max = 50, message = "权限分组长度不能超过50")
    private String permissionGroup;

    /**
     * 权限描述
     */
    @Size(max = 200, message = "权限描述长度不能超过200")
    private String description;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 排序
     */
    private Integer sort;
}
