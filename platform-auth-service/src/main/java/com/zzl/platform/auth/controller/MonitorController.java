package com.zzl.platform.auth.controller;

import com.zzl.platform.auth.aspect.RequiresPermission;
import com.zzl.platform.auth.service.OnlineUserService;
import com.zzl.platform.auth.vo.OnlineUserVO;
import com.zzl.platform.common.core.res.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 监控控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/monitor")
public class MonitorController {

    private final OnlineUserService onlineUserService;

    public MonitorController(OnlineUserService onlineUserService) {
        this.onlineUserService = onlineUserService;
    }

    /**
     * 查询在线用户列表
     */
    @GetMapping("/online/list")
    @RequiresPermission(value = "monitor:online:list", desc = "在线用户查询")
    public Result<List<OnlineUserVO>> listOnlineUsers() {
        try {
            List<OnlineUserVO> users = onlineUserService.listOnlineUsers();
            return Result.success(users);
        } catch (Exception e) {
            log.error("List online users error", e);
            return Result.fail(e.getMessage());
        }
    }

    /**
     * 踐出用户
     */
    @DeleteMapping("/online/kick/{id}")
    @RequiresPermission(value = "monitor:online:kick", desc = "踢出用户")
    public Result<Void> kickOutUser(@PathVariable Long id) {
        try {
            onlineUserService.kickOutUser(id);
            return Result.success("踢出成功", null);
        } catch (Exception e) {
            log.error("Kick out user error", e);
            return Result.fail(e.getMessage());
        }
    }
}
