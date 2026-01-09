package com.devdecision.inventory.domain;

import com.devdecision.inventory.api.InventoryService;
import com.devdecision.shared.config.QuickCheckTestBase;
import net.java.quickcheck.Generator;
import net.java.quickcheck.characteristic.AbstractCharacteristic;
import net.java.quickcheck.generator.PrimitiveGenerators;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Property-based tests for Technology data persistence
 * Feature: dev-decision, Property 1: Technology Data Round Trip
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class TechnologyPersistencePropertyTest extends QuickCheckTestBase {

    @Autowired
    private InventoryService inventoryService;

    /**
     * Property 1: Technology Data Round Trip
     * For any valid technology object with name, category, metrics, and tags,
     * storing it in the system and then retrieving it should return an equivalent
     * object with all properties preserved
     * **Validates: Requirements 1.1, 8.1**
     */
    @Test
    void technologyDataRoundTrip() {
        Generator<Technology> technologyGen = createTechnologyGenerator();
        
        qt.forAll(technologyGen, new AbstractCharacteristic<Technology>() {
            @Override
            protected void doSpecify(Technology originalTechnology) throws Throwable {
                // Save the technology
                Technology savedTechnology = inventoryService.saveTechnology(originalTechnology);
                
                // Retrieve the technology by ID
                Technology retrievedTechnology = inventoryService.findTechnologyById(savedTechnology.getId())
                    .orElseThrow(() -> new AssertionError("Technology should be retrievable after saving"));
                
                // Verify all properties are preserved (excluding auto-generated fields)
                assertTechnologyEquivalent(originalTechnology, retrievedTechnology);
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
     * Asserts that two technologies are equivalent (ignoring auto-generated fields)
     */
    private void assertTechnologyEquivalent(Technology original, Technology retrieved) {
        if (!original.getName().equals(retrieved.getName())) {
            throw new AssertionError("Technology name should be preserved: expected '" + 
                original.getName() + "' but got '" + retrieved.getName() + "'");
        }
        
        if (!original.getCategory().equals(retrieved.getCategory())) {
            throw new AssertionError("Technology category should be preserved: expected '" + 
                original.getCategory() + "' but got '" + retrieved.getCategory() + "'");
        }
        
        // Description can be null, so handle that case
        if (original.getDescription() == null && retrieved.getDescription() != null) {
            throw new AssertionError("Technology description should be null but got: " + retrieved.getDescription());
        }
        if (original.getDescription() != null && !original.getDescription().equals(retrieved.getDescription())) {
            throw new AssertionError("Technology description should be preserved: expected '" + 
                original.getDescription() + "' but got '" + retrieved.getDescription() + "'");
        }
        
        // Check metrics
        if (!original.getMetrics().equals(retrieved.getMetrics())) {
            throw new AssertionError("Technology metrics should be preserved: expected " + 
                original.getMetrics() + " but got " + retrieved.getMetrics());
        }
        
        // Check tags
        if (!original.getTags().equals(retrieved.getTags())) {
            throw new AssertionError("Technology tags should be preserved: expected " + 
                original.getTags() + " but got " + retrieved.getTags());
        }
        
        // Retrieved technology should have an ID assigned
        if (retrieved.getId() == null) {
            throw new AssertionError("Retrieved technology should have an ID assigned");
        }
        
        // Retrieved technology should have createdAt timestamp
        if (retrieved.getCreatedAt() == null) {
            throw new AssertionError("Retrieved technology should have createdAt timestamp");
        }
    }
}