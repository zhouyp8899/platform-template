package com.zzl.platform.auth.service.controller;

import com.zzl.platform.auth.dto.PhoneLoginRequest;
import com.zzl.platform.auth.dto.RefreshTokenRequest;
import com.zzl.platform.auth.dto.SendCodeRequest;
import com.zzl.platform.auth.service.UserService;
import com.zzl.platform.auth.vo.LoginResponse;
import com.zzl.platform.auth.vo.ResponseResult;
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
    public ResponseResult<Void> sendCode(@Valid @RequestBody SendCodeRequest request) {
        try {
            userService.sendCode(request);
            return ResponseResult.success("验证码发送成功", null);
        } catch (Exception e) {
            log.error("Send code error", e);
            return ResponseResult.error(e.getMessage());
        }
    }

    /**
     * 手机号登录
     */
    @PostMapping("/login-phone")
    public ResponseResult<LoginResponse> loginPhone(@Valid @RequestBody PhoneLoginRequest request) {
        try {
            LoginResponse response = userService.h5PhoneLogin(request);
            return ResponseResult.success("登录成功", response);
        } catch (Exception e) {
            log.error("H5 phone login error", e);
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
                    request.getTokenType() != null ? request.getTokenType() : "h5"
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
            userService.logout(userId, token, "h5");
            return ResponseResult.success();
        } catch (Exception e) {
            log.error("Logout error", e);
            return ResponseResult.error(e.getMessage());
        }
    }
}
