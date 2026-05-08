package com.zzl.platform.common.db.config;

import com.zzl.platform.common.db.handler.CurrentUserProvider;
import com.zzl.platform.common.db.handler.MyMetaObjectHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus 自动填充配置
 * 独立于 MybatisPlusConfig，避免被 @ConditionalOnMissingBean 条件跳过
 */
@Configuration
public class AutoFillConfig {

    @Bean
    @ConditionalOnMissingBean
    public MyMetaObjectHandler myMetaObjectHandler(@Autowired(required = false) CurrentUserProvider currentUserProvider) {
        MyMetaObjectHandler handler = new MyMetaObjectHandler();
        handler.setCurrentUserProvider(currentUserProvider);
        return handler;
    }
}
