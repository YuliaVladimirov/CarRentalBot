package org.example.carrentalbot.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericToStringSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {

        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        template.setKeySerializer(new StringRedisSerializer());// for Redis keys
        template.setHashKeySerializer(new StringRedisSerializer());// for redisTemplate.opsForHash() for keys
        template.setHashValueSerializer(new GenericToStringSerializer<>(Object.class));// for redisTemplate.opsForHash() for values
        template.setValueSerializer(new GenericToStringSerializer<>(Object.class));// for redisTemplate.opsForValue()

        template.afterPropertiesSet();
        return template;
    }
}
