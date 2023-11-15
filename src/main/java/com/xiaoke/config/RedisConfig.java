package com.xiaoke.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;

/**
 * 默认是采用JDK 序列化；缺点：可读性差，内存占用较大。
 * 自定义SpringDataRedis的序列化
 */
@Configuration
public class RedisConfig{
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        //1.创建
        RedisTemplate<String,Object> redisTemplate=new RedisTemplate<>();
        //设置连接工厂
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        //json序列化工具
        //GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer();
        //设置key和hashKey的采用String序列化
        redisTemplate.setKeySerializer(RedisSerializer.string());
        redisTemplate.setHashKeySerializer(RedisSerializer.string());
        //设置Value和hashValue采用Json序列化
        //redisTemplate.setValueSerializer(serializer);
        //redisTemplate.setHashKeySerializer(serializer);
        return redisTemplate;
    }
}