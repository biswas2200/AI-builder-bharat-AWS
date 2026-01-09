package com.devdecision.referee.domain;

import com.devdecision.inventory.api.InventoryService;
import com.devdecision.inventory.domain.Criteria;
import com.devdecision.inventory.domain.CriteriaType;
import com.devdecision.inventory.domain.Technology;
import com.devdecision.referee.api.WeightedScoringService;
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

/**
 * Property-based tests for score normalization range validation
 * Feature: dev-decision, Property 7: Score Normalization Range
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class ScoreNormalizationRangePropertyTest extends QuickCheckTestBase {

    @Autowired
    private WeightedScoringService weightedScoringService;

    @Autowired
    private InventoryService inventoryService;

    /**
     * Property 7: Score Normalization Range
     * For any calculated technology scores, all criterion values should fall within the range 0 to 100 inclusive
     * **Validates: Requirements 2.4**
     */
    @Test
    void scoreNormalizationRange() {
        Generator<ScoreRangeTestData> testDataGen = createScoreRangeTestDataGenerator();
        
        qt.forAll(testDataGen, new AbstractCharacteristic<ScoreRangeTestData>() {
            @Override
            protected void doSpecify(ScoreRangeTestData testData) throws Throwable {
                // Calculate scores for the technologies
                List<TechnologyScore> scores = weightedScoringService.calculateScores(
                    testData.technologyIds, testData.userConstraints);
                
                // Verify all scores are within valid range
                for (TechnologyScore score : scores) {
                    // Check overall score is in range [0, 100]
                    double overallScore = score.getOverallScore();
                    if (overallScore < 0.0 || overallScore > 100.0) {
                        throw new AssertionError("Overall score must be between 0.0 and 100.0, got: " + 
                            overallScore + " for technology " + score.getTechnologyName());
                    }
                    
                    // Check all criterion scores are in range [0, 100]
                    Map<String, Double> criterionScores = score.getCriterionScores();
                    for (Map.Entry<String, Double> entry : criterionScores.entrySet()) {
                        String criterionName = entry.getKey();
                        Double criterionScore = entry.getValue();
                        
                        if (criterionScore == null) {
                            throw new AssertionError("Criterion score should not be null for criterion: " + 
                                criterionName + " on technology " + score.getTechnologyName());
                        }
                        
                        if (criterionScore < 0.0 || criterionScore > 100.0) {
                            throw new AssertionError("Criterion score for '" + criterionName + 
                                "' must be between 0.0 and 100.0, got: " + criterionScore + 
                                " for technology " + score.getTechnologyName());
                        }
                    }
                }
                
                // Also test single technology scoring
                if (!testData.technologyIds.isEmpty()) {
                    Long firstTechId = testData.technologyIds.get(0);
                    TechnologyScore singleScore = weightedScoringService.calculateScore(
                        firstTechId, testData.userConstraints);
                    
                    // Verify single score is also in valid range
                    double singleOverallScore = singleScore.getOverallScore();
                    if (singleOverallScore < 0.0 || singleOverallScore > 100.0) {
                        throw new AssertionError("Single technology overall score must be between 0.0 and 100.0, got: " + 
                            singleOverallScore + " for technology " + singleScore.getTechnologyName());
                    }
                    
                    // Check single technology criterion scores
                    Map<String, Double> singleCriterionScores = singleScore.getCriterionScores();
                    for (Map.Entry<String, Double> entry : singleCriterionScores.entrySet()) {
                        String criterionName = entry.getKey();
                        Double criterionScore = entry.getValue();
                        
                        if (criterionScore < 0.0 || criterionScore > 100.0) {
                            throw new AssertionError("Single technology criterion score for '" + criterionName + 
                                "' must be between 0.0 and 100.0, got: " + criterionScore + 
                                " for technology " + singleScore.getTechnologyName());
                        }
                    }
                }
            }
        });
    }

    /**
     * Creates a generator for score range test data with various metric ranges
     */
    private Generator<ScoreRangeTestData> createScoreRangeTestDataGenerator() {
        return new Generator<ScoreRangeTestData>() {
            @Override
            public ScoreRangeTestData next() {
                // Generate 1-3 technologies for testing
                int numTechnologies = PrimitiveGenerators.integers(1, 3).next();
                List<Technology> technologies = new ArrayList<>();
                List<Long> technologyIds = new ArrayList<>();
                
                for (int i = 0; i < numTechnologies; i++) {
                    Technology tech = createTechnologyWithVariousMetricRanges(i);
                    Technology savedTech = inventoryService.saveTechnology(tech);
                    technologies.add(savedTech);
                    technologyIds.add(savedTech.getId());
                }
                
                // Ensure we have test criteria
                ensureTestCriteriaExist();
                
                // Generate user constraints
                UserConstraints constraints = generateUserConstraints();
                
                return new ScoreRangeTestData(technologies, technologyIds, constraints);
            }
        };
    }

    /**
     * Creates a test technology with metrics in various ranges to test normalization
     */
    private Technology createTechnologyWithVariousMetricRanges(int index) {
        String uniqueName = "RangeTestTech_" + System.nanoTime() + "_" + index;
        String category = "RangeTestCategory";
        
        Technology tech = new Technology(uniqueName, category, "Range test description " + index);
        
        Map<String, Double> metrics = new HashMap<>();
        
        // Test different score ranges that should be normalized to 0-100
        switch (index % 4) {
            case 0:
                // 5-star rating system (0-5 range)
                metrics.put("performance_score", PrimitiveGenerators.doubles(0.0, 5.0).next());
                metrics.put("learning_curve_score", PrimitiveGenerators.doubles(0.0, 5.0).next());
                metrics.put("community_score", PrimitiveGenerators.doubles(0.0, 5.0).next());
                metrics.put("satisfaction_score", PrimitiveGenerators.doubles(0.0, 5.0).next());
                break;
            case 1:
                // 10-point scale (0-10 range)
                metrics.put("performance_score", PrimitiveGenerators.doubles(0.0, 10.0).next());
                metrics.put("learning_curve_score", PrimitiveGenerators.doubles(0.0, 10.0).next());
                metrics.put("community_score", PrimitiveGenerators.doubles(0.0, 10.0).next());
                metrics.put("satisfaction_score", PrimitiveGenerators.doubles(0.0, 10.0).next());
                break;
            case 2:
                // Already in 0-100 scale
                metrics.put("performance_score", PrimitiveGenerators.doubles(0.0, 100.0).next());
                metrics.put("learning_curve_score", PrimitiveGenerators.doubles(0.0, 100.0).next());
                metrics.put("community_score", PrimitiveGenerators.doubles(0.0, 100.0).next());
                metrics.put("satisfaction_score", PrimitiveGenerators.doubles(0.0, 100.0).next());
                break;
            case 3:
                // Large numbers (like GitHub stars) that need logarithmic scaling
                metrics.put("github_stars", PrimitiveGenerators.doubles(1.0, 200000.0).next());
                metrics.put("npm_downloads", PrimitiveGenerators.doubles(100.0, 10000000.0).next());
                metrics.put("job_openings", PrimitiveGenerators.doubles(1.0, 50000.0).next());
                // Mix with normal range scores
                metrics.put("performance_score", PrimitiveGenerators.doubles(0.0, 100.0).next());
                break;
        }
        
        tech.setMetrics(metrics);
        
        // Add some tags
        Set<String> tags = new HashSet<>();
        tags.add("range-test-tag" + index);
        tech.setTags(tags);
        
        return tech;
    }

    /**
     * Ensures test criteria exist in the system
     */
    private void ensureTestCriteriaExist() {
        List<Criteria> existingCriteria = inventoryService.getAllCriteria();
        
        // If no criteria exist, create comprehensive test criteria
        if (existingCriteria.isEmpty()) {
            List<Criteria> testCriteria = Arrays.asList(
                new Criteria("Performance", "Performance metrics", 1.0, CriteriaType.PERFORMANCE),
                new Criteria("Learning Curve", "Ease of learning", 1.0, CriteriaType.LEARNING_CURVE),
                new Criteria("Community Support", "Community size and activity", 1.0, CriteriaType.COMMUNITY),
                new Criteria("Documentation", "Quality of documentation", 1.0, CriteriaType.DOCUMENTATION),
                new Criteria("Scalability", "Scalability characteristics", 1.0, CriteriaType.SCALABILITY),
                new Criteria("Security", "Security features", 1.0, CriteriaType.SECURITY),
                new Criteria("Maturity", "Maturity and stability", 1.0, CriteriaType.MATURITY),
                new Criteria("Developer Experience", "Development productivity", 1.0, CriteriaType.DEVELOPER_EXPERIENCE)
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
        
        // Randomly add 0-2 priority tags
        int numTags = PrimitiveGenerators.integers(0, 2).next();
        String[] possibleTags = {"performance", "learning-curve", "community", "documentation", "scalability"};
        
        for (int i = 0; i < numTags && i < possibleTags.length; i++) {
            priorityTags.add(possibleTags[i]);
        }
        
        return UserConstraints.withPriorityTags(priorityTags);
    }

    /**
     * Test data container for score range tests
     */
    private static class ScoreRangeTestData {
        final List<Technology> technologies;
        final List<Long> technologyIds;
        final UserConstraints userConstraints;
        
        ScoreRangeTestData(List<Technology> technologies, List<Long> technologyIds, UserConstraints userConstraints) {
            this.technologies = technologies;
            this.technologyIds = technologyIds;
            this.userConstraints = userConstraints;
        }
    }
}