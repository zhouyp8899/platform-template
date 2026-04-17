package com.zzl.platform.common.db.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.zzl.platform.common.db.interceptor.MdcMyBatisInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis MDC配置
 * 注册MDC拦截器，确保数据库操作包含链路追踪信息
 */
@Configuration
@MapperScan("com.zzl.platform.**.mapper")
public class MdcMyBatisConfig {

    /**
     * 是否启用SQL执行时间统计
     */
    @Value("${mybatis.mdc.enable-sql-time:true}")
    private boolean enableSqlTime;

    /**
     * 是否启用SQL参数打印
     */
    @Value("${mybatis.mdc.enable-sql-params:false}")
    private boolean enableSqlParams;

    /**
     * 慢SQL阈值（毫秒）
     */
    @Value("${mybatis.mdc.slow-sql-threshold:1000}")
    private long slowSqlThreshold;

    /**
     * 配置MyBatis Plus拦截器链
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        // 添加分页插件
        PaginationInnerInterceptor paginationInterceptor = new PaginationInnerInterceptor(DbType.MYSQL);
        interceptor.addInnerInterceptor(paginationInterceptor);

        // 添加MDC拦截器
        MdcMyBatisInterceptor mdcInterceptor = new MdcMyBatisInterceptor();
        mdcInterceptor.setEnableSqlTime(enableSqlTime);
        mdcInterceptor.setEnableSqlParams(enableSqlParams);
        mdcInterceptor.setSlowSqlThreshold(slowSqlThreshold);
        interceptor.addInnerInterceptor(mdcInterceptor);

        return interceptor;
    }
}
