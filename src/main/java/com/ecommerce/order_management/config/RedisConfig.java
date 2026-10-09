package com.ecommerce.order_management.config;

import com.ecommerce.order_management.entity.Product;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

@Configuration
public class RedisConfig {

    // Configure Redis cache for Product
    @Bean
    public RedisCacheManager cacheManager(
            ObjectMapper objectMapper,
            RedisConnectionFactory connectionFactory
    ) {
        // Serialize Product as JSON
        Jackson2JsonRedisSerializer<Product> serializer =
                new Jackson2JsonRedisSerializer<>(objectMapper, Product.class);

        // Configure JSON value serialization
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(serializer)
                );

        // Create Redis cache manager
        return RedisCacheManager.builder()
                .cacheWriter(
                        RedisCacheWriter.nonLockingRedisCacheWriter(connectionFactory)
                )
                .cacheDefaults(config)
                .build();
    }
}