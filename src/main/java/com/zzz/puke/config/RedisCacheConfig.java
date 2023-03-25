package com.zzz.puke.config;

import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

@Configuration
public class RedisCacheConfig {


    @Bean
    public RedisTemplate<String, Object> redisTemplate(LettuceConnectionFactory factory) {
        // 我们为了自己开发方便，一般直接使用 <String, Object>
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        // key采用String的序列化方式
        template.setKeySerializer(keySerializer());
        // hash的key也采用String的序列化方式
        template.setHashKeySerializer(keySerializer());
        // value序列化方式采用jackson
        template.setValueSerializer(valueSerializer());
        // hash的value序列化方式采用jackson
        template.setHashValueSerializer(valueSerializer());
        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public CacheManager redisCacheManager(LettuceConnectionFactory connectionFactory) {
        // Redis序列化上下文。序列化键值对
        RedisSerializationContext.SerializationPair<String> key = RedisSerializationContext.SerializationPair.fromSerializer(keySerializer());
        RedisSerializationContext.SerializationPair<Object> value = RedisSerializationContext.SerializationPair.fromSerializer(valueSerializer());

        // 生成默认Redis配置: 未指定自定义缓存块时, 其他所有缓存数据都以该配置为生成依据
        RedisCacheConfiguration defaultRedisCacheConfiguration = getRedisCacheConfiguration(Duration.ofSeconds(60), key, value);// 默认缓存时间永久,禁用缓存空值

        // 生成自定义缓存块,redis配置(这个取决于项目中是否有需要在使用@Cacheable缓存数据时,希望有不同的过期时间再决定是否使用)
        Map<String, RedisCacheConfiguration> customCacheBlockConfig = new LinkedHashMap<>();
        customCacheBlockConfig.put("detail", getRedisCacheConfiguration(Duration.ofDays(1), key, value)); // 详情缓存时间
        customCacheBlockConfig.put("voiceList", getRedisCacheConfiguration(Duration.ofDays(1), key, value)); // 详情缓存时间
        // 生成 CacheManager
        return RedisCacheManager.builder(RedisCacheWriter.nonLockingRedisCacheWriter(connectionFactory)) // 非锁定Redis缓存编写器
                .cacheDefaults(defaultRedisCacheConfiguration) // 缓存默认值配置
                .withInitialCacheConfigurations(customCacheBlockConfig) // 自定义缓存默认值配置
                .build(); // 建立
    }

    /**
     * 生成Redis缓存配置
     */
    private RedisCacheConfiguration getRedisCacheConfiguration(Duration expirationTime, RedisSerializationContext.SerializationPair<String> key, RedisSerializationContext.SerializationPair<?> value) {
        RedisCacheConfiguration redisCacheConfiguration = RedisCacheConfiguration.defaultCacheConfig().entryTtl(expirationTime) // 默认缓存时间永久
                .serializeKeysWith(key) // 默认序列化键方式
                .serializeValuesWith(value) // 默认序列化值方式
                .computePrefixWith(name -> name + ":") //变双冒号为单冒号
                .disableCachingNullValues(); // 禁用缓存空值
        return redisCacheConfiguration;
    }

    /**
     * 序列化键
     */
    private RedisSerializer<String> keySerializer() {
        return new StringRedisSerializer();
    }

    /**
     * 序列化值
     */
    private RedisSerializer<Object> valueSerializer() {
        return new Jackson2JsonRedisSerializer(Object.class);
    }

}
