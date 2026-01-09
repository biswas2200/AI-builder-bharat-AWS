package com.devdecision.referee.domain;

import com.devdecision.inventory.api.InventoryService;
import com.devdecision.inventory.domain.Criteria;
import com.devdecision.inventory.domain.CriteriaType;
import com.devdecision.inventory.domain.Technology;
import com.devdecision.referee.api.ComparisonService;
import com.devdecision.shared.config.QuickCheckTestBase;
import net.java.quickcheck.Generator;
import net.java.quickcheck.characteristic.AbstractCharacteristic;
import net.java.quickcheck.generator.PrimitiveGenerators;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Property-based tests for radar chart data structure
 * Feature: dev-decision, Property 9: Radar Chart Data Structure
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class RadarChartDataStructurePropertyTest extends QuickCheckTestBase {

    @Autowired
    private ComparisonService comparisonService;

    @Autowired
    private InventoryService inventoryService;

    /**
     * Property 9: Radar Chart Data Structure
     * For any set of compared technologies, the generated radar chart data should contain 
     * subject, technology score fields (A, B, C, etc.), and fullMark values for each criterion
     * **Validates: Requirements 3.2**
     */
    @Test
    void radarChartDataStructure() {
        Generator<ComparisonTestData> testDataGen = createComparisonTestDataGenerator();
        
        qt.forAll(testDataGen, new AbstractCharacteristic<ComparisonTestData>() {
            @Override
            protected void doSpecify(ComparisonTestData testData) throws Throwable {
                // Generate comparison result with radar chart data
                ComparisonResult result = comparisonService.generateComparison(
                    testData.technologyIds, testData.userConstraints);
                
                // Verify radar chart data structure
                List<RadarChartData> radarData = result.getRadarData();
                
                // Property: Radar chart data should not be null or empty
                if (radarData == null) {
                    throw new AssertionError("Radar chart data should not be null");
                }
                
                if (radarData.isEmpty()) {
                    throw new AssertionError("Radar chart data should not be empty for valid comparison");
                }
                
                // Property: Each radar chart data point should have required structure
                for (RadarChartData chartData : radarData) {
                    validateRadarChartDataStructure(chartData, testData.technologies.size());
                }
                
                // Property: Number of radar data points should match number of criteria
                List<Criteria> allCriteria = inventoryService.getAllCriteria();
                if (!allCriteria.isEmpty()) {
                    // Should have at least one radar data point per criterion that has data
                    if (radarData.size() > allCriteria.size()) {
                        throw new AssertionError("Radar chart data should not exceed number of criteria: " +
                            "expected max " + allCriteria.size() + " but got " + radarData.size());
                    }
                }
                
                // Property: All radar data points should have consistent technology count
                int expectedTechCount = testData.technologies.size();
                for (RadarChartData chartData : radarData) {
                    int actualTechCount = chartData.getTechnologyCount();
                    // For single technology, the system uses 2-technology structure with 0.0 placeholder
                    int expectedStructureCount = expectedTechCount == 1 ? 2 : expectedTechCount;
                    if (actualTechCount != expectedStructureCount) {
                        throw new AssertionError("Radar chart data should have consistent technology count: " +
                            "expected " + expectedStructureCount + " but got " + actualTechCount + 
                            " for criterion '" + chartData.getSubject() + "'");
                    }
                }
            }
        });
    }

    /**
     * Validates the structure of a single radar chart data point
     */
    private void validateRadarChartDataStructure(RadarChartData chartData, int expectedTechnologyCount) {
        // Property: Subject should not be null or empty
        if (chartData.getSubject() == null || chartData.getSubject().trim().isEmpty()) {
            throw new AssertionError("Radar chart data subject should not be null or empty");
        }
        
        // Property: Technology A score should always be present and valid
        if (chartData.getA() == null) {
            throw new AssertionError("Technology A score should not be null");
        }
        
        validateScoreRange(chartData.getA(), "A", chartData.getSubject());
        
        // Property: FullMark should be present and valid
        if (chartData.getFullMark() == null) {
            throw new AssertionError("FullMark should not be null for criterion '" + chartData.getSubject() + "'");
        }
        
        validateScoreRange(chartData.getFullMark(), "fullMark", chartData.getSubject());
        
        // Property: Technology scores should be present based on number of technologies
        switch (expectedTechnologyCount) {
            case 1:
                // A should be non-null, B should be 0.0 (placeholder), others should be null
                if (chartData.getB() == null || !chartData.getB().equals(0.0)) {
                    throw new AssertionError("For 1 technology, B score should be 0.0 (placeholder) for criterion '" + 
                        chartData.getSubject() + "', but got: " + chartData.getB());
                }
                if (chartData.getC() != null || chartData.getD() != null || chartData.getE() != null) {
                    throw new AssertionError("For 1 technology, only A and B (0.0) scores should be present for criterion '" + 
                        chartData.getSubject() + "'");
                }
                break;
            case 2:
                // A and B should be non-null, others null
                if (chartData.getB() == null) {
                    throw new AssertionError("Technology B score should not be null for 2 technologies on criterion '" + 
                        chartData.getSubject() + "'");
                }
                validateScoreRange(chartData.getB(), "B", chartData.getSubject());
                
                if (chartData.getC() != null || chartData.getD() != null || chartData.getE() != null) {
                    throw new AssertionError("For 2 technologies, only A and B scores should be present for criterion '" + 
                        chartData.getSubject() + "'");
                }
                break;
            case 3:
                // A, B, C should be non-null, D and E null
                if (chartData.getB() == null || chartData.getC() == null) {
                    throw new AssertionError("Technology B and C scores should not be null for 3 technologies on criterion '" + 
                        chartData.getSubject() + "'");
                }
                validateScoreRange(chartData.getB(), "B", chartData.getSubject());
                validateScoreRange(chartData.getC(), "C", chartData.getSubject());
                
                if (chartData.getD() != null || chartData.getE() != null) {
                    throw new AssertionError("For 3 technologies, only A, B, and C scores should be present for criterion '" + 
                        chartData.getSubject() + "'");
                }
                break;
            case 4:
                // A, B, C, D should be non-null, E null
                if (chartData.getB() == null || chartData.getC() == null || chartData.getD() == null) {
                    throw new AssertionError("Technology B, C, and D scores should not be null for 4 technologies on criterion '" + 
                        chartData.getSubject() + "'");
                }
                validateScoreRange(chartData.getB(), "B", chartData.getSubject());
                validateScoreRange(chartData.getC(), "C", chartData.getSubject());
                validateScoreRange(chartData.getD(), "D", chartData.getSubject());
                
                if (chartData.getE() != null) {
                    throw new AssertionError("For 4 technologies, only A, B, C, and D scores should be present for criterion '" + 
                        chartData.getSubject() + "'");
                }
                break;
            case 5:
                // All scores should be non-null
                if (chartData.getB() == null || chartData.getC() == null || 
                    chartData.getD() == null || chartData.getE() == null) {
                    throw new AssertionError("All technology scores should not be null for 5 technologies on criterion '" + 
                        chartData.getSubject() + "'");
                }
                validateScoreRange(chartData.getB(), "B", chartData.getSubject());
                validateScoreRange(chartData.getC(), "C", chartData.getSubject());
                validateScoreRange(chartData.getD(), "D", chartData.getSubject());
                validateScoreRange(chartData.getE(), "E", chartData.getSubject());
                break;
            default:
                throw new AssertionError("Unsupported number of technologies: " + expectedTechnologyCount);
        }
        
        // Property: All non-null scores should be accessible by index
        for (int i = 0; i < expectedTechnologyCount; i++) {
            Double scoreByIndex = chartData.getScoreByIndex(i);
            if (scoreByIndex == null) {
                throw new AssertionError("Score at index " + i + " should not be null for criterion '" + 
                    chartData.getSubject() + "'");
            }
            validateScoreRange(scoreByIndex, "index " + i, chartData.getSubject());
        }
        
        // For single technology, also validate that index 1 returns 0.0
        if (expectedTechnologyCount == 1) {
            Double scoreBByIndex = chartData.getScoreByIndex(1);
            if (scoreBByIndex == null || !scoreBByIndex.equals(0.0)) {
                throw new AssertionError("Score at index 1 should be 0.0 for single technology comparison on criterion '" + 
                    chartData.getSubject() + "', but got: " + scoreBByIndex);
            }
        }
    }

    /**
     * Validates that a score is within the valid range (0.0 to 100.0)
     */
    private void validateScoreRange(Double score, String scoreName, String criterion) {
        if (score < 0.0 || score > 100.0) {
            throw new AssertionError("Score " + scoreName + " should be between 0.0 and 100.0 for criterion '" + 
                criterion + "', but got: " + score);
        }
    }

    /**
     * Creates a generator for comparison test data including technologies and constraints
     */
    private Generator<ComparisonTestData> createComparisonTestDataGenerator() {
        return new Generator<ComparisonTestData>() {
            @Override
            public ComparisonTestData next() {
                // Generate 1-5 technologies for comparison
                int numTechnologies = PrimitiveGenerators.integers(1, 5).next();
                List<Technology> technologies = new ArrayList<>();
                List<Long> technologyIds = new ArrayList<>();
                
                for (int i = 0; i < numTechnologies; i++) {
                    Technology tech = createTestTechnology(i);
                    Technology savedTech = inventoryService.saveTechnology(tech);
                    technologies.add(savedTech);
                    technologyIds.add(savedTech.getId());
                }
                
                // Ensure we have some criteria in the system
                ensureTestCriteriaExist();
                
                // Generate user constraints
                UserConstraints constraints = generateUserConstraints();
                
                return new ComparisonTestData(technologies, technologyIds, constraints);
            }
        };
    }

    /**
     * Creates a test technology with predictable metrics for radar chart generation
     */
    private Technology createTestTechnology(int index) {
        String uniqueName = "RadarTestTech_" + System.nanoTime() + "_" + index;
        String category = "RadarTestCategory_" + (index % 3); // Rotate through 3 categories
        
        Technology tech = new Technology(uniqueName, category, "Radar test description " + index);
        
        // Add metrics that will be used for radar chart generation
        Map<String, Double> metrics = new HashMap<>();
        metrics.put("performance_score", 20.0 + (index * 15.0)); // 20, 35, 50, 65, 80
        metrics.put("learning_curve_score", 30.0 + (index * 12.0)); // 30, 42, 54, 66, 78
        metrics.put("community_score", 40.0 + (index * 10.0)); // 40, 50, 60, 70, 80
        metrics.put("documentation_score", 25.0 + (index * 18.0)); // 25, 43, 61, 79, 97
        metrics.put("scalability_score", 35.0 + (index * 13.0)); // 35, 48, 61, 74, 87
        metrics.put("github_stars", 1000.0 + (index * 500.0)); // For KPI metrics
        metrics.put("satisfaction_score", 3.0 + (index * 0.4)); // For KPI metrics
        
        tech.setMetrics(metrics);
        
        // Add some tags for scoring
        Set<String> tags = new HashSet<>();
        tags.add("radar-tag-" + index);
        tags.add("common-radar-tag");
        tech.setTags(tags);
        
        return tech;
    }

    /**
     * Ensures test criteria exist in the system for radar chart generation
     */
    private void ensureTestCriteriaExist() {
        List<Criteria> existingCriteria = inventoryService.getAllCriteria();
        
        // If no criteria exist, create some basic ones that match our test metrics
        if (existingCriteria.isEmpty()) {
            List<Criteria> testCriteria = Arrays.asList(
                new Criteria("Performance", "Performance metrics", 1.0, CriteriaType.PERFORMANCE),
                new Criteria("Learning Curve", "Ease of learning", 1.0, CriteriaType.LEARNING_CURVE),
                new Criteria("Community Support", "Community size and activity", 1.0, CriteriaType.COMMUNITY),
                new Criteria("Documentation", "Quality of documentation", 1.0, CriteriaType.DOCUMENTATION),
                new Criteria("Scalability", "Scalability characteristics", 1.0, CriteriaType.SCALABILITY)
            );
            
            for (Criteria criteria : testCriteria) {
                inventoryService.saveCriteria(criteria);
            }
        }
    }

    /**
     * Generates random user constraints for testing
     */
    private UserConstraints generateUserConstraints() {
        Set<String> priorityTags = new HashSet<>();
        
        // Randomly add 0-3 priority tags
        int numTags = PrimitiveGenerators.integers(0, 3).next();
        String[] possibleTags = {"performance", "learning-curve", "community", "documentation", "scalability"};
        
        for (int i = 0; i < numTags && i < possibleTags.length; i++) {
            priorityTags.add(possibleTags[i]);
        }
        
        return UserConstraints.withPriorityTags(priorityTags);
    }

    /**
     * Test data container for comparison tests
     */
    private static class ComparisonTestData {
        final List<Technology> technologies;
        final List<Long> technologyIds;
        final UserConstraints userConstraints;
        
        ComparisonTestData(List<Technology> technologies, List<Long> technologyIds, UserConstraints userConstraints) {
            this.technologies = technologies;
            this.technologyIds = technologyIds;
            this.userConstraints = userConstraints;
        }
    }
}