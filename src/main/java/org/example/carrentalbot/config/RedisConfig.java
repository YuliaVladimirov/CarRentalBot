package org.example.carrentalbot.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericToStringSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis configuration class for customizing Redis serialization strategy.
 * <p>Defines a {@link RedisTemplate} bean with explicit serializers for
 * keys and values to ensure consistent storage and retrieval of data
 * from Redis.</p>
 * <p>This configuration supports:
 * <ul>
 *     <li>String serialization for keys and hash keys</li>
 *     <li>Generic string-based serialization for values and hash values</li>
 * </ul>
 * </p>
 */
@Configuration
public class RedisConfig {

    /**
     * Creates and configures a {@link RedisTemplate} for Redis operations.
     * <p>Configures serialization rules:
     * <ul>
     *     <li>StringRedisSerializer for keys and hash keys</li>
     *     <li>GenericToStringSerializer for values and hash values</li>
     * </ul>
     * </p>
     *
     * @param connectionFactory Redis connection factory used to establish connections
     * @return configured RedisTemplate instance
     */
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
