package com.zzl.platform.auth.controller;

import com.zzl.platform.common.core.res.Result;
import com.zzl.platform.common.core.util.MdcUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * MDC链路追踪测试控制器
 * 用于演示完整的链路追踪功能，包括HTTP请求、异步任务、服务间调用等场景
 */
@RestController
@RequestMapping("/api/mdc-test")
public class MdcTestController {

    private static final Logger logger = LoggerFactory.getLogger(MdcTestController.class);
    private final ExecutorService executorService = Executors.newFixedThreadPool(4);
    @Autowired
    private RestTemplate restTemplate;

    /**
     * 基础测试：验证MDC上下文是否正常工作
     */
    @GetMapping("/basic")
    public Result<Map<String, Object>> basicTest() {
        logger.info("=== 基础MDC测试 ===");

        Map<String, Object> result = new HashMap<>();
        result.put("traceId", MdcUtils.getTraceId());
        result.put("userId", MdcUtils.get("userId", "N/A"));
        result.put("clientIp", MdcUtils.get("clientIp", "N/A"));
        result.put("requestUri", MdcUtils.get("requestUri", "N/A"));
        result.put("requestMethod", MdcUtils.get("requestMethod", "N/A"));

        logger.info("MDC上下文信息: {}", result);

        return Result.success(result);
    }

    /**
     * 测试异步任务：验证MDC上下文是否正确传递到异步线程
     */
    @GetMapping("/async")
    public Result<Map<String, Object>> asyncTest() {
        logger.info("=== 异步MDC测试 ===");

        String mainTraceId = MdcUtils.getTraceId();
        logger.info("主线程traceId: {}", mainTraceId);

        Map<String, Object> result = new HashMap<>();
        result.put("mainTraceId", mainTraceId);

        // 创建多个异步任务
        CompletableFuture<String> task1 = CompletableFuture.supplyAsync(
                MdcUtils.wrapSupplier(() -> {
                    String asyncTraceId = MdcUtils.getTraceId();
                    logger.info("异步任务1 traceId: {}", asyncTraceId);
                    return asyncTraceId;
                }), executorService);

        CompletableFuture<String> task2 = CompletableFuture.supplyAsync(
                MdcUtils.wrapSupplier(() -> {
                    String asyncTraceId = MdcUtils.getTraceId();
                    logger.info("异步任务2 traceId: {}", asyncTraceId);
                    return asyncTraceId;
                }), executorService);

        CompletableFuture<String> task3 = CompletableFuture.supplyAsync(
                MdcUtils.wrapSupplier(() -> {
                    String asyncTraceId = MdcUtils.getTraceId();
                    logger.info("异步任务3 traceId: {}", asyncTraceId);
                    return asyncTraceId;
                }), executorService);

        // 等待所有任务完成
        CompletableFuture.allOf(task1, task2, task3).join();

        try {
            result.put("asyncTask1TraceId", task1.get());
            result.put("asyncTask2TraceId", task2.get());
            result.put("asyncTask3TraceId", task3.get());
        } catch (Exception e) {
            logger.error("异步任务执行失败", e);
            return Result.fail("异步任务执行失败: " + e.getMessage());
        }

        logger.info("所有异步任务完成，主线程traceId: {}", MdcUtils.getTraceId());

        return Result.success(result);
    }

    /**
     * 测试嵌套调用：验证多级调用的追踪ID传递
     */
    @GetMapping("/nested")
    public Result<Map<String, Object>> nestedTest() {
        logger.info("=== 嵌套调用MDC测试 ===");

        String level1TraceId = MdcUtils.getTraceId();
        logger.info("第一级调用 traceId: {}", level1TraceId);

        Map<String, Object> result = new HashMap<>();
        result.put("level1TraceId", level1TraceId);

        // 第二级调用
        String level2TraceId = secondLevelCall();
        result.put("level2TraceId", level2TraceId);

        // 恢复第一级追踪ID
        logger.info("返回第一级调用 traceId: {}", MdcUtils.getTraceId());

        return Result.success(result);
    }

    /**
     * 第二级调用
     */
    private String secondLevelCall() {
        String currentTraceId = MdcUtils.getTraceId();
        String parentTraceId = MdcUtils.get("parentTraceId", null);

        logger.info("第二级调用 - 当前traceId: {}, 父级traceId: {}",
                currentTraceId, parentTraceId);

        // 创建子追踪ID
        String childTraceId = MdcUtils.createChildTraceId(currentTraceId);

        logger.info("第二级调用 - 子traceId: {}", childTraceId);

        // 第三级调用
        String level3TraceId = thirdLevelCall(childTraceId);
        logger.info("第三级调用返回 traceId: {}", level3TraceId);

        // 恢复父级追踪ID
        MdcUtils.restoreParentTraceId();
        logger.info("恢复父级traceId: {}", MdcUtils.getTraceId());

        return childTraceId;
    }

    /**
     * 第三级调用
     */
    private String thirdLevelCall(String parentTraceId) {
        String currentTraceId = MdcUtils.getTraceId();
        logger.info("第三级调用 - traceId: {}, 父级traceId: {}",
                currentTraceId, MdcUtils.get("parentTraceId", null));

        // 创建子追踪ID
        String childTraceId = MdcUtils.createChildTraceId(currentTraceId);
        logger.info("第三级调用 - 子traceId: {}", childTraceId);

        // 恢复父级追踪ID
        MdcUtils.restoreParentTraceId();

        return childTraceId;
    }

    /**
     * 测试错误处理：验证异常情况下MDC上下文是否正确清理
     */
    @GetMapping("/error")
    public Result<Map<String, Object>> errorTest() {
        logger.info("=== 错误处理MDC测试 ===");

        String traceId = MdcUtils.getTraceId();
        logger.info("发生错误前的traceId: {}", traceId);

        try {
            // 模拟错误
            throw new RuntimeException("测试异常");
        } catch (Exception e) {
            logger.error("捕获异常: {}", e.getMessage(), e);
            Map<String, Object> result = new HashMap<>();
            result.put("traceId", MdcUtils.getTraceId());
            result.put("errorMessage", e.getMessage());
            return Result.success("错误处理完成", result);
        }
    }

    /**
     * 测试服务间调用：验证MDC上下文是否通过RestTemplate传递
     */
    @GetMapping("/service-call")
    public Result<Map<String, Object>> serviceCallTest(
            @RequestParam(defaultValue = "http://localhost:8080") String targetUrl) {
        logger.info("=== 服务间调用MDC测试 ===");

        String currentTraceId = MdcUtils.getTraceId();
        logger.info("当前服务traceId: {}", currentTraceId);

        try {
            // 调用其他服务（这里调用自身进行演示）
            String url = targetUrl + "/api/mdc-test/basic";
            logger.info("调用下游服务: {}", url);

            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

            logger.info("下游服务响应: {}", response);

            Map<String, Object> result = new HashMap<>();
            result.put("upstreamTraceId", currentTraceId);
            result.put("downstreamResponse", response);

            return Result.success(result);

        } catch (Exception e) {
            logger.error("服务间调用失败", e);
            Map<String, Object> result = new HashMap<>();
            result.put("upstreamTraceId", currentTraceId);
            result.put("errorMessage", e.getMessage());
            return Result.success("服务间调用失败", result);
        }
    }

    /**
     * 测试自定义MDC字段：设置自定义业务信息
     */
    @GetMapping("/custom")
    public Result<Map<String, Object>> customFieldsTest() {
        logger.info("=== 自定义MDC字段测试 ===");

        // 设置自定义字段
        MdcUtils.setBusinessType("MDC_TEST");
        MdcUtils.setTenantId("tenant-001");

        String traceId = MdcUtils.getTraceId();
        String businessType = MdcUtils.get("businessType", "N/A");
        String tenantId = MdcUtils.get("tenantId", "N/A");

        logger.info("自定义MDC字段 - trace: {}, businessType: {}, tenantId: {}",
                traceId, businessType, tenantId);

        Map<String, Object> result = new HashMap<>();
        result.put("traceId", traceId);
        result.put("businessType", businessType);
        result.put("tenantId", tenantId);

        return Result.success(result);
    }
}
