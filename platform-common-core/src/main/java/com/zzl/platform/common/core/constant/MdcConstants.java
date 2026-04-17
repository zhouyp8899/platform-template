package com.zzl.platform.common.core.constant;

/**
 * MDC (Mapped Diagnostic Context) 常量定义
 * 用于链路追踪的MDC键名常量
 */
public class MdcConstants {

    /**
     * 链路追踪ID - 全局唯一标识一次请求的完整调用链路
     */
    public static final String TRACE_ID = "traceId";

    /**
     * 父级追踪ID - 用于标识父级调用链路
     */
    public static final String PARENT_TRACE_ID = "parentTraceId";

    /**
     * 跨度ID - 标识当前调用跨度
     */
    public static final String SPAN_ID = "spanId";

    /**
     * 用户ID - 当前操作用户标识
     */
    public static final String USER_ID = "userId";

    /**
     * 用户名 - 当前操作用户名称
     */
    public static final String USERNAME = "username";

    /**
     * 应用名称 - 当前应用服务标识
     */
    public static final String APP_NAME = "appName";

    /**
     * 租户ID - 多租户场景下的租户标识
     */
    public static final String TENANT_ID = "tenantId";

    /**
     * 客户端IP - 请求来源IP地址
     */
    public static final String CLIENT_IP = "clientIp";

    /**
     * 请求URI - 请求路径
     */
    public static final String REQUEST_URI = "requestUri";

    /**
     * 请求方法 - HTTP请求方法
     */
    public static final String REQUEST_METHOD = "requestMethod";

    /**
     * 业务类型 - 自定义业务分类标识
     */
    public static final String BUSINESS_TYPE = "businessType";

    /**
     * 环境标识 - 运行环境
     */
    public static final String ENV = "env";

    /**
     * 默认追踪ID
     */
    public static final String DEFAULT_TRACE_ID = "N/A";

    private MdcConstants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
