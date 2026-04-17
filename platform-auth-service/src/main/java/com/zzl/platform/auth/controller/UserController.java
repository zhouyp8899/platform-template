package com.zzl.platform.auth.controller;

import com.zzl.platform.auth.aspect.RequiresPermission;
import com.zzl.platform.auth.dto.*;
import com.zzl.platform.auth.service.UserService;
import com.zzl.platform.auth.vo.PageResponse;
import com.zzl.platform.auth.vo.UserVO;
import com.zzl.platform.common.core.res.Result;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/system/user")
@Validated
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 分页查询用户列表
     */
    @PostMapping("/page")
    @RequiresPermission(value = "system:user:list", desc = "用户查询")
    public Result<PageResponse<UserVO>> pageUser(@RequestBody PageRequest<UserQueryRequest> request) {
        try {
            PageResponse<UserVO> response = userService.pageUser(request);
            return Result.success(response);
        } catch (Exception e) {
            log.error("Page user error", e);
            return Result.fail(e.getMessage());
        }
    }

    /**
     * 根据ID查询用户详情
     */
    @GetMapping("/{id}")
    @RequiresPermission(value = "system:user:get", desc = "用户详情")
    public Result<UserVO> getUserById(@PathVariable Long id) {
        try {
            UserVO userVO = userService.getUserById(id);
            return Result.success(userVO);
        } catch (Exception e) {
            log.error("Get user by id error", e);
            return Result.fail(e.getMessage());
        }
    }

    /**
     * 新增用户
     */
    @PostMapping("/add")
    @RequiresPermission(value = "system:user:add", desc = "用户新增")
    public Result<Long> addUser(@Valid @RequestBody UserAddRequest request,
                                        @RequestHeader("X-User-Id") Long operatorId) {
        try {
            Long userId = userService.addUser(request, operatorId);
            return Result.success("用户新增成功", userId);
        } catch (Exception e) {
            log.error("Add user error", e);
            return Result.fail(e.getMessage());
        }
    }

    /**
     * 编辑用户
     */
    @PutMapping("/edit")
    @RequiresPermission(value = "system:user:edit", desc = "用户编辑")
    public Result<Void> editUser(@Valid @RequestBody UserEditRequest request,
                                         @RequestHeader("X-User-Id") Long operatorId) {
        try {
            userService.editUser(request, operatorId);
            return Result.success("用户编辑成功", null);
        } catch (Exception e) {
            log.error("Edit user error", e);
            return Result.fail(e.getMessage());
        }
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/delete/{id}")
    @RequiresPermission(value = "system:user:delete", desc = "用户删除")
    public Result<Void> deleteUser(@PathVariable Long id,
                                           @RequestHeader("X-User-Id") Long operatorId) {
        try {
            userService.deleteUser(id, operatorId);
            return Result.success("用户删除成功", null);
        } catch (Exception e) {
            log.error("Delete user error", e);
            return Result.fail(e.getMessage());
        }
    }

    /**
     * 批量删除用户
     */
    @DeleteMapping("/batch-delete")
    @RequiresPermission(value = "system:user:delete", desc = "用户删除")
    public Result<Void> batchDeleteUsers(@RequestBody List<Long> userIds,
                                                 @RequestHeader("X-User-Id") Long operatorId) {
        try {
            userService.batchDeleteUsers(userIds, operatorId);
            return Result.success("批量删除成功", null);
        } catch (Exception e) {
            log.error("Batch delete users error", e);
            return Result.fail(e.getMessage());
        }
    }

    /**
     * 重置用户密码
     */
    @PutMapping("/reset-password")
    @RequiresPermission(value = "system:user:reset", desc = "重置密码")
    public Result<Void> resetPassword(@RequestBody Long userId,
                                              @RequestHeader("X-User-Id") Long operatorId) {
        try {
            userService.resetPassword(userId, operatorId);
            return Result.success("密码重置成功", null);
        } catch (Exception e) {
            log.error("Reset password error", e);
            return Result.fail(e.getMessage());
        }
    }

    /**
     * 修改用户状态
     */
    @PutMapping("/change-status")
    @RequiresPermission(value = "system:user:edit", desc = "用户编辑")
    public Result<Void> changeUserStatus(@RequestBody ChangeStatusRequest request,
                                                 @RequestHeader("X-User-Id") Long operatorId) {
        try {
            userService.changeUserStatus(request.getUserId(), request.getStatus(), operatorId);
            return Result.success("状态修改成功", null);
        } catch (Exception e) {
            log.error("Change user status error", e);
            return Result.fail(e.getMessage());
        }
    }

    /**
     * 为用户分配角色
     */
    @PostMapping("/grant-roles")
    @RequiresPermission(value = "system:user:grant", desc = "用户授权")
    public Result<Void> grantRoles(@Valid @RequestBody GrantRolesRequest request,
                                           @RequestHeader("X-User-Id") Long operatorId) {
        try {
            userService.grantRoles(request, operatorId);
            return Result.success("角色分配成功", null);
        } catch (Exception e) {
            log.error("Grant roles error", e);
            return Result.fail(e.getMessage());
        }
    }

    /**
     * 获取用户的角色列表
     */
    @GetMapping("/{id}/roles")
    @RequiresPermission(value = "system:user:get", desc = "用户详情")
    public Result<List<Long>> getUserRoles(@PathVariable Long id) {
        try {
            List<Long> roleIds = userService.getUserRoles(id);
            return Result.success(roleIds);
        } catch (Exception e) {
            log.error("Get user roles error", e);
            return Result.fail(e.getMessage());
        }
    }
}
