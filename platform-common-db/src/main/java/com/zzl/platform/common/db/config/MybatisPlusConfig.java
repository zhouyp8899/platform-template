package com.zzl.platform.common.db.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis Plus 统一配置
 * <p>
 * 特点：
 * 1. 使用 @ConditionalOnMissingBean 避免与子模块配置冲突
 * 2. 支持通过 YAML 配置覆盖参数
 * 3. 统一管理分页插件
 */
@Configuration
@ConditionalOnMissingBean(MybatisPlusInterceptor.class)
public class MybatisPlusConfig {

    /**
     * MyBatis Plus 拦截器
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor(MybatisPlusProperties properties) {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        // 分页插件
        PaginationInnerInterceptor paginationInnerInterceptor = new PaginationInnerInterceptor(DbType.MYSQL);

        // 允许通过配置覆盖默认值
        paginationInnerInterceptor.setMaxLimit(properties.getMaxLimit() > 0 ? properties.getMaxLimit() : 1000L);
        paginationInnerInterceptor.setOverflow(properties.isOverflow());

        interceptor.addInnerInterceptor(paginationInnerInterceptor);
        return interceptor;
    }

    /**
     * MyBatis 基础配置
     */
    @Bean
    @ConditionalOnMissingBean(MybatisConfiguration.class)
    public MybatisConfiguration mybatisConfiguration() {
        MybatisConfiguration configuration = new MybatisConfiguration();
        configuration.setMapUnderscoreToCamelCase(true);
        return configuration;
    }

    /**
     * MyBatis Plus 配置属性
     * 可在各服务的 application.yml 中覆盖
     */
    @Bean
    @ConfigurationProperties(prefix = "platform.mybatis-plus")
    public MybatisPlusProperties mybatisPlusProperties() {
        return new MybatisPlusProperties();
    }

    /**
     * MyBatis Plus 配置属性类
     */
    public static class MybatisPlusProperties {
        /**
         * 单页最大限制数量
         * 默认：1000
         */
        private long maxLimit = 1000L;

        /**
         * 溢出总页数后是否进行处理
         * 默认：false
         */
        private boolean overflow = false;

        /**
         * 是否启用安全 SQL 检查
         * 默认：true
         */
        private boolean safeSql = true;

        public long getMaxLimit() {
            return maxLimit;
        }

        public void setMaxLimit(long maxLimit) {
            this.maxLimit = maxLimit;
        }

        public boolean isOverflow() {
            return overflow;
        }

        public void setOverflow(boolean overflow) {
            this.overflow = overflow;
        }

        public boolean isSafeSql() {
            return safeSql;
        }

        public void setSafeSql(boolean safeSql) {
            this.safeSql = safeSql;
        }
    }
}
