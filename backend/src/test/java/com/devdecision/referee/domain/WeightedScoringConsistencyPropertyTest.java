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
import java.util.stream.Collectors;

/**
 * Property-based tests for WeightedScoringService consistency
 * Feature: dev-decision, Property 5: Weighted Scoring Consistency
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class WeightedScoringConsistencyPropertyTest extends QuickCheckTestBase {

    @Autowired
    private WeightedScoringService weightedScoringService;

    @Autowired
    private InventoryService inventoryService;

    /**
     * Property 5: Weighted Scoring Consistency
     * For any set of technologies and criteria, calculating scores multiple times 
     * with identical inputs should produce identical results
     * **Validates: Requirements 2.1**
     */
    @Test
    void weightedScoringConsistency() {
        Generator<ScoringTestData> testDataGen = createScoringTestDataGenerator();
        
        qt.forAll(testDataGen, new AbstractCharacteristic<ScoringTestData>() {
            @Override
            protected void doSpecify(ScoringTestData testData) throws Throwable {
                // Calculate scores first time
                List<TechnologyScore> firstCalculation = weightedScoringService.calculateScores(
                    testData.technologyIds, testData.userConstraints);
                
                // Calculate scores second time with identical inputs
                List<TechnologyScore> secondCalculation = weightedScoringService.calculateScores(
                    testData.technologyIds, testData.userConstraints);
                
                // Verify results are identical
                assertScoringResultsIdentical(firstCalculation, secondCalculation);
                
                // Also test the by-names method for consistency
                List<String> technologyNames = testData.technologies.stream()
                    .map(Technology::getName)
                    .collect(Collectors.toList());
                
                List<TechnologyScore> firstByNames = weightedScoringService.calculateScoresByNames(
                    technologyNames, testData.userConstraints);
                List<TechnologyScore> secondByNames = weightedScoringService.calculateScoresByNames(
                    technologyNames, testData.userConstraints);
                
                assertScoringResultsIdentical(firstByNames, secondByNames);
            }
        });
    }

    /**
     * Creates a generator for scoring test data including technologies, criteria, and constraints
     */
    private Generator<ScoringTestData> createScoringTestDataGenerator() {
        return new Generator<ScoringTestData>() {
            @Override
            public ScoringTestData next() {
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
                
                return new ScoringTestData(technologies, technologyIds, constraints);
            }
        };
    }

    /**
     * Creates a test technology with predictable metrics
     */
    private Technology createTestTechnology(int index) {
        String uniqueName = "TestTech_" + System.nanoTime() + "_" + index;
        String category = "TestCategory_" + (index % 3); // Rotate through 3 categories
        
        Technology tech = new Technology(uniqueName, category, "Test description " + index);
        
        // Add consistent metrics for testing
        Map<String, Double> metrics = new HashMap<>();
        metrics.put("performance_score", 50.0 + (index * 10.0)); // 50, 60, 70, 80, 90
        metrics.put("learning_curve_score", 40.0 + (index * 5.0)); // 40, 45, 50, 55, 60
        metrics.put("community_score", 60.0 + (index * 8.0)); // 60, 68, 76, 84, 92
        metrics.put("github_stars", 1000.0 + (index * 500.0)); // 1000, 1500, 2000, 2500, 3000
        metrics.put("satisfaction_score", 3.0 + (index * 0.5)); // 3.0, 3.5, 4.0, 4.5, 5.0
        
        tech.setMetrics(metrics);
        
        // Add some tags
        Set<String> tags = new HashSet<>();
        tags.add("tag" + index);
        tags.add("common-tag");
        tech.setTags(tags);
        
        return tech;
    }

    /**
     * Ensures test criteria exist in the system
     */
    private void ensureTestCriteriaExist() {
        List<Criteria> existingCriteria = inventoryService.getAllCriteria();
        
        // If no criteria exist, create some basic ones
        if (existingCriteria.isEmpty()) {
            List<Criteria> testCriteria = Arrays.asList(
                new Criteria("Performance", "Performance metrics", 1.0, CriteriaType.PERFORMANCE),
                new Criteria("Learning Curve", "Ease of learning", 1.0, CriteriaType.LEARNING_CURVE),
                new Criteria("Community Support", "Community size and activity", 1.0, CriteriaType.COMMUNITY),
                new Criteria("Documentation", "Quality of documentation", 1.0, CriteriaType.DOCUMENTATION)
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
        String[] possibleTags = {"performance", "learning-curve", "community", "documentation"};
        
        for (int i = 0; i < numTags && i < possibleTags.length; i++) {
            priorityTags.add(possibleTags[i]);
        }
        
        return UserConstraints.withPriorityTags(priorityTags);
    }

    /**
     * Asserts that two scoring results are identical
     */
    private void assertScoringResultsIdentical(List<TechnologyScore> first, List<TechnologyScore> second) {
        if (first.size() != second.size()) {
            throw new AssertionError("Scoring results should have same size: expected " + 
                first.size() + " but got " + second.size());
        }
        
        // Sort both lists by technology ID for consistent comparison
        first.sort(Comparator.comparing(TechnologyScore::getTechnologyId));
        second.sort(Comparator.comparing(TechnologyScore::getTechnologyId));
        
        for (int i = 0; i < first.size(); i++) {
            TechnologyScore firstScore = first.get(i);
            TechnologyScore secondScore = second.get(i);
            
            // Check technology ID matches
            if (!firstScore.getTechnologyId().equals(secondScore.getTechnologyId())) {
                throw new AssertionError("Technology IDs should match at index " + i + 
                    ": expected " + firstScore.getTechnologyId() + 
                    " but got " + secondScore.getTechnologyId());
            }
            
            // Check overall scores are identical (within floating point precision)
            double scoreDiff = Math.abs(firstScore.getOverallScore() - secondScore.getOverallScore());
            if (scoreDiff > 0.0001) { // Allow for tiny floating point differences
                throw new AssertionError("Overall scores should be identical for technology " + 
                    firstScore.getTechnologyName() + ": expected " + firstScore.getOverallScore() + 
                    " but got " + secondScore.getOverallScore() + " (difference: " + scoreDiff + ")");
            }
            
            // Check criterion scores are identical
            Map<String, Double> firstCriterion = firstScore.getCriterionScores();
            Map<String, Double> secondCriterion = secondScore.getCriterionScores();
            
            if (firstCriterion.size() != secondCriterion.size()) {
                throw new AssertionError("Criterion scores should have same size for technology " + 
                    firstScore.getTechnologyName() + ": expected " + firstCriterion.size() + 
                    " but got " + secondCriterion.size());
            }
            
            for (Map.Entry<String, Double> entry : firstCriterion.entrySet()) {
                String criterionName = entry.getKey();
                Double firstValue = entry.getValue();
                Double secondValue = secondCriterion.get(criterionName);
                
                if (secondValue == null) {
                    throw new AssertionError("Criterion '" + criterionName + 
                        "' missing in second calculation for technology " + firstScore.getTechnologyName());
                }
                
                double criterionDiff = Math.abs(firstValue - secondValue);
                if (criterionDiff > 0.0001) {
                    throw new AssertionError("Criterion scores should be identical for '" + criterionName + 
                        "' on technology " + firstScore.getTechnologyName() + 
                        ": expected " + firstValue + " but got " + secondValue + 
                        " (difference: " + criterionDiff + ")");
                }
            }
        }
    }

    /**
     * Test data container for scoring consistency tests
     */
    private static class ScoringTestData {
        final List<Technology> technologies;
        final List<Long> technologyIds;
        final UserConstraints userConstraints;
        
        ScoringTestData(List<Technology> technologies, List<Long> technologyIds, UserConstraints userConstraints) {
            this.technologies = technologies;
            this.technologyIds = technologyIds;
            this.userConstraints = userConstraints;
        }
    }
}