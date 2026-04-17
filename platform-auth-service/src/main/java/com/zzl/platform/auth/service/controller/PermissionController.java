package com.zzl.platform.auth.service.controller;

import com.zzl.platform.auth.aspect.RequiresPermission;
import com.zzl.platform.auth.dto.PermissionAddRequest;
import com.zzl.platform.auth.dto.PermissionEditRequest;
import com.zzl.platform.auth.service.PermissionService;
import com.zzl.platform.auth.vo.PageResponse;
import com.zzl.platform.auth.vo.PermissionVO;
import com.zzl.platform.common.core.res.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 权限管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/system/permission")
public class PermissionController {

    private final PermissionService permissionService;

    public PermissionController(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    /**
     * 分页查询权限列表
     */
    @PostMapping("/page")
    @RequiresPermission(value = "system:permission:list", desc = "权限查询")
    public Result<PageResponse<PermissionVO>> pagePermission(@RequestParam(defaultValue = "1") Integer pageNum,
                                                             @RequestParam(defaultValue = "10") Integer pageSize,
                                                             @RequestParam(required = false) String keyword) {
        try {
            PageResponse<PermissionVO> response = permissionService.pagePermission(pageNum, pageSize, keyword);
            return Result.success(response);
        } catch (Exception e) {
            log.error("Page permission error", e);
            return Result.fail(e.getMessage());
        }
    }

    /**
     * 查询所有权限列表
     */
    @GetMapping("/list")
    @RequiresPermission(value = "system:permission:list", desc = "权限查询")
    public Result<List<PermissionVO>> listAllPermissions() {
        try {
            List<PermissionVO> permissions = permissionService.listAllPermissions();
            return Result.success(permissions);
        } catch (Exception e) {
            log.error("List all permissions error", e);
            return Result.fail(e.getMessage());
        }
    }

    /**
     * 根据ID查询权限详情
     */
    @GetMapping("/{id}")
    @RequiresPermission(value = "system:permission:get", desc = "权限详情")
    public Result<PermissionVO> getPermissionById(@PathVariable Long id) {
        try {
            PermissionVO permission = permissionService.getPermissionById(id);
            return Result.success(permission);
        } catch (Exception e) {
            log.error("Get permission by id error", e);
            return Result.fail(e.getMessage());
        }
    }

    /**
     * 新增权限
     */
    @PostMapping("/add")
    @RequiresPermission(value = "system:permission:add", desc = "权限新增")
    public Result<Long> addPermission(@RequestBody PermissionAddRequest request,
                                              @RequestHeader("X-User-Id") Long operatorId) {
        try {
            Long permissionId = permissionService.addPermission(request, operatorId);
            return Result.success("权限新增成功", permissionId);
        } catch (Exception e) {
            log.error("Add permission error", e);
            return Result.fail(e.getMessage());
        }
    }

    /**
     * 编辑权限
     */
    @PutMapping("/edit")
    @RequiresPermission(value = "system:permission:edit", desc = "权限编辑")
    public Result<Void> editPermission(@RequestBody PermissionEditRequest request,
                                               @RequestHeader("X-User-Id") Long operatorId) {
        try {
            permissionService.editPermission(request, operatorId);
            return Result.success("权限编辑成功", null);
        } catch (Exception e) {
            log.error("Edit permission error", e);
            return Result.fail(e.getMessage());
        }
    }

    /**
     * 删除权限
     */
    @DeleteMapping("/delete/{id}")
    @RequiresPermission(value = "system:permission:delete", desc = "权限删除")
    public Result<Void> deletePermission(@PathVariable Long id,
                                                 @RequestHeader("X-User-Id") Long operatorId) {
        try {
            permissionService.deletePermission(id, operatorId);
            return Result.success("权限删除成功", null);
        } catch (Exception e) {
            log.error("Delete permission error", e);
            return Result.fail(e.getMessage());
        }
    }
}
