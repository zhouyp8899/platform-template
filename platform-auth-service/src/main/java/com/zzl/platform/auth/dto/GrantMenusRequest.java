package com.zzl.platform.auth.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 角色分配菜单请求
 */
@Data
public class GrantMenusRequest {

    /**
     * 角色ID
     */
    @NotNull(message = "角色ID不能为空")
    private Long roleId;

    /**
     * 菜单ID列表
     */
    @NotEmpty(message = "菜单ID列表不能为空")
    private List<Long> menuIds;
}
