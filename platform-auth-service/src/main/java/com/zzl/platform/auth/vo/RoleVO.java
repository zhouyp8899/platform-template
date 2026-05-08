package com.zzl.platform.auth.vo;

import com.zzl.platform.auth.enums.DataScopeType;
import com.zzl.platform.auth.enums.RoleType;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 角色VO
 */
@Data
public class RoleVO {

    /**
     * 角色ID
     */
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
     * 状态: 1-正常, 2-禁用
     */
    private Integer status;

    /**
     * 状态描述
     */
    private String statusDesc;

    /**
     * 权限ID列表
     */
    private List<Long> permissionIds;

    /**
     * 权限编码列表
     */
    private List<String> permissionCodes;

    /**
     * 菜单ID列表
     */
    private List<Long> menuIds;

    /**
     * 用户数量
     */
    private Long userCount;

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
