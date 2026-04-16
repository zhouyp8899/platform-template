package com.zzl.platform.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 菜单编辑请求DTO
 */
@Data
public class MenuEditRequest {

    /**
     * 菜单ID
     */
    @NotNull(message = "菜单ID不能为空")
    private Long id;

    /**
     * 菜单名称
     */
    @NotBlank(message = "菜单名称不能为空")
    @Size(max = 50, message = "菜单名称长度不能超过50")
    private String menuName;

    /**
     * 父级菜单ID
     */
    private Long parentId;

    /**
     * 菜单类型
     */
    @NotBlank(message = "菜单类型不能为空")
    private String menuType;

    /**
     * 菜单图标
     */
    @Size(max = 100, message = "菜单图标长度不能超过100")
    private String menuIcon;

    /**
     * 路由地址
     */
    @Size(max = 200, message = "路由地址长度不能超过200")
    private String menuPath;

    /**
     * 组件路径
     */
    @Size(max = 200, message = "组件路径长度不能超过200")
    private String component;

    /**
     * 重定向地址
     */
    @Size(max = 200, message = "重定向地址长度不能超过200")
    private String redirect;

    /**
     * 是否缓存
     */
    private Integer isCache;

    /**
     * 是否显示
     */
    private Integer isVisible;

    /**
     * 是否外链
     */
    private Integer isExternal;

    /**
     * 显示顺序
     */
    private Integer menuSort;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 备注
     */
    @Size(max = 500, message = "备注长度不能超过500")
    private String remark;
}
