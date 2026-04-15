package com.zzl.platform.gw.session;

import com.zzl.platform.common.redis.service.RedisCacheService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Gateway会话管理服务
 * 基于Redis实现分布式会话，支持用户会话共享
 */
@Slf4j
@Service
public class GatewaySessionService {

    private static final String SESSION_PREFIX = "gateway:session:";
    private static final String USER_SESSION_PREFIX = "gateway:user-session:";
    private static final long DEFAULT_EXPIRE = 7200; // 2小时
    private final RedisCacheService cacheService;

    public GatewaySessionService(RedisCacheService cacheService) {
        this.cacheService = cacheService;
    }

    /**
     * 创建会话
     *
     * @param sessionId 会话ID
     * @param userId    用户ID
     * @param expiry    过期时间（秒）
     */
    public void createSession(String sessionId, String userId, Long expiry) {
        String sessionKey = SESSION_PREFIX + sessionId;
        long actualExpiry = expiry != null ? expiry : DEFAULT_EXPIRE;

        cacheService.set(sessionKey, userId, actualExpiry, TimeUnit.SECONDS);
        log.debug("Created session: {}, userId: {}, expiry: {}s", sessionId, userId, actualExpiry);
    }

    /**
     * 获取会话用户ID
     *
     * @param sessionId 会话ID
     * @return 用户ID
     */
    public String getSessionUserId(String sessionId) {
        String sessionKey = SESSION_PREFIX + sessionId;
        String userId = cacheService.get(sessionKey);

        if (userId != null) {
            log.debug("Found session: {}, userId: {}", sessionId, userId);
        } else {
            log.debug("Session not found: {}", sessionId);
        }

        return userId;
    }

    /**
     * 刷新会话
     *
     * @param sessionId 会话ID
     * @param expiry    新的过期时间（秒）
     */
    public void refreshSession(String sessionId, Long expiry) {
        String sessionKey = SESSION_PREFIX + sessionId;
        long actualExpiry = expiry != null ? expiry : DEFAULT_EXPIRE;

        cacheService.expire(sessionKey, actualExpiry, TimeUnit.SECONDS);
        log.debug("Refreshed session: {}, expiry: {}s", sessionId, actualExpiry);
    }

    /**
     * 删除会话
     *
     * @param sessionId 会话ID
     */
    public void deleteSession(String sessionId) {
        String sessionKey = SESSION_PREFIX + sessionId;
        Boolean result = cacheService.delete(sessionKey);

        if (result != null && result) {
            log.debug("Deleted session: {}", sessionId);
        }
    }

    /**
     * 创建用户会话（绑定用户ID和会话）
     *
     * @param userId    用户ID
     * @param sessionId 会话ID
     * @param expiry    过期时间（秒）
     */
    public void createUserSession(String userId, String sessionId, Long expiry) {
        String userSessionKey = USER_SESSION_PREFIX + userId;
        long actualExpiry = expiry != null ? expiry : DEFAULT_EXPIRE;

        cacheService.set(userSessionKey, sessionId, actualExpiry, TimeUnit.SECONDS);
        log.debug("Created user session: userId: {}, sessionId: {}, expiry: {}s", userId, sessionId, actualExpiry);
    }

    /**
     * 获取用户当前会话ID
     *
     * @param userId 用户ID
     * @return 会话ID
     */
    public String getUserSessionId(String userId) {
        String userSessionKey = USER_SESSION_PREFIX + userId;
        String sessionId = cacheService.get(userSessionKey);

        if (sessionId != null) {
            log.debug("Found user session: userId: {}, sessionId: {}", userId, sessionId);
        } else {
            log.debug("User session not found: userId: {}", userId);
        }

        return sessionId;
    }

    /**
     * 清除用户所有会话（用于用户登出）
     *
     * @param userId 用户ID
     */
    public void clearUserSessions(String userId) {
        String userSessionKey = USER_SESSION_PREFIX + userId;
        Boolean result = cacheService.delete(userSessionKey);

        if (result != null && result) {
            log.debug("Cleared user sessions: userId: {}", userId);
        }
    }

    /**
     * 检查会话是否存在
     *
     * @param sessionId 会话ID
     * @return 是否存在
     */
    public boolean sessionExists(String sessionId) {
        String sessionKey = SESSION_PREFIX + sessionId;
        return cacheService.exists(sessionKey);
    }

    /**
     * 获取会话剩余过期时间
     *
     * @param sessionId 会话ID
     * @return 剩余时间（秒）
     */
    public Long getSessionTtl(String sessionId) {
        String sessionKey = SESSION_PREFIX + sessionId;
        return cacheService.getExpire(sessionKey, TimeUnit.SECONDS);
    }
}
