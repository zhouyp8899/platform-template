package com.zzl.platform.gw.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

/**
 * GatewayMetrics 单元测试
 */
@ExtendWith(MockitoExtension.class)
class GatewayMetricsTest {

    private MeterRegistry meterRegistry;
    private GatewayMetrics gatewayMetrics;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        gatewayMetrics = new GatewayMetrics(meterRegistry);
    }

    @AfterEach
    void tearDown() {
        if (gatewayMetrics != null) {
            gatewayMetrics.clearCounterCache();
        }
    }

    @Test
    @DisplayName("测试记录请求计数")
    void testRecordRequest() {
        gatewayMetrics.recordRequest("/api/test", "GET");
        gatewayMetrics.recordRequest("/api/test", "GET");
        gatewayMetrics.recordRequest("/api/test", "POST");

        GatewayMetrics.MetricsSummary summary = gatewayMetrics.getSummary();
        assertEquals(3L, summary.totalRequests());
    }

    @Test
    @DisplayName("测试记录成功请求")
    void testRecordSuccess() {
        gatewayMetrics.recordSuccess("/api/test", "GET");
        gatewayMetrics.recordSuccess("/api/test", "GET");

        double count = meterRegistry.get("gateway.requests.success")
                .counters()
                .stream()
                .mapToDouble(io.micrometer.core.instrument.Counter::count)
                .sum();

        assertEquals(2.0, count, 0.001);
    }

    @Test
    @DisplayName("测试记录错误请求")
    void testRecordError() {
        gatewayMetrics.recordError("/api/test", "GET", "INVALID_PARAM");
        gatewayMetrics.recordError("/api/test", "POST", "INVALID_PARAM");

        double count = meterRegistry.get("gateway.requests.error")
                .counters()
                .stream()
                .mapToDouble(io.micrometer.core.instrument.Counter::count)
                .sum();

        assertEquals(2.0, count, 0.001);
    }

    @Test
    @DisplayName("测试记录超时请求")
    void testRecordTimeout() {
        gatewayMetrics.recordTimeout("/api/test", "GET");

        double count = meterRegistry.get("gateway.requests.timeout")
                .counters()
                .stream()
                .mapToDouble(io.micrometer.core.instrument.Counter::count)
                .sum();

        assertEquals(1.0, count, 0.001);
    }

    @Test
    @DisplayName("测试记录鉴权错误")
    void testRecordAuthError() {
        gatewayMetrics.recordAuthError("/api/test", "TOKEN_EXPIRED");

        double count = meterRegistry.get("gateway.requests.auth_error")
                .counters()
                .stream()
                .mapToDouble(io.micrometer.core.instrument.Counter::count)
                .sum();

        assertEquals(1.0, count, 0.001);
    }

    @Test
    @DisplayName("测试记录限流")
    void testRecordRateLimit() {
        gatewayMetrics.recordRateLimit("/api/test", "IP_BASED");

        double count = meterRegistry.get("gateway.requests.rate_limited")
                .counters()
                .stream()
                .mapToDouble(io.micrometer.core.instrument.Counter::count)
                .sum();

        assertEquals(1.0, count, 0.001);
    }

    @Test
    @DisplayName("测试记录响应时间")
    void testRecordResponseTime() {
        gatewayMetrics.recordResponseTime("/api/test", "GET", 100);
        gatewayMetrics.recordResponseTime("/api/test", "GET", 200);
        gatewayMetrics.recordResponseTime("/api/test", "GET", 300);

        GatewayMetrics.MetricsSummary summary = gatewayMetrics.getSummary();
        assertEquals(200.0, summary.avgResponseTime());

        // 验证 Gauge 指标
        double avgTime = meterRegistry.get("gateway.response.time.avg")
                .gauges()
                .stream()
                .mapToDouble(io.micrometer.core.instrument.Gauge::value)
                .findFirst()
                .orElse(0.0);

        assertEquals(200.0, avgTime, 0.001);
    }

    @Test
    @DisplayName("测试最大响应时间更新")
    void testMaxResponseTimeUpdate() {
        gatewayMetrics.recordResponseTime("/api/test", "GET", 100);
        gatewayMetrics.recordResponseTime("/api/test", "GET", 300);
        gatewayMetrics.recordResponseTime("/api/test", "GET", 200);

        double maxTime = meterRegistry.get("gateway.response.time.max")
                .gauges()
                .stream()
                .mapToDouble(io.micrometer.core.instrument.Gauge::value)
                .findFirst()
                .orElse(0.0);

        assertEquals(300.0, maxTime, 0.001);
    }

    @Test
    @DisplayName("测试标签清理功能")
    void testTagSanitization() {
        gatewayMetrics.recordRequest("/api/用户/profile", "GET");
        gatewayMetrics.recordRequest("/api/test#special", "POST");

        // 验证特殊字符被替换
        long count = meterRegistry.get("gateway.requests.total")
                .counters()
                .stream()
                .filter(c -> c.getId().getTags().stream()
                        .anyMatch(t -> "path".equals(t.getKey()) && t.getValue().contains("_")))
                .count();

        assertTrue(count > 0, "Special characters should be sanitized");
    }

    @Test
    @DisplayName("测试标签长度限制")
    void testTagLengthLimitation() {
        String longPath = "/api/" + "a".repeat(100);
        gatewayMetrics.recordRequest(longPath, "GET");

        long count = meterRegistry.get("gateway.requests.total")
                .counters()
                .stream()
                .filter(c -> c.getId().getTags().stream()
                        .anyMatch(t -> "path".equals(t.getKey()) && t.getValue().length() <= 50))
                .count();

        assertTrue(count > 0, "Tags should be limited to 50 characters");
    }

    @Test
    @DisplayName("测试计数器缓存清理")
    void testClearCounterCache() {
        gatewayMetrics.recordRequest("/api/test", "GET");
        gatewayMetrics.recordRequest("/api/test2", "GET");

        // 记录清理前的计数器数量
        long beforeClear = meterRegistry.getMeters().size();

        gatewayMetrics.clearCounterCache();

        // 记录清理后的计数器数量（基础计数器仍存在）
        long afterClear = meterRegistry.getMeters().size();

        assertEquals(beforeClear, afterClear, "Base counters should remain after cache clear");
    }

    @Test
    @DisplayName("测试空路径处理")
    void testNullPathHandling() {
        assertDoesNotThrow(() -> {
            gatewayMetrics.recordRequest(null, "GET");
            gatewayMetrics.recordSuccess(null, "GET");
            gatewayMetrics.recordError(null, "GET", "ERROR");
        });
    }

    @Test
    @DisplayName("测试并发记录请求")
    void testConcurrentRecordRequests() throws InterruptedException {
        int threadCount = 10;
        int requestsPerThread = 1000;
        Thread[] threads = new Thread[threadCount];

        for (int i = 0; i < threadCount; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < requestsPerThread; j++) {
                    gatewayMetrics.recordRequest("/api/test", "GET");
                }
            });
            threads[i].start();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        GatewayMetrics.MetricsSummary summary = gatewayMetrics.getSummary();
        assertEquals((long) threadCount * (long) requestsPerThread, summary.totalRequests());
    }
}
