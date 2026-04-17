package com.zzl.platform.common.core.util;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 分布式链路追踪ID生成器
 * 基于雪花算法思想优化，保证全局唯一性
 */
public class TraceIdGenerator {

    /**
     * 机器ID标识 (0-31)
     */
    private static final long MACHINE_ID = getMachineId();

    /**
     * 序列号计数器
     */
    private static final AtomicLong SEQUENCE = new AtomicLong(0);

    /**
     * 时间戳掩码 - 获取当前毫秒时间戳
     */
    private static final long TIMESTAMP_MASK = 0xFFFFFFFFFFFFL; // 48位时间戳

    /**
     * 序列号掩码 - 支持4096个序列号
     */
    private static final long SEQUENCE_MASK = 0xFFFL; // 12位序列号

    /**
     * 机器ID掩码 - 支持32台机器
     */
    private static final long MACHINE_MASK = 0x1FL; // 5位机器ID

    /**
     * 序列号位移
     */
    private static final int SEQUENCE_SHIFT = 5;

    /**
     * 时间戳位移
     */
    private static final int TIMESTAMP_SHIFT = 17;

    /**
     * 随机数生成器 - 用于生成UUID类型追踪ID
     */
    private static final SecureRandom RANDOM = new SecureRandom();

    private TraceIdGenerator() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * 获取机器ID
     */
    private static long getMachineId() {
        try {
            String hostname = java.net.InetAddress.getLocalHost().getHostName();
            return Math.abs(hostname.hashCode()) % 32;
        } catch (Exception e) {
            return 1; // 默认机器ID
        }
    }

    /**
     * 生成Snowflake风格追踪ID
     * 格式: 时间戳(48位) + 序列号(12位) + 机器ID(5位)
     *
     * @return 追踪ID
     */
    public static String generateTraceId() {
        long now = Instant.now().toEpochMilli();
        long seq = SEQUENCE.getAndIncrement() & SEQUENCE_MASK;

        long timestamp = now & TIMESTAMP_MASK;
        long machine = MACHINE_ID & MACHINE_MASK;

        long traceId = (timestamp << TIMESTAMP_SHIFT)
                | (seq << SEQUENCE_SHIFT)
                | machine;

        return Long.toHexString(traceId);
    }

    /**
     * 生成UUID风格追踪ID (Base64编码，去除填充字符)
     *
     * @return 追踪ID
     */
    public static String generateUUIDTraceId() {
        byte[] randomBytes = new byte[16];
        RANDOM.nextBytes(randomBytes);
        String base64 = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(randomBytes);
        return base64.substring(0, 22); // 取前22个字符，保证URL安全
    }

    /**
     * 生成时间戳+随机数风格追踪ID
     * 格式: {timestamp}-{random}
     *
     * @return 追踪ID
     */
    public static String generateTimestampTraceId() {
        long timestamp = System.currentTimeMillis();
        long random = RANDOM.nextInt(1000000);
        return timestamp + "-" + random;
    }

    /**
     * 默认生成追踪ID方法
     *
     * @return 追踪ID
     */
    public static String generate() {
        return generateTraceId();
    }

    /**
     * 验证追踪ID是否有效
     *
     * @param traceId 追踪ID
     * @return 是否有效
     */
    public static boolean isValid(String traceId) {
        if (traceId == null || traceId.isEmpty()) {
            return false;
        }
        return !traceId.equals("N/A");
    }
}
