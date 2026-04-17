package com.zzl.platform.common.core.util;

import com.zzl.platform.common.core.constant.MdcConstants;
import org.slf4j.MDC;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.Supplier;

/**
 * MDC (Mapped Diagnostic Context) 工具类
 * 提供链路追踪相关的MDC操作，包括初始化、清理、跨线程传递等功能
 */
public class MdcUtils {

    private MdcUtils() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * 初始化MDC上下文
     * 生成新的追踪ID并初始化MDC
     *
     * @return 新生成的追踪ID
     */
    public static String init() {
        String traceId = TraceIdGenerator.generate();
        put(MdcConstants.TRACE_ID, traceId);
        return traceId;
    }

    /**
     * 初始化MDC上下文（使用指定追踪ID）
     *
     * @param traceId 追踪ID
     * @return 实际使用的追踪ID
     */
    public static String init(String traceId) {
        if (traceId == null || traceId.isEmpty()) {
            traceId = TraceIdGenerator.generate();
        }
        put(MdcConstants.TRACE_ID, traceId);
        return traceId;
    }

    /**
     * 获取当前追踪ID
     *
     * @return 追踪ID，如果不存在返回默认值
     */
    public static String getTraceId() {
        return get(MdcConstants.TRACE_ID, MdcConstants.DEFAULT_TRACE_ID);
    }

    /**
     * 检查是否存在追踪ID
     *
     * @return 是否存在
     */
    public static boolean hasTraceId() {
        return MDC.get(MdcConstants.TRACE_ID) != null;
    }

    /**
     * 设置用户信息
     *
     * @param userId   用户ID
     * @param username 用户名
     */
    public static void setUser(String userId, String username) {
        put(MdcConstants.USER_ID, userId);
        put(MdcConstants.USERNAME, username);
    }

    /**
     * 设置应用名称
     *
     * @param appName 应用名称
     */
    public static void setAppName(String appName) {
        put(MdcConstants.APP_NAME, appName);
    }

    /**
     * 设置租户ID
     *
     * @param tenantId 租户ID
     */
    public static void setTenantId(String tenantId) {
        put(MdcConstants.TENANT_ID, tenantId);
    }

    /**
     * 设置客户端IP
     *
     * @param clientIp 客户端IP
     */
    public static void setClientIp(String clientIp) {
        put(MdcConstants.CLIENT_IP, clientIp);
    }

    /**
     * 设置请求信息
     *
     * @param uri    请求URI
     * @param method 请求方法
     */
    public static void setRequest(String uri, String method) {
        put(MdcConstants.REQUEST_URI, uri);
        put(MdcConstants.REQUEST_METHOD, method);
    }

    /**
     * 设置业务类型
     *
     * @param businessType 业务类型
     */
    public static void setBusinessType(String businessType) {
        put(MdcConstants.BUSINESS_TYPE, businessType);
    }

    /**
     * 设置环境标识
     *
     * @param env 环境标识
     */
    public static void setEnv(String env) {
        put(MdcConstants.ENV, env);
    }

    /**
     * 存储父级追踪ID并生成新的子追踪ID
     * 用于跨服务调用场景
     *
     * @param parentTraceId 父级追踪ID
     * @return 新的追踪ID
     */
    public static String createChildTraceId(String parentTraceId) {
        if (parentTraceId != null && !parentTraceId.isEmpty()) {
            put(MdcConstants.PARENT_TRACE_ID, parentTraceId);
        }
        return init();
    }

    /**
     * 恢复父级追踪ID为当前追踪ID
     * 用于服务调用返回后恢复上下文
     */
    public static void restoreParentTraceId() {
        String parentTraceId = get(MdcConstants.PARENT_TRACE_ID, null);
        if (parentTraceId != null && !parentTraceId.isEmpty()) {
            put(MdcConstants.TRACE_ID, parentTraceId);
            remove(MdcConstants.PARENT_TRACE_ID);
        }
    }

    /**
     * 存储MDC键值对
     *
     * @param key   键
     * @param value 值
     */
    public static void put(String key, String value) {
        if (key != null && value != null) {
            MDC.put(key, value);
        }
    }

    /**
     * 获取MDC值
     *
     * @param key          键
     * @param defaultValue 默认值
     * @return 值
     */
    public static String get(String key, String defaultValue) {
        String value = MDC.get(key);
        return value != null ? value : defaultValue;
    }

    /**
     * 移除MDC键值对
     *
     * @param key 键
     */
    public static void remove(String key) {
        MDC.remove(key);
    }

    /**
     * 清空MDC上下文
     */
    public static void clear() {
        MDC.clear();
    }

    /**
     * 获取当前MDC上下文副本
     *
     * @return MDC上下文副本
     */
    public static Map<String, String> getCopyOfContextMap() {
        return MDC.getCopyOfContextMap();
    }

    /**
     * 设置MDC上下文
     *
     * @param contextMap 上下文
     */
    public static void setContextMap(Map<String, String> contextMap) {
        if (contextMap != null && !contextMap.isEmpty()) {
            MDC.setContextMap(contextMap);
        }
    }

    /**
     * 线程包装：将当前MDC上下文传递到新线程
     * 用于确保异步线程继承主线程的MDC上下文
     *
     * @param runnable 可执行任务
     * @return 包装后的Runnable
     */
    public static Runnable wrap(Runnable runnable) {
        if (runnable == null) {
            return null;
        }
        Map<String, String> contextMap = getCopyOfContextMap();
        return () -> {
            Map<String, String> oldContext = getCopyOfContextMap();
            try {
                setContextMap(contextMap);
                runnable.run();
            } finally {
                setContextMap(oldContext);
            }
        };
    }

    /**
     * 线程包装：将当前MDC上下文传递到Callable
     *
     * @param callable 可调用任务
     * @param <T>      返回类型
     * @return 包装后的Callable
     */
    public static <T> Callable<T> wrap(Callable<T> callable) {
        if (callable == null) {
            return null;
        }
        Map<String, String> contextMap = getCopyOfContextMap();
        return () -> {
            Map<String, String> oldContext = getCopyOfContextMap();
            try {
                setContextMap(contextMap);
                return callable.call();
            } finally {
                setContextMap(oldContext);
            }
        };
    }

    /**
     * 线程包装：将当前MDC上下文传递到Supplier
     *
     * @param supplier 供应任务
     * @param <T>      返回类型
     * @return 包装后的Supplier
     */
    public static <T> Supplier<T> wrapSupplier(Supplier<T> supplier) {
        if (supplier == null) {
            return null;
        }
        Map<String, String> contextMap = getCopyOfContextMap();
        return () -> {
            Map<String, String> oldContext = getCopyOfContextMap();
            try {
                setContextMap(contextMap);
                return supplier.get();
            } finally {
                setContextMap(oldContext);
            }
        };
    }

    /**
     * 执行任务并确保MDC上下文传递
     *
     * @param runnable 可执行任务
     */
    public static void runWithMdc(Runnable runnable) {
        if (runnable == null) {
            return;
        }
        Map<String, String> contextMap = getCopyOfContextMap();
        Map<String, String> oldContext = getCopyOfContextMap();
        try {
            setContextMap(contextMap);
            runnable.run();
        } finally {
            setContextMap(oldContext);
        }
    }

    /**
     * 执行任务并确保MDC上下文传递（带返回值）
     *
     * @param callable 可调用任务
     * @param <T>      返回类型
     * @return 执行结果
     */
    public static <T> T runWithMdc(Callable<T> callable) throws Exception {
        if (callable == null) {
            return null;
        }
        Map<String, String> contextMap = getCopyOfContextMap();
        Map<String, String> oldContext = getCopyOfContextMap();
        try {
            setContextMap(contextMap);
            return callable.call();
        } finally {
            setContextMap(oldContext);
        }
    }

    /**
     * 获取MDC上下文的字符串表示
     *
     * @return MDC上下文字符串
     */
    public static String getContextString() {
        Map<String, String> contextMap = getCopyOfContextMap();
        if (contextMap == null || contextMap.isEmpty()) {
            return "{}";
        }
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, String> entry : contextMap.entrySet()) {
            if (!first) {
                sb.append(", ");
            }
            sb.append(entry.getKey()).append("=").append(entry.getValue());
            first = false;
        }
        sb.append("}");
        return sb.toString();
    }
}
