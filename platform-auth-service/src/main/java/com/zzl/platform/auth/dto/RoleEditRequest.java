package com.zzl.platform.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 角色编辑请求DTO
 */
@Data
public class RoleEditRequest {

    /**
     * 角色ID
     */
    @NotNull(message = "角色ID不能为空")
    private Long id;

    /**
     * 角色编码
     */
    @NotBlank(message = "角色编码不能为空")
    @Size(max = 50, message = "角色编码长度不能超过50")
    private String roleCode;

    /**
     * 角色名称
     */
    @NotBlank(message = "角色名称不能为空")
    @Size(max = 50, message = "角色名称长度不能超过50")
    private String roleName;

    /**
     * 角色类型
     */
    private String roleType;

    /**
     * 数据范围
     */
    private String dataScope;

    /**
     * 角色描述
     */
    @Size(max = 500, message = "角色描述长度不能超过500")
    private String description;

    /**
     * 权限ID列表
     */
    private List<Long> permissionIds;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 状态
     */
    private Integer status;
}
