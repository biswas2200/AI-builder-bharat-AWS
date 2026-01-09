package com.devdecision.shared.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

/**
 * Test cache configuration using simple in-memory caching
 * instead of Redis for test environments.
 */
@Configuration
@EnableCaching
@Profile("test")
public class TestCacheConfig {

    /**
     * Simple in-memory cache manager for tests
     */
    @Bean
    @Primary
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager(
            "technologies",
            "technology-search", 
            "criteria",
            "comparisons",
            "categories",
            "tags",
            "insights",
            "personalizedInsights",
            "tradeOffAnalysis",
            "decisionQuestions"
        );
    }
}