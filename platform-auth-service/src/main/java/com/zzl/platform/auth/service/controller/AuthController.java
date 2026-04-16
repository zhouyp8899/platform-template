package com.zzl.platform.auth.service.controller;

import com.zzl.platform.auth.dto.ChangePasswordRequest;
import com.zzl.platform.auth.dto.LoginRequest;
import com.zzl.platform.auth.dto.RefreshTokenRequest;
import com.zzl.platform.auth.service.UserService;
import com.zzl.platform.auth.vo.LoginResponse;
import com.zzl.platform.auth.vo.ResponseResult;
import com.zzl.platform.auth.vo.UserVO;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/auth")
@Validated
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 管理员登录
     */
    @PostMapping("/login")
    public ResponseResult<LoginResponse> adminLogin(@Valid @RequestBody LoginRequest request) {
        try {
            LoginResponse response = userService.adminLogin(request);
            return ResponseResult.success(response);
        } catch (Exception e) {
            log.error("Admin login error", e);
            return ResponseResult.error(e.getMessage());
        }
    }

    /**
     * 刷新Token
     */
    @PostMapping("/refresh")
    public ResponseResult<LoginResponse> refreshToken(@RequestBody RefreshTokenRequest request) {
        try {
            LoginResponse response = userService.refreshToken(
                    request.getRefreshToken(),
                    request.getTokenType() != null ? request.getTokenType() : "admin"
            );
            return ResponseResult.success(response);
        } catch (Exception e) {
            log.error("Refresh token error", e);
            return ResponseResult.error(e.getMessage());
        }
    }

    /**
     * 登出
     */
    @PostMapping("/logout")
    public ResponseResult<Void> logout(@RequestHeader("X-User-Id") Long userId,
                                       @RequestHeader("Authorization") String token) {
        try {
            userService.logout(userId, token, "admin");
            return ResponseResult.success();
        } catch (Exception e) {
            log.error("Logout error", e);
            return ResponseResult.error(e.getMessage());
        }
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/current")
    public ResponseResult<UserVO> getCurrentUser(@RequestHeader("X-User-Id") Long userId) {
        try {
            UserVO userVO = userService.getCurrentUser(userId);
            return ResponseResult.success(userVO);
        } catch (Exception e) {
            log.error("Get current user error", e);
            return ResponseResult.error(e.getMessage());
        }
    }

    /**
     * 修改当前用户密码
     */
    @PutMapping("/current/change-password")
    public ResponseResult<Void> changeCurrentUserPassword(@RequestHeader("X-User-Id") Long userId,
                                                          @Valid @RequestBody ChangePasswordRequest request) {
        try {
            userService.changeCurrentUserPassword(userId, request);
            return ResponseResult.success();
        } catch (Exception e) {
            log.error("Change current user password error", e);
            return ResponseResult.error(e.getMessage());
        }
    }
}
