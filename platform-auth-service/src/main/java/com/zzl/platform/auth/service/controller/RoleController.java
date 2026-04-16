package com.zzl.platform.auth.service.controller;

import com.zzl.platform.auth.aspect.RequiresPermission;
import com.zzl.platform.auth.dto.GrantPermissionsRequest;
import com.zzl.platform.auth.dto.RoleAddRequest;
import com.zzl.platform.auth.dto.RoleEditRequest;
import com.zzl.platform.auth.service.RoleService;
import com.zzl.platform.auth.vo.PageResponse;
import com.zzl.platform.auth.vo.ResponseResult;
import com.zzl.platform.auth.vo.RoleVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 角色管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/system/role")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    /**
     * 分页查询角色列表
     */
    @PostMapping("/page")
    @RequiresPermission(value = "system:role:list", desc = "角色查询")
    public ResponseResult<PageResponse<RoleVO>> pageRole(@RequestParam(defaultValue = "1") Integer pageNum,
                                                         @RequestParam(defaultValue = "10") Integer pageSize,
                                                         @RequestParam(required = false) String keyword) {
        try {
            PageResponse<RoleVO> response = roleService.pageRole(pageNum, pageSize, keyword);
            return ResponseResult.success(response);
        } catch (Exception e) {
            log.error("Page role error", e);
            return ResponseResult.error(e.getMessage());
        }
    }

    /**
     * 查询所有角色列表（下拉框用）
     */
    @GetMapping("/list")
    @RequiresPermission(value = "system:role:list", desc = "角色查询")
    public ResponseResult<List<RoleVO>> listAllRoles() {
        try {
            List<RoleVO> roles = roleService.listAllRoles();
            return ResponseResult.success(roles);
        } catch (Exception e) {
            log.error("List all roles error", e);
            return ResponseResult.error(e.getMessage());
        }
    }

    /**
     * 根据ID查询角色详情
     */
    @GetMapping("/{id}")
    @RequiresPermission(value = "system:role:get", desc = "角色详情")
    public ResponseResult<RoleVO> getRoleById(@PathVariable Long id) {
        try {
            RoleVO roleVO = roleService.getRoleById(id);
            return ResponseResult.success(roleVO);
        } catch (Exception e) {
            log.error("Get role by id error", e);
            return ResponseResult.error(e.getMessage());
        }
    }

    /**
     * 新增角色
     */
    @PostMapping("/add")
    @RequiresPermission(value = "system:role:add", desc = "角色新增")
    public ResponseResult<Long> addRole(@RequestBody RoleAddRequest request,
                                        @RequestHeader("X-User-Id") Long operatorId) {
        try {
            Long roleId = roleService.addRole(request, operatorId);
            return ResponseResult.success("角色新增成功", roleId);
        } catch (Exception e) {
            log.error("Add role error", e);
            return ResponseResult.error(e.getMessage());
        }
    }

    /**
     * 编辑角色
     */
    @PutMapping("/edit")
    @RequiresPermission(value = "system:role:edit", desc = "角色编辑")
    public ResponseResult<Void> editRole(@RequestBody RoleEditRequest request,
                                         @RequestHeader("X-User-Id") Long operatorId) {
        try {
            roleService.editRole(request, operatorId);
            return ResponseResult.success("角色编辑成功", null);
        } catch (Exception e) {
            log.error("Edit role error", e);
            return ResponseResult.error(e.getMessage());
        }
    }

    /**
     * 删除角色
     */
    @DeleteMapping("/delete/{id}")
    @RequiresPermission(value = "system:role:delete", desc = "角色删除")
    public ResponseResult<Void> deleteRole(@PathVariable Long id,
                                           @RequestHeader("X-User-Id") Long operatorId) {
        try {
            roleService.deleteRole(id, operatorId);
            return ResponseResult.success("角色删除成功", null);
        } catch (Exception e) {
            log.error("Delete role error", e);
            return ResponseResult.error(e.getMessage());
        }
    }

    /**
     * 为角色分配权限
     */
    @PostMapping("/grant-permissions")
    @RequiresPermission(value = "system:role:grant", desc = "角色授权")
    public ResponseResult<Void> grantPermissions(@RequestBody GrantPermissionsRequest request,
                                                 @RequestHeader("X-User-Id") Long operatorId) {
        try {
            roleService.grantPermissions(request, operatorId);
            return ResponseResult.success("权限分配成功", null);
        } catch (Exception e) {
            log.error("Grant permissions error", e);
            return ResponseResult.error(e.getMessage());
        }
    }

    /**
     * 获取角色的权限列表
     */
    @GetMapping("/{id}/permissions")
    @RequiresPermission(value = "system:role:get", desc = "角色详情")
    public ResponseResult<List<Long>> getRolePermissions(@PathVariable Long id) {
        try {
            List<Long> permissionIds = roleService.getRolePermissions(id);
            return ResponseResult.success(permissionIds);
        } catch (Exception e) {
            log.error("Get role permissions error", e);
            return ResponseResult.error(e.getMessage());
        }
    }
}
