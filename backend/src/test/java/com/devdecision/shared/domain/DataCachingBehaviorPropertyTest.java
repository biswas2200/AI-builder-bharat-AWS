package com.devdecision.shared.domain;

import com.devdecision.inventory.api.InventoryService;
import com.devdecision.inventory.domain.Technology;
import com.devdecision.shared.config.QuickCheckTestBase;
import net.java.quickcheck.Generator;
import net.java.quickcheck.characteristic.AbstractCharacteristic;
import net.java.quickcheck.generator.PrimitiveGenerators;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Property-based tests for data caching behavior
 * Feature: dev-decision, Property 17: Data Caching Behavior
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class DataCachingBehaviorPropertyTest extends QuickCheckTestBase {

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private CacheManager cacheManager;

    /**
     * Property 17: Data Caching Behavior
     * For any frequently accessed technology data, subsequent requests should retrieve 
     * the data from cache when available, reducing database queries
     * **Validates: Requirements 8.2**
     */
    @Test
    void dataCachingBehavior() {
        Generator<Technology> technologyGen = createTechnologyGenerator();
        
        qt.forAll(technologyGen, new AbstractCharacteristic<Technology>() {
            @Override
            protected void doSpecify(Technology technology) throws Throwable {
                // Clear all caches to start fresh
                clearAllCaches();
                
                // Save the technology first
                Technology savedTechnology = inventoryService.saveTechnology(technology);
                Long technologyId = savedTechnology.getId();
                String technologyName = savedTechnology.getName();
                String category = savedTechnology.getCategory();
                
                // Clear caches again after save (since save evicts cache)
                clearAllCaches();
                
                // First access - should populate cache
                inventoryService.findTechnologyById(technologyId);
                
                // Verify cache is populated for ID lookup
                assertCacheContainsKey("technologies", "id:" + technologyId);
                
                // Second access - should hit cache
                inventoryService.findTechnologyById(technologyId);
                
                // Cache should still contain the entry
                assertCacheContainsKey("technologies", "id:" + technologyId);
                
                // Test category-based caching
                clearAllCaches();
                inventoryService.findTechnologiesByCategory(category);
                assertCacheContainsKey("technologies", "category:" + category);
                
                // Second category access should hit cache
                inventoryService.findTechnologiesByCategory(category);
                assertCacheContainsKey("technologies", "category:" + category);
                
                // Test name-based caching
                clearAllCaches();
                inventoryService.findTechnologyByName(technologyName);
                assertCacheContainsKey("technologies", "name:" + technologyName);
                
                // Second name access should hit cache
                inventoryService.findTechnologyByName(technologyName);
                assertCacheContainsKey("technologies", "name:" + technologyName);
                
                // Test that cache eviction works on save operations
                clearAllCaches();
                inventoryService.findTechnologyById(technologyId);
                assertCacheContainsKey("technologies", "id:" + technologyId);
                
                // Modify and save technology - should evict cache
                savedTechnology.setDescription("Modified description");
                inventoryService.saveTechnology(savedTechnology);
                
                // Cache should be cleared after save operation
                assertCacheDoesNotContainKey("technologies", "id:" + technologyId);
            }
        });
    }

    /**
     * Creates a generator for valid Technology objects
     */
    private Generator<Technology> createTechnologyGenerator() {
        return new Generator<Technology>() {
            @Override
            public Technology next() {
                // Generate valid technology name (1-100 characters, non-blank)
                String name = generateValidName();
                
                // Generate valid category (1-50 characters, non-blank)
                String category = generateValidCategory();
                
                // Generate optional description
                String description = PrimitiveGenerators.strings().next();
                
                // Create technology with basic properties
                Technology technology = new Technology(name, category, description);
                
                // Add random metrics
                Map<String, Double> metrics = generateMetrics();
                technology.setMetrics(metrics);
                
                // Add random tags
                Set<String> tags = generateTags();
                technology.setTags(tags);
                
                return technology;
            }
        };
    }

    /**
     * Generates a valid technology name (1-100 characters, non-blank, unique)
     */
    private String generateValidName() {
        String baseName;
        do {
            baseName = PrimitiveGenerators.strings().next();
        } while (baseName == null || baseName.trim().isEmpty() || baseName.length() > 90);
        
        // Ensure it's not just whitespace and within length limits
        baseName = baseName.trim();
        if (baseName.isEmpty()) {
            baseName = "TestTech"; // Fallback to ensure non-empty
        }
        
        // Add timestamp to ensure uniqueness (within 100 char limit)
        String uniqueName = baseName + "_" + System.nanoTime();
        if (uniqueName.length() > 100) {
            // Truncate base name to fit timestamp
            int maxBaseLength = 100 - String.valueOf(System.nanoTime()).length() - 1;
            baseName = baseName.substring(0, Math.min(baseName.length(), maxBaseLength));
            uniqueName = baseName + "_" + System.nanoTime();
        }
        
        return uniqueName;
    }

    /**
     * Generates a valid category (1-50 characters, non-blank)
     */
    private String generateValidCategory() {
        String category;
        do {
            category = PrimitiveGenerators.strings().next();
        } while (category == null || category.trim().isEmpty() || category.length() > 50);
        
        // Ensure it's not just whitespace and within length limits
        category = category.trim();
        if (category.length() > 50) {
            category = category.substring(0, 50);
        }
        if (category.isEmpty()) {
            category = "TestCategory"; // Fallback to ensure non-empty
        }
        
        return category;
    }

    /**
     * Generates random metrics map
     */
    private Map<String, Double> generateMetrics() {
        Map<String, Double> metrics = new HashMap<>();
        
        // Add some common metrics with random values
        String[] metricKeys = {
            "github_stars", "npm_downloads", "job_openings", 
            "satisfaction_score", "performance_score", 
            "learning_curve_score", "community_score"
        };
        
        // Randomly add 0-7 metrics
        int numMetrics = PrimitiveGenerators.integers(0, metricKeys.length).next();
        for (int i = 0; i < numMetrics; i++) {
            String key = metricKeys[i];
            Double value = PrimitiveGenerators.doubles().next();
            // Ensure positive values for most metrics
            if (value != null && !Double.isNaN(value) && !Double.isInfinite(value)) {
                metrics.put(key, Math.abs(value));
            }
        }
        
        return metrics;
    }

    /**
     * Generates random tags set
     */
    private Set<String> generateTags() {
        Set<String> tags = new HashSet<>();
        
        // Add 0-5 random tags
        int numTags = PrimitiveGenerators.integers(0, 5).next();
        for (int i = 0; i < numTags; i++) {
            String tag = PrimitiveGenerators.strings().next();
            if (tag != null && !tag.trim().isEmpty()) {
                tags.add(tag.trim().toLowerCase());
            }
        }
        
        return tags;
    }

    /**
     * Clears all caches to ensure clean test state
     */
    private void clearAllCaches() {
        cacheManager.getCacheNames().forEach(cacheName -> {
            var cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                cache.clear();
            }
        });
    }

    /**
     * Asserts that a cache contains a specific key
     */
    private void assertCacheContainsKey(String cacheName, String key) {
        var cache = cacheManager.getCache(cacheName);
        if (cache == null) {
            throw new AssertionError("Cache '" + cacheName + "' should exist");
        }
        
        var cachedValue = cache.get(key);
        if (cachedValue == null) {
            throw new AssertionError("Cache '" + cacheName + "' should contain key '" + key + "'");
        }
    }

    /**
     * Asserts that a cache does not contain a specific key
     */
    private void assertCacheDoesNotContainKey(String cacheName, String key) {
        var cache = cacheManager.getCache(cacheName);
        if (cache == null) {
            return; // Cache doesn't exist, so it definitely doesn't contain the key
        }
        
        var cachedValue = cache.get(key);
        if (cachedValue != null) {
            throw new AssertionError("Cache '" + cacheName + "' should not contain key '" + key + "'");
        }
    }
}