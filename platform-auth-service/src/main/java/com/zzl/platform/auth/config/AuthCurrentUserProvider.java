package com.zzl.platform.auth.config;

import com.zzl.platform.auth.constants.AuthConstants;
import com.zzl.platform.common.db.handler.CurrentUserProvider;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 从请求头获取当前用户ID
 */
@Slf4j
@Component
public class AuthCurrentUserProvider implements CurrentUserProvider {

    @Override
    public Long getCurrentUserId() {
        try {
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes == null) {
                return null;
            }
            HttpServletRequest request = attributes.getRequest();
            String userIdStr = request.getHeader(AuthConstants.USER_ID_HEADER);
            if (userIdStr != null && !userIdStr.isEmpty()) {
                return Long.parseLong(userIdStr);
            }
        } catch (Exception e) {
            log.warn("Get current user id from request header failed", e);
        }
        return null;
    }
}
