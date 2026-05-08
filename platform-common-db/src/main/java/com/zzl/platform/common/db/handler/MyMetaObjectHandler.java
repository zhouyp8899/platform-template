package com.zzl.platform.common.db.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;

import java.time.LocalDateTime;

/**
 * MyBatis-Plus 自动填充处理器
 * 自动填充 createTime、updateTime、createBy、updateBy 字段
 */
public class MyMetaObjectHandler implements MetaObjectHandler {

    private CurrentUserProvider currentUserProvider;

    public void setCurrentUserProvider(CurrentUserProvider currentUserProvider) {
        this.currentUserProvider = currentUserProvider;
    }

    @Override
    public void insertFill(MetaObject metaObject) {
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());

        Long userId = getCurrentUserId();
        if (userId != null) {
            this.strictInsertFill(metaObject, "createBy", Long.class, userId);
            this.strictInsertFill(metaObject, "updateBy", Long.class, userId);
        }
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());

        Long userId = getCurrentUserId();
        if (userId != null) {
            this.strictUpdateFill(metaObject, "updateBy", Long.class, userId);
        }
    }

    private Long getCurrentUserId() {
        if (currentUserProvider == null) {
            return null;
        }
        return currentUserProvider.getCurrentUserId();
    }
}
