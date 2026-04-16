package com.zzl.platform.auth.service;

import com.zzl.platform.auth.entity.SysOnlineUser;
import com.zzl.platform.auth.vo.OnlineUserVO;

import java.util.List;

/**
 * 在线用户服务
 */
public interface OnlineUserService {

    /**
     * 查询在线用户列表
     *
     * @return 在线用户列表
     */
    List<OnlineUserVO> listOnlineUsers();

    /**
     * 踢出用户
     *
     * @param sessionId 会话ID
     */
    void kickOutUser(Long sessionId);

    /**
     * 踢出用户的所有会话
     *
     * @param userId 用户ID
     */
    void kickOutUserAll(Long userId);

    /**
     * 添加在线用户
     *
     * @param onlineUser 在线用户信息
     */
    void addOnlineUser(SysOnlineUser onlineUser);

    /**
     * 删除过期的在线用户
     */
    void deleteExpiredUsers();

    /**
     * 获取用户的在线会话
     *
     * @param userId 用户ID
     * @return 会话列表
     */
    List<SysOnlineUser> getUserSessions(Long userId);
}
