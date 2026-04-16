package com.zzl.platform.auth.aspect;

import com.zzl.platform.auth.constants.AuthConstants;
import com.zzl.platform.auth.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 权限校验切面
 */
@Slf4j
@Aspect
@Component
public class PermissionAspect {

    private final UserService userService;
    private final RedisTemplate<String, Object> redisTemplate;

    public PermissionAspect(UserService userService, RedisTemplate<String, Object> redisTemplate) {
        this.userService = userService;
        this.redisTemplate = redisTemplate;
    }

    /**
     * 权限校验
     */
    @Around("@annotation(requiresPermission)")
    public Object checkPermission(ProceedingJoinPoint joinPoint, RequiresPermission requiresPermission) throws Throwable {
        Long userId = getCurrentUserId();
        if (userId == null) {
            throw new RuntimeException("用户未登录");
        }

        String permissionCode = requiresPermission.value();

        // 检查是否超级管理员
        if (userService.hasRole(userId, AuthConstants.SUPER_ADMIN_ROLE)) {
            return joinPoint.proceed();
        }

        // 检查权限
        if (!userService.hasPermission(userId, permissionCode)) {
            throw new RuntimeException("无权限: " + requiresPermission.desc());
        }

        return joinPoint.proceed();
    }

    /**
     * 角色校验
     */
    @Around("@annotation(requiresRole)")
    public Object checkRole(ProceedingJoinPoint joinPoint, RequiresRole requiresRole) throws Throwable {
        Long userId = getCurrentUserId();
        if (userId == null) {
            throw new RuntimeException("用户未登录");
        }

        String roleCode = requiresRole.value();

        // 检查角色
        if (!userService.hasRole(userId, roleCode)) {
            throw new RuntimeException("无角色: " + requiresRole.desc());
        }

        return joinPoint.proceed();
    }

    /**
     * 获取当前登录用户ID
     */
    private Long getCurrentUserId() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes == null) {
                return null;
            }

            HttpServletRequest request = attributes.getRequest();
            String userIdStr = request.getHeader(AuthConstants.USER_ID_HEADER);
            if (userIdStr != null && !userIdStr.isEmpty()) {
                return Long.parseLong(userIdStr);
            }
        } catch (Exception e) {
            log.error("Get current user id error", e);
        }
        return null;
    }
}
