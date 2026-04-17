package com.zzl.platform.auth.controller;

import com.zzl.platform.auth.dto.PhoneLoginRequest;
import com.zzl.platform.auth.dto.RefreshTokenRequest;
import com.zzl.platform.auth.dto.SendCodeRequest;
import com.zzl.platform.auth.service.UserService;
import com.zzl.platform.auth.vo.LoginResponse;
import com.zzl.platform.common.core.res.Result;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * H5认证控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/h5/auth")
@Validated
public class H5AuthController {

    private final UserService userService;

    public H5AuthController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 发送验证码
     */
    @PostMapping("/send-code")
    public Result<Void> sendCode(@Valid @RequestBody SendCodeRequest request) {
        try {
            userService.sendCode(request);
            return Result.success("验证码发送成功", null);
        } catch (Exception e) {
            log.error("Send code error", e);
            return Result.fail(e.getMessage());
        }
    }

    /**
     * 手机号登录
     */
    @PostMapping("/login-phone")
    public Result<LoginResponse> loginPhone(@Valid @RequestBody PhoneLoginRequest request) {
        try {
            LoginResponse response = userService.h5PhoneLogin(request);
            return Result.success("登录成功", response);
        } catch (Exception e) {
            log.error("H5 phone login error", e);
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
                    request.getTokenType() != null ? request.getTokenType() : "h5"
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
            userService.logout(userId, token, "h5");
            return Result.success();
        } catch (Exception e) {
            log.error("Logout error", e);
            return Result.fail(e.getMessage());
        }
    }
}
