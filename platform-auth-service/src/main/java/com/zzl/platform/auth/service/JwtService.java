package com.zzl.platform.auth.service;

import java.util.Map;

/**
 * JWT Token服务
 */
public interface JwtService {

    /**
     * 生成Token
     *
     * @param userId      用户ID
     * @param username    用户名
     * @param tokenType   Token类型（h5或admin）
     * @param extraClaims 额外声明信息（如角色、权限等）
     * @return Token
     */
    String generateToken(Long userId, String username, String tokenType, Map<String, Object> extraClaims);

    /**
     * 生成刷新Token
     *
     * @param userId    用户ID
     * @param username  用户名
     * @param tokenType Token类型
     * @return 刷新Token
     */
    String generateRefreshToken(Long userId, String username, String tokenType);

    /**
     * 解析Token
     *
     * @param token     Token
     * @param tokenType Token类型
     * @return 声明信息
     */
    Map<String, Object> parseToken(String token, String tokenType);

    /**
     * 验证Token
     *
     * @param token     Token
     * @param tokenType Token类型
     * @return 是否有效
     */
    boolean validateToken(String token, String tokenType);

    /**
     * 从Token中获取用户ID
     *
     * @param token     Token
     * @param tokenType Token类型
     * @return 用户ID
     */
    Long getUserIdFromToken(String token, String tokenType);

    /**
     * 从Token中获取用户名
     *
     * @param token     Token
     * @param tokenType Token类型
     * @return 用户名
     */
    String getUsernameFromToken(String token, String tokenType);

    /**
     * 检查Token是否即将过期（5分钟内）
     *
     * @param token     Token
     * @param tokenType Token类型
     * @return 是否即将过期
     */
    boolean isTokenExpiringSoon(String token, String tokenType);

    /**
     * 获取Token过期时间（秒）
     *
     * @param tokenType Token类型
     * @return 过期时间
     */
    long getTokenExpireTime(String tokenType);

    /**
     * 获取刷新Token过期时间（秒）
     *
     * @param tokenType Token类型
     * @return 过期时间
     */
    long getRefreshTokenExpireTime(String tokenType);
}
