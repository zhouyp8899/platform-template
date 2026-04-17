package com.zzl.platform.common.core.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * MDC链路追踪使用示例
 * 演示各种场景下的MDC使用方法
 */
public class MdcExample {

    private static final Logger logger = LoggerFactory.getLogger(MdcExample.class);
    private static final ExecutorService executorService = Executors.newFixedThreadPool(4);

    /**
     * 关闭线程池
     */
    public static void shutdown() {
        executorService.shutdown();
    }

    /**
     * 示例1: 基�使用 - 手动初始化和清理
     */
    public void basicExample() {
        try {
            // 初始化MDC上下文
            String traceId = MdcUtils.init();
            logger.info("基础示例 - 追踪ID: {}", traceId);

            // 设置用户信息
            MdcUtils.setUser("user-001", "admin");

            // 设置业务类型
            MdcUtils.setBusinessType("BASIC_EXAMPLE");

            logger.info("执行业务逻辑...");

        } finally {
            // 清理MDC上下文（重要！）
            MdcUtils.clear();
        }
    }

    /**
     * 示例2: 跨服务调用 - 创建子追踪ID
     */
    public void crossServiceExample() {
        try {
            // 初始化主追踪ID
            String parentTraceId = MdcUtils.init();
            logger.info("父级服务 - 追踪ID: {}", parentTraceId);

            // 调用子服务，创建子追踪ID
            callChildService(parentTraceId);

            logger.info("父级服务继续执行...");

        } finally {
            MdcUtils.clear();
        }
    }

    /**
     * 调用子服务
     */
    private void callChildService(String parentTraceId) {
        try {
            // 创建子追踪ID
            String childTraceId = MdcUtils.createChildTraceId(parentTraceId);
            logger.info("子级服务 - 追踪ID: {}, 矶级追踪ID: {}",
                    childTraceId, parentTraceId);

            // 执行子服务逻辑...

        } finally {
            MdcUtils.clear();
        }
    }

    /**
     * 示例3: 异步任务 - 使用MdcUtils.wrap()
     */
    public void asyncTaskExample() {
        try {
            MdcUtils.init();
            logger.info("主线程 - 开始执行异步任务");

            List<CompletableFuture<Void>> futures = new ArrayList<>();

            for (int i = 0; i < 5; i++) {
                final int taskId = i;

                // 使用MdcUtils.wrap()包装异步任务
                Runnable task = MdcUtils.wrap(() -> {
                    logger.info("异步任务 - taskId={}", taskId);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    logger.info("异步任务完成 - taskId={}", taskId);
                });

                CompletableFuture<Void> future = CompletableFuture.runAsync(task, executorService);
                futures.add(future);
            }

            // 等待所有异步任务完成
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

            logger.info("所有异步任务完成");

        } catch (Exception e) {
            logger.error("异步任务执行异常", e);
        } finally {
            MdcUtils.clear();
        }
    }

    /**
     * 示例4: 使用runWithMdc()方法
     */
    public void runWithMdcExample() {
        MdcUtils.init();

        try {
            logger.info("主线程执行");

            // 使用runWithMdc()确保MDC上下文传递
            MdcUtils.runWithMdc(() -> {
                logger.info("嵌套执行1");
            });

            MdcUtils.runWithMdc(() -> {
                logger.info("嵌套执行2");
            });

            // 带返回值的示例
            String result = MdcUtils.runWithMdc(() -> {
                logger.info("执行带返回值的任务");
                return "success";
            });

            logger.info("任务执行结果: {}", result);

        } catch (Exception e) {
            logger.error("任务执行异常", e);
        } finally {
            MdcUtils.clear();
        }
    }

    /**
     * 示例5: 恢复父级追踪ID
     */
    public void restoreParentExample() {
        try {
            MdcUtils.init();
            String originalTraceId = MdcUtils.getTraceId();
            logger.info("原始追踪ID: {}", originalTraceId);

            // 调用服务，创建子追踪ID
            String childTraceId = MdcUtils.createChildTraceId(originalTraceId);
            logger.info("子追踪ID: {}", childTraceId);

            // 恢复父级追踪ID
            MdcUtils.restoreParentTraceId();
            logger.info("恢复后的追踪ID: {}", MdcUtils.getTraceId());

        } finally {
            MdcUtils.clear();
        }
    }

    /**
     * 示例6: 设置完整的上下文信息
     */
    public void fullContextExample() {
        try {
            MdcUtils.init();
            logger.info("初始化上下文");

            // 设置应用信息
            MdcUtils.setAppName("platform-example");
            MdcUtils.setEnv("dev");

            // 设置用户信息
            MdcUtils.setUser("user-002", "test-user");

            // 设置租户信息
            MdcUtils.setTenantId("tenant-001");

            // 设置客户端信息
            MdcUtils.setClientIp("192.168.1.100");

            // 设置请求信息
            MdcUtils.setRequest("/api/example", "GET");

            // 设置业务类型
            MdcUtils.setBusinessType("FULL_CONTEXT");

            logger.info("完整上下文信息: {}", MdcUtils.getContextString());

        } finally {
            MdcUtils.clear();
        }
    }

    /**
     * 示例7: 错误处理场景
     */
    public void errorHandlingExample() {
        try {
            MdcUtils.init();
            logger.info("开始执行任务");

            try {
                // 模拟错误
                throw new RuntimeException("业务处理失败");
            } catch (Exception e) {
                // 错误日志会自动包含MDC信息
                logger.error("任务执行失败", e);
                throw e;
            }

        } catch (Exception e) {
            logger.error("捕获异常", e);
        } finally {
            MdcUtils.clear();
        }
    }

    /**
     * 示例8: 多级调用链
     */
    public void multiLevelCallChain() {
        try {
            MdcUtils.init();
            logger.info("第一级调用");

            secondLevelCall();

            logger.info("第一级调用完成");

        } finally {
            MdcUtils.clear();
        }
    }

    private void secondLevelCall() {
        try {
            String currentTraceId = MdcUtils.getTraceId();
            MdcUtils.createChildTraceId(currentTraceId);
            logger.info("第二级调用");

            thirdLevelCall();

            logger.info("第二级调用完成");

        } finally {
            MdcUtils.restoreParentTraceId();
        }
    }

    private void thirdLevelCall() {
        try {
            String currentTraceId = MdcUtils.getTraceId();
            MdcUtils.createChildTraceId(currentTraceId);
            logger.info("第三级调用");
            logger.info("第三级调用完成");

        } finally {
            MdcUtils.restoreParentTraceId();
        }
    }
}
