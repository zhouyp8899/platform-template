package com.zzl.platform.auth;

import com.zzl.platform.common.db.config.MdcMyBatisConfig;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * MyBatis配置
 * 导入公共模块的MDC MyBatis配置
 */
@Configuration
@Import(MdcMyBatisConfig.class)
public class MyBatisConfig {
    // 此配置类主要用于导入MdcMyBatisConfig
    // 可以在此添加自定义的MyBatis配置
}
