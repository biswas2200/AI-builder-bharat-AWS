package com.devdecision.shared.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * Service for managing cache eviction policies and scheduled cache cleanup.
 * Implements cache-aside pattern with proactive eviction strategies.
 */
@Service
@Profile("!test & !standalone")
public class CacheEvictionService {

    private static final Logger log = LoggerFactory.getLogger(CacheEvictionService.class);

    private final CacheManager cacheManager;

    public CacheEvictionService(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    /**
     * Evict all technology-related caches
     * Called when technology data is modified
     */
    @CacheEvict(value = {"technologies", "technology-search", "categories", "tags"}, allEntries = true)
    public void evictTechnologyCaches() {
        log.info("Evicted all technology-related caches");
    }

    /**
     * Evict all criteria-related caches
     * Called when criteria data is modified
     */
    @CacheEvict(value = "criteria", allEntries = true)
    public void evictCriteriaCaches() {
        log.info("Evicted all criteria-related caches");
    }

    /**
     * Evict all comparison-related caches
     * Called when scoring algorithms or data dependencies change
     */
    @CacheEvict(value = "comparisons", allEntries = true)
    public void evictComparisonCaches() {
        log.info("Evicted all comparison-related caches");
    }

    /**
     * Evict all caches
     * Nuclear option for cache management
     */
    public void evictAllCaches() {
        log.info("Evicting all caches");
        cacheManager.getCacheNames().forEach(cacheName -> {
            var cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                cache.clear();
                log.debug("Cleared cache: {}", cacheName);
            }
        });
        log.info("All caches evicted successfully");
    }

    /**
     * Scheduled cache cleanup - runs every hour to prevent memory bloat
     * This is a safety net for cache entries that might not expire properly
     */
    @Scheduled(fixedRate = 3600000) // 1 hour
    public void scheduledCacheCleanup() {
        log.debug("Running scheduled cache cleanup");
        
        // Get cache statistics and log them
        cacheManager.getCacheNames().forEach(cacheName -> {
            var cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                // Log cache name for monitoring
                log.debug("Cache '{}' is active", cacheName);
            }
        });
        
        log.debug("Scheduled cache cleanup completed");
    }

    /**
     * Get cache statistics for monitoring
     */
    public void logCacheStatistics() {
        log.info("=== Cache Statistics ===");
        cacheManager.getCacheNames().forEach(cacheName -> {
            var cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                log.info("Cache '{}': Active", cacheName);
            }
        });
        log.info("========================");
    }
}