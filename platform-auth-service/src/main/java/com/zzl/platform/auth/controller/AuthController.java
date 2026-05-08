package com.zzl.platform.auth.controller;

import com.zzl.platform.auth.dto.ChangePasswordRequest;
import com.zzl.platform.auth.dto.LoginRequest;
import com.zzl.platform.auth.dto.RefreshTokenRequest;
import com.zzl.platform.auth.service.MenuService;
import com.zzl.platform.auth.service.UserService;
import com.zzl.platform.auth.vo.LoginResponse;
import com.zzl.platform.auth.vo.MenuVO;
import com.zzl.platform.auth.vo.UserVO;
import com.zzl.platform.common.core.res.Result;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 认证控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/auth")
@Validated
public class AuthController {

    private final UserService userService;
    private final MenuService menuService;

    public AuthController(UserService userService, MenuService menuService) {
        this.userService = userService;
        this.menuService = menuService;
    }

    /**
     * 管理员登录
     */
    @PostMapping("/login")
    public Result<LoginResponse> adminLogin(@Valid @RequestBody LoginRequest request) {
        try {
            LoginResponse response = userService.adminLogin(request);
            return Result.success(response);
        } catch (Exception e) {
            log.error("Admin login error", e);
            return Result.fail(e.getMessage());
        }
    }

    /**
     * 刷新Token
     */
    @PostMapping("/refresh")
    public Result<LoginResponse> refreshToken(@RequestBody RefreshTokenRequest request) {
        try {
            LoginResponse response = userService.refreshToken(
                    request.getRefreshToken(),
                    request.getTokenType() != null ? request.getTokenType() : "admin"
            );
            return Result.success(response);
        } catch (Exception e) {
            log.error("Refresh token error", e);
            return Result.fail(e.getMessage());
        }
    }

    /**
     * 登出
     */
    @PostMapping("/logout")
    public Result<Void> logout(@RequestHeader("X-User-Id") Long userId,
                               @RequestHeader("Authorization") String token) {
        try {
            userService.logout(userId, token, "admin");
            return Result.success();
        } catch (Exception e) {
            log.error("Logout error", e);
            return Result.fail(e.getMessage());
        }
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/current")
    public Result<UserVO> getCurrentUser(@RequestHeader("X-User-Id") Long userId) {
        try {
            UserVO userVO = userService.getCurrentUser(userId);
            return Result.success(userVO);
        } catch (Exception e) {
            log.error("Get current user error", e);
            return Result.fail(e.getMessage());
        }
    }

    /**
     * 修改当前用户密码
     */
    @PutMapping("/current/change-password")
    public Result<Void> changeCurrentUserPassword(@RequestHeader("X-User-Id") Long userId,
                                                  @Valid @RequestBody ChangePasswordRequest request) {
        try {
            userService.changeCurrentUserPassword(userId, request);
            return Result.success();
        } catch (Exception e) {
            log.error("Change current user password error", e);
            return Result.fail(e.getMessage());
        }
    }

    /**
     * 获取当前用户菜单树（前端侧边栏用）
     */
    @GetMapping("/menus")
    public Result<List<MenuVO>> getCurrentUserMenus(@RequestHeader("X-User-Id") Long userId) {
        try {
            List<MenuVO> menus = menuService.treeUserMenus(userId);
            return Result.success(menus);
        } catch (Exception e) {
            log.error("Get current user menus error", e);
            return Result.fail(e.getMessage());
        }
    }
}
