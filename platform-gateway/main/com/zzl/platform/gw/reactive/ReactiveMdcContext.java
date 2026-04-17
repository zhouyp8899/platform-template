package com.zzl.platform.gw.reactive;

import com.zzl.platform.common.core.constant.MdcConstants;
import com.zzl.platform.common.core.util.MdcUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.Map;

/**
 * 响应式MDC上下文管理器
 * 用于Gateway基于Reactor的非阻塞场景，确保MDC上下文正确传递
 */
public class ReactiveMdcContext {

    private static final Logger logger = LoggerFactory.getLogger(ReactiveMdcContext.class);

    /**
     * 请求上下文属性键
     */
    private static final String MDC_CONTEXT_KEY = "REACTIVE_MDC_CONTEXT";

    private ReactiveMdcContext() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * 初始化响应式MDC上下文
     * 生成追踪ID并存储到请求属性中
     *
     * @param traceId 追踪ID（可为null）
     * @return 追踪ID
     */
    public static String init(String traceId) {
        if (traceId == null || traceId.isEmpty()) {
            traceId = MdcUtils.init();
        } else {
            MdcUtils.init(traceId);
        }

        // 存储MDC上下文到ThreadLocal（临时，用于日志）
        Map<String, String> contextMap = MDC.getCopyOfContextMap();

        // 记录日志
        logger.debug("初始化响应式MDC上下文: traceId={}", traceId);

        return traceId;
    }

    /**
     * 保存MDC上下文到Map（用于跨线程传递）
     *
     * @return MDC上下文副本
     */
    public static Map<String, String> capture() {
        Map<String, String> contextMap = MDC.getCopyOfContextMap();
        logger.debug("捕获MDC上下文: size={}", contextMap != null ? contextMap.size() : 0);
        return contextMap;
    }

    /**
     * 恢复MDC上下文
     *
     * @param contextMap MDC上下文
     */
    public static void restore(Map<String, String> contextMap) {
        Map<String, String> oldContext = MDC.getCopyOfContextMap();
        try {
            if (contextMap != null && !contextMap.isEmpty()) {
                MDC.setContextMap(contextMap);
                logger.debug("恢复MDC上下文: size={}", contextMap.size());
            }
        } catch (Exception e) {
            logger.error("恢复MDC上下文失败", e);
        }
    }

    /**
     * 清理MDC上下文
     */
    public static void clear() {
        logger.debug("清理MDC上下文");
        MDC.clear();
    }

    /**
     * 设置用户信息
     *
     * @param userId   用户ID
     * @param username 用户名
     */
    public static void setUser(String userId, String username) {
        MdcUtils.setUser(userId, username);
    }

    /**
     * 设置租户ID
     *
     * @param tenantId 租户ID
     */
    public static void setTenantId(String tenantId) {
        MdcUtils.setTenantId(tenantId);
    }

    /**
     * 设置客户端IP
     *
     * @param clientIp 客户端IP
     */
    public static void setClientIp(String clientIp) {
        MdcUtils.setClientIp(clientIp);
    }

    /**
     * 设置请求信息
     *
     * @param uri    请求URI
     * @param method 请求方法
     */
    public static void setRequest(String uri, String method) {
        MdcUtils.setRequest(uri, method);
    }

    /**
     * 创建包装器，用于确保MDC上下文在Reactor流中传递
     *
     * @param runnable 可执行任务
     * @return 包装后的Runnable
     */
    public static Runnable wrap(Runnable runnable) {
        Map<String, String> contextMap = capture();
        return () -> {
            Map<String, String> oldContext = MDC.getCopyOfContextMap();
            try {
                restore(contextMap);
                runnable.run();
            } finally {
                if (oldContext != null && !oldContext.isEmpty()) {
                    MDC.setContextMap(oldContext);
                } else {
                    clear();
                }
            }
        };
    }

    /**
     * 获取当前追踪ID
     *
     * @return 追踪ID
     */
    public static String getTraceId() {
        return MdcUtils.getTraceId();
    }

    /**
     * 创建子追踪ID
     *
     * @param parentTraceId 父级追踪ID
     * @return 子追踪ID
     */
    public static String createChildTraceId(String parentTraceId) {
        if (parentTraceId != null && !parentTraceId.isEmpty()) {
            MdcUtils.put(MdcConstants.PARENT_TRACE_ID, parentTraceId);
        }
        String childTraceId = MdcUtils.init();
        logger.debug("创建子追踪ID: parentTraceId={}, childTraceId={}", parentTraceId, childTraceId);
        return childTraceId;
    }

    /**
     * 获取MDC上下文的字符串表示
     *
     * @return MDC上下文字符串
     */
    public String toString() {
        return MdcUtils.getContextString();
    }
}
