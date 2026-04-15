package com.zzl.platform.common.redis.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

/**
 * Redis自动配置类
 * 提供RedisTemplate、StringRedisTemplate等核心Bean配置
 */
@Slf4j
@Configuration
@EnableCaching
@EnableConfigurationProperties(com.zzl.platform.common.redis.properties.RedisProperties.class)
@ConditionalOnProperty(prefix = "platform.redis", name = "enabled", havingValue = "true", matchIfMissing = true)
public class RedisConfig {

    /**
     * 配置RedisTemplate，使用JSON序列化器
     * 支持Java 8时间类型、泛型对象等复杂类型
     */
    @Bean
    @ConditionalOnMissingBean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        log.info("Initializing RedisTemplate with JSON serializer");
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // 使用String序列化器作为Key的序列化器
        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringRedisSerializer);
        template.setHashKeySerializer(stringRedisSerializer);

        // 使用Jackson JSON序列化器作为Value的序列化器
        GenericJackson2JsonRedisSerializer jsonRedisSerializer = createJsonSerializer();
        template.setValueSerializer(jsonRedisSerializer);
        template.setHashValueSerializer(jsonRedisSerializer);

        template.afterPropertiesSet();
        return template;
    }

    /**
     * 配置StringRedisTemplate
     * 用于字符串类型的Redis操作
     */
    @Bean
    @ConditionalOnMissingBean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory connectionFactory) {
        log.info("Initializing StringRedisTemplate");
        return new StringRedisTemplate(connectionFactory);
    }

    /**
     * 创建支持Java 8时间类型的JSON序列化器
     */
    private GenericJackson2JsonRedisSerializer createJsonSerializer() {
        ObjectMapper objectMapper = new ObjectMapper();
        // 注册Java时间模块，支持LocalDate、LocalDateTime等
        objectMapper.registerModule(new JavaTimeModule());
        // 启用默认类型信息，支持反序列化时保留类型信息
        objectMapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL
        );
        return new GenericJackson2JsonRedisSerializer(objectMapper);
    }

    /**
     * 配置Spring Cache默认缓存配置
     */
    @Bean
    public RedisCacheConfiguration redisCacheConfiguration() {
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofHours(1)) // 默认缓存1小时
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(createJsonSerializer()))
                .disableCachingNullValues(); // 不缓存null值
    }
}
