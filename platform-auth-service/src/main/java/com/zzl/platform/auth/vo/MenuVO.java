package com.zzl.platform.auth.vo;

import com.zzl.platform.auth.enums.MenuType;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 菜单VO
 */
@Data
public class MenuVO {

    /**
     * 菜单ID
     */
    private Long id;

    /**
     * 菜单名称
     */
    private String menuName;

    /**
     * 父级菜单ID
     */
    private Long parentId;

    /**
     * 菜单类型
     */
    private MenuType menuType;

    /**
     * 菜单图标
     */
    private String menuIcon;

    /**
     * 路由地址
     */
    private String menuPath;

    /**
     * 组件路径
     */
    private String component;

    /**
     * 重定向地址
     */
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
     * 权限编码
     */
    private String permissionCode;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;

    /**
     * 子菜单
     */
    private List<MenuVO> children;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
