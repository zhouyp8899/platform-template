package com.zzl.platform.common.redis.utils;

import java.text.MessageFormat;

/**
 * Redis Key构建器
 * 提供统一的Key命名规范和构建方式
 */
public class RedisKeyBuilder {

    /**
     * 构建Redis Key
     *
     * @param prefix 前缀
     * @param parts  Key的各个组成部分
     * @return 完整的Redis Key
     */
    public static String build(String prefix, Object... parts) {
        if (parts == null || parts.length == 0) {
            return prefix;
        }
        String[] strParts = new String[parts.length];
        for (int i = 0; i < parts.length; i++) {
            strParts[i] = parts[i] != null ? parts[i].toString() : "null";
        }
        return MessageFormat.format(prefix, (Object[]) strParts).replace("'", "");
    }

    /**
     * 构建带命名空间的Key
     *
     * @param namespace 命名空间
     * @param key       Key
     * @return 完整的Redis Key
     */
    public static String withNamespace(String namespace, String key) {
        return namespace + ":" + key;
    }

    /**
     * 常用Key前缀定义
     */
    public static class Prefix {
        /**
         * 缓存前缀
         */
        public static final String CACHE = "cache:{0}:{1}";

        /**
         * 分布式锁前缀
         */
        public static final String LOCK = "lock:{0}";

        /**
         * 限流前缀
         */
        public static final String RATE_LIMIT = "rate:{0}:{1}";

        /**
         * 用户Session前缀
         */
        public static final String USER_SESSION = "session:user:{0}";

        /**
         * 接口幂等性Key前缀
         */
        public static final String IDEMPOTENT = "idempotent:{0}";

        /**
         * 热点数据前缀
         */
        public static final String HOT_DATA = "hot:{0}";

        /**
         * 倒计时前缀
         */
        public static final String COUNTDOWN = "countdown:{0}";

        /**
         * 计数器前缀
         */
        public static final String COUNTER = "counter:{0}";

        /**
         * 消息队列前缀
         */
        public static final String QUEUE = "queue:{0}";

        /**
         * 集合前缀
         */
        public static final String SET = "set:{0}";

        /**
         * 排行榜前缀
         */
        public static final String RANK = "rank:{0}";
    }

    /**
     * OTA平台业务相关Key前缀
     */
    public static class OtaPrefix {
        /**
         * 酒店详情缓存
         */
        public static final String HOTEL_DETAIL = "cache:hotel:detail:{0}";

        /**
         * 房态信息缓存
         */
        public static final String ROOM_STATUS = "cache:room:status:{0}:{1}";

        /**
         * 价格策略缓存
         */
        public static final String PRICE_STRATEGY = "cache:price:strategy:{0}:{1}:{2}";

        /**
         * 订单信息缓存
         */
        public static final String ORDER_INFO = "cache:order:info:{0}";

        /**
         * 库存缓存
         */
        public static final String INVENTORY = "cache:inventory:{0}";

        /**
         * 会员信息缓存
         */
        public static final String MEMBER_INFO = "cache:member:info:{0}";

        /**
         * 渠道配置缓存
         */
        public static final String CHANNEL_CONFIG = "cache:channel:config:{0}";
    }
}
