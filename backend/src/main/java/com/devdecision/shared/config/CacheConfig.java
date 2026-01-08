package com.devdecision.shared.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Redis cache configuration with custom TTL settings for different cache regions.
 * Implements cache-aside pattern with appropriate eviction policies.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    @Value("${devdecision.cache.technology-ttl:3600}")
    private long technologyTtl;

    @Value("${devdecision.cache.comparison-ttl:1800}")
    private long comparisonTtl;

    /**
     * Configure Redis cache manager with custom TTL settings for different cache regions
     */
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        // Default cache configuration
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofSeconds(600)) // 10 minutes default
                .serializeKeysWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new GenericJackson2JsonRedisSerializer()))
                .disableCachingNullValues();

        // Cache-specific configurations with custom TTL
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        
        // Technology data cache - 1 hour TTL
        cacheConfigurations.put("technologies", defaultConfig
                .entryTtl(Duration.ofSeconds(technologyTtl)));
        
        // Technology search results cache - 30 minutes TTL
        cacheConfigurations.put("technology-search", defaultConfig
                .entryTtl(Duration.ofSeconds(1800)));
        
        // Criteria cache - 2 hours TTL (changes less frequently)
        cacheConfigurations.put("criteria", defaultConfig
                .entryTtl(Duration.ofSeconds(7200)));
        
        // Comparison results cache - 30 minutes TTL
        cacheConfigurations.put("comparisons", defaultConfig
                .entryTtl(Duration.ofSeconds(comparisonTtl)));
        
        // Categories and tags cache - 1 hour TTL
        cacheConfigurations.put("categories", defaultConfig
                .entryTtl(Duration.ofSeconds(3600)));
        
        cacheConfigurations.put("tags", defaultConfig
                .entryTtl(Duration.ofSeconds(3600)));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();
    }
}