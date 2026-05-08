package com.zzl.platform.common.db.handler;

/**
 * 当前用户提供者接口
 * 各服务需实现此接口，提供当前登录用户ID
 */
public interface CurrentUserProvider {

    /**
     * 获取当前登录用户ID
     *
     * @return 用户ID，未登录返回null
     */
    Long getCurrentUserId();
}
