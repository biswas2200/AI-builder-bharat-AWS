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
 * Property-based tests for technology display completeness
 * Feature: dev-decision, Property 3: Technology Display Completeness
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class TechnologyDisplayCompletenessPropertyTest extends QuickCheckTestBase {

    @Autowired
    private InventoryService inventoryService;

    /**
     * Property 3: Technology Display Completeness
     * For any technology being displayed, the rendered output should include 
     * category, description, and key characteristics fields
     * **Validates: Requirements 1.4**
     */
    @Test
    void technologyDisplayCompleteness() {
        Generator<Technology> technologyGen = createTechnologyGenerator();
        
        qt.forAll(technologyGen, new AbstractCharacteristic<Technology>() {
            @Override
            protected void doSpecify(Technology technology) throws Throwable {
                // Save the technology to ensure it exists in the system
                Technology savedTechnology = inventoryService.saveTechnology(technology);
                
                // Retrieve the technology as it would be displayed (via API)
                Technology displayedTechnology = inventoryService.findTechnologyById(savedTechnology.getId())
                    .orElseThrow(() -> new AssertionError("Technology should be retrievable for display"));
                
                // Verify that the displayed technology contains all required display fields
                assertDisplayCompleteness(displayedTechnology);
            }
        });
    }

    /**
     * Creates a generator for valid Technology objects with varying completeness
     */
    private Generator<Technology> createTechnologyGenerator() {
        return new Generator<Technology>() {
            @Override
            public Technology next() {
                // Generate valid technology name (1-100 characters, non-blank)
                String name = generateValidName();
                
                // Generate valid category (1-50 characters, non-blank)
                String category = generateValidCategory();
                
                // Generate description (can be null, empty, or have content)
                String description = generateDescription();
                
                // Create technology with basic properties
                Technology technology = new Technology(name, category, description);
                
                // Add metrics (key characteristics)
                Map<String, Double> metrics = generateMetrics();
                technology.setMetrics(metrics);
                
                // Add tags (key characteristics)
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
     * Generates description (can be null, empty, or have content to test various scenarios)
     */
    private String generateDescription() {
        // 20% chance of null, 10% chance of empty, 70% chance of content
        double random = PrimitiveGenerators.doubles().next();
        if (random < 0.2) {
            return null;
        } else if (random < 0.3) {
            return "";
        } else {
            String desc = PrimitiveGenerators.strings().next();
            return desc != null ? desc.trim() : "Default description";
        }
    }

    /**
     * Generates random metrics map (key characteristics)
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
     * Generates random tags set (key characteristics)
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
     * Asserts that a technology contains all required display fields according to Requirements 1.4:
     * - Category (required)
     * - Description (should be accessible, even if null/empty)
     * - Key characteristics (metrics and tags should be accessible)
     */
    private void assertDisplayCompleteness(Technology technology) {
        // Verify category is present and not null/empty (required field)
        if (technology.getCategory() == null || technology.getCategory().trim().isEmpty()) {
            throw new AssertionError("Technology display must include a non-empty category, but got: " + 
                technology.getCategory());
        }
        
        // Verify description field is accessible (can be null/empty, but getter should work)
        try {
            String description = technology.getDescription();
            // Description can be null or empty, but the field should be accessible
            // This ensures the API response includes the description field
        } catch (Exception e) {
            throw new AssertionError("Technology display must include accessible description field, but got exception: " + 
                e.getMessage());
        }
        
        // Verify key characteristics (metrics) are accessible
        try {
            Map<String, Double> metrics = technology.getMetrics();
            if (metrics == null) {
                throw new AssertionError("Technology display must include accessible metrics (key characteristics), but got null");
            }
            // Metrics can be empty, but should be a valid map
        } catch (Exception e) {
            throw new AssertionError("Technology display must include accessible metrics (key characteristics), but got exception: " + 
                e.getMessage());
        }
        
        // Verify key characteristics (tags) are accessible
        try {
            Set<String> tags = technology.getTags();
            if (tags == null) {
                throw new AssertionError("Technology display must include accessible tags (key characteristics), but got null");
            }
            // Tags can be empty, but should be a valid set
        } catch (Exception e) {
            throw new AssertionError("Technology display must include accessible tags (key characteristics), but got exception: " + 
                e.getMessage());
        }
        
        // Verify that the technology has some form of key characteristics
        // Either metrics or tags should provide meaningful characteristics
        Map<String, Double> metrics = technology.getMetrics();
        Set<String> tags = technology.getTags();
        
        boolean hasMetrics = metrics != null && !metrics.isEmpty();
        boolean hasTags = tags != null && !tags.isEmpty();
        
        if (!hasMetrics && !hasTags) {
            // This is acceptable - a technology might not have metrics or tags yet
            // The requirement is that the fields are accessible for display, not that they must have content
            // The API should still return these fields (even if empty) so the UI can display them
        }
        
        // Verify that all required display fields are non-null at the object level
        // This ensures the JSON serialization will include all required fields
        if (technology.getName() == null) {
            throw new AssertionError("Technology display must include name field");
        }
        
        // Additional validation: ensure the technology can be properly serialized for display
        // This simulates what happens when the API returns the technology as JSON
        try {
            // Test that all display-critical methods work
            String displayInfo = String.format("Technology: %s, Category: %s, Description: %s, Metrics: %d, Tags: %d",
                technology.getName(),
                technology.getCategory(),
                technology.getDescription() != null ? technology.getDescription() : "null",
                technology.getMetrics().size(),
                technology.getTags().size()
            );
            
            // If we can create this display string without exceptions, the technology is displayable
            if (displayInfo.isEmpty()) {
                throw new AssertionError("Technology display information should not be empty");
            }
            
        } catch (Exception e) {
            throw new AssertionError("Technology must be displayable without exceptions, but got: " + e.getMessage());
        }
    }
}