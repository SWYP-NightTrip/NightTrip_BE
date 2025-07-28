package com.nighttrip.core.global.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer()); // 키 직렬화
        template.setValueSerializer(new StringRedisSerializer()); // 값 직렬화
        template.setHashKeySerializer(new StringRedisSerializer()); // 해시 키 직렬화 (옵션)
        template.setHashValueSerializer(new StringRedisSerializer()); // 해시 값 직렬화 (옵션)
        return template;
    }


}