package com.zzl.platform.common.db.interceptor;

import com.baomidou.mybatisplus.extension.plugins.inner.InnerInterceptor;
import com.zzl.platform.common.core.util.MdcUtils;
import org.apache.ibatis.mapping.MappedStatement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.Map;
import java.util.Properties;

/**
 * MDC MyBatis Plus拦截器
 * 将链路追踪信息传递到MyBatis执行上下文，用于数据库操作日志记录
 * 实现MyBatis Plus的InnerInterceptor接口
 */
public class MdcMyBatisInterceptor implements InnerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(MdcMyBatisInterceptor.class);

    /**
     * 是否启用SQL执行时间统计
     */
    private boolean enableSqlTime = true;

    /**
     * 是否启用SQL参数打印
     */
    private boolean enableSqlParams = false;

    /**
     * 慢SQL阈值（毫秒）
     */
    private long slowSqlThreshold = 1000L;

    /**
     * 设置MDC上下文
     */
    private void setupMdcContext(MappedStatement ms, String operation) {
        // 保存当前MDC上下文
        Map<String, String> oldContext = MDC.getCopyOfContextMap();

        try {
            // 确保MDC上下文存在
            if (!MdcUtils.hasTraceId()) {
                MdcUtils.init();
            }

            // 获取方法信息
            String statementId = ms.getId();
            String sqlCommandType = ms.getSqlCommandType().name();

            logger.debug("MyBatis执行: statementId={}, sqlCommandType={}, traceId={}, operation={}",
                    statementId, sqlCommandType, MdcUtils.getTraceId(), operation);

        } catch (Exception e) {
            logger.error("设置MDC上下文失败", e);
        }
    }

    /**
     * 设置属性
     */
    public void setProperties(Properties properties) {
        if (properties != null) {
            this.enableSqlTime = Boolean.parseBoolean(
                    properties.getProperty("enableSqlTime", "true"));
            this.enableSqlParams = Boolean.parseBoolean(
                    properties.getProperty("enableSqlParams", "false"));
            this.slowSqlThreshold = Long.parseLong(
                    properties.getProperty("slowSqlThreshold", "1000"));
        }
    }

    /**
     * Setter方法，支持外部配置
     */
    public void setEnableSqlTime(boolean enableSqlTime) {
        this.enableSqlTime = enableSqlTime;
    }

    public void setEnableSqlParams(boolean enableSqlParams) {
        this.enableSqlParams = enableSqlParams;
    }

    public void setSlowSqlThreshold(long slowSqlThreshold) {
        this.slowSqlThreshold = slowSqlThreshold;
    }
}
