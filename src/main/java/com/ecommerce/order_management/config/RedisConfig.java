package com.ecommerce.order_management.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

@Configuration
public class RedisConfig {

  // Configure Redis cache with JSON serialization
  @Bean
  public RedisCacheManager cacheManager(
          ObjectMapper objectMapper,
          RedisConnectionFactory connectionFactory
  ) {
    GenericJackson2JsonRedisSerializer serializer =
            new GenericJackson2JsonRedisSerializer(objectMapper);

    // Store cached values as JSON
    RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
            .serializeValuesWith(
                    RedisSerializationContext.SerializationPair.fromSerializer(serializer)
            );

    return RedisCacheManager.builder()
            .cacheWriter(
                    RedisCacheWriter.nonLockingRedisCacheWriter(connectionFactory)
            )
            .cacheDefaults(config)
            .build();
  }
}