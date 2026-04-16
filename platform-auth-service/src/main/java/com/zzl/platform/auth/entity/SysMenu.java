package com.zzl.platform.auth.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.zzl.platform.auth.enums.MenuType;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统菜单实体
 */
@Data
@TableName("t_sys_menu")
public class SysMenu {

    /**
     * 菜单ID
     */
    @TableId(type = IdType.AUTO)
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
     * 状态
     */
    private Integer status;

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
