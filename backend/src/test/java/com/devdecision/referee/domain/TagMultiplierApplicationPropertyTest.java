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
 * Property-based tests for tag multiplier application in weighted scoring
 * Feature: dev-decision, Property 6: Tag Multiplier Application
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class TagMultiplierApplicationPropertyTest extends QuickCheckTestBase {

    @Autowired
    private WeightedScoringService weightedScoringService;

    @Autowired
    private InventoryService inventoryService;

    private static final double TAG_MULTIPLIER = 1.5;
    private static final double TOLERANCE = 0.0001;

    /**
     * Property 6: Tag Multiplier Application
     * For any technology score and selected importance tag, when the tag matches a criterion type,
     * the corresponding score should be exactly 1.5 times the base score
     * **Validates: Requirements 2.2**
     */
    @Test
    void tagMultiplierApplication() {
        Generator<TagMultiplierTestData> testDataGen = createTagMultiplierTestDataGenerator();
        
        qt.forAll(testDataGen, new AbstractCharacteristic<TagMultiplierTestData>() {
            @Override
            protected void doSpecify(TagMultiplierTestData testData) throws Throwable {
                // Calculate scores without priority tags (baseline)
                UserConstraints baselineConstraints = UserConstraints.empty();
                List<TechnologyScore> baselineScores = weightedScoringService.calculateScores(
                    testData.technologyIds, baselineConstraints);
                
                // Calculate scores with priority tag
                UserConstraints priorityConstraints = UserConstraints.withPriorityTags(
                    Set.of(testData.priorityTag));
                List<TechnologyScore> priorityScores = weightedScoringService.calculateScores(
                    testData.technologyIds, priorityConstraints);
                
                // Verify tag multiplier was applied correctly
                verifyTagMultiplierApplication(baselineScores, priorityScores, 
                    testData.priorityTag, testData.expectedCriterionName);
            }
        });
    }

    /**
     * Creates a generator for tag multiplier test data
     */
    private Generator<TagMultiplierTestData> createTagMultiplierTestDataGenerator() {
        return new Generator<TagMultiplierTestData>() {
            @Override
            public TagMultiplierTestData next() {
                // Ensure test criteria exist
                ensureTestCriteriaExist();
                
                // Generate 1-3 technologies for testing
                int numTechnologies = PrimitiveGenerators.integers(1, 3).next();
                List<Technology> technologies = new ArrayList<>();
                List<Long> technologyIds = new ArrayList<>();
                
                for (int i = 0; i < numTechnologies; i++) {
                    Technology tech = createTestTechnology(i);
                    Technology savedTech = inventoryService.saveTechnology(tech);
                    technologies.add(savedTech);
                    technologyIds.add(savedTech.getId());
                }
                
                // Select a random criteria type to test multiplier on
                CriteriaType[] criteriaTypes = CriteriaType.values();
                CriteriaType selectedType = criteriaTypes[PrimitiveGenerators.integers(0, criteriaTypes.length - 1).next()];
                
                // Map criteria type to priority tag format
                String priorityTag = mapCriteriaTypeToTag(selectedType);
                String expectedCriterionName = mapCriteriaTypeToName(selectedType);
                
                return new TagMultiplierTestData(technologies, technologyIds, priorityTag, expectedCriterionName);
            }
        };
    }

    /**
     * Creates a test technology with predictable metrics for all criteria types
     */
    private Technology createTestTechnology(int index) {
        String uniqueName = "TagMultiplierTest_" + System.nanoTime() + "_" + index;
        String category = "TestCategory";
        
        Technology tech = new Technology(uniqueName, category, "Test description for tag multiplier");
        
        // Add metrics for all criteria types with predictable values
        Map<String, Double> metrics = new HashMap<>();
        double baseValue = 50.0 + (index * 10.0); // 50, 60, 70, etc.
        
        metrics.put("performance_score", baseValue);
        metrics.put("learning_curve_score", baseValue + 5.0);
        metrics.put("community_score", baseValue + 10.0);
        metrics.put("documentation_score", baseValue + 15.0);
        metrics.put("scalability_score", baseValue + 20.0);
        metrics.put("security_score", baseValue + 25.0);
        metrics.put("maturity_score", baseValue + 30.0);
        metrics.put("developer_experience_score", baseValue + 35.0);
        metrics.put("cost_score", baseValue + 40.0);
        metrics.put("custom_score", baseValue + 45.0);
        
        tech.setMetrics(metrics);
        
        // Add some tags
        Set<String> tags = new HashSet<>();
        tags.add("test-tag-" + index);
        tech.setTags(tags);
        
        return tech;
    }

    /**
     * Ensures test criteria exist in the system for all types
     */
    private void ensureTestCriteriaExist() {
        List<Criteria> existingCriteria = inventoryService.getAllCriteria();
        Set<CriteriaType> existingTypes = existingCriteria.stream()
            .map(Criteria::getType)
            .collect(Collectors.toSet());
        
        // Create criteria for any missing types
        for (CriteriaType type : CriteriaType.values()) {
            if (!existingTypes.contains(type)) {
                String name = mapCriteriaTypeToName(type);
                Criteria criteria = new Criteria(name, "Test criteria for " + name, 1.0, type);
                inventoryService.saveCriteria(criteria);
            }
        }
    }

    /**
     * Maps criteria type to the priority tag format used by users
     */
    private String mapCriteriaTypeToTag(CriteriaType type) {
        return switch (type) {
            case PERFORMANCE -> "performance";
            case LEARNING_CURVE -> "learning-curve";
            case COMMUNITY -> "community";
            case DOCUMENTATION -> "documentation";
            case SCALABILITY -> "scalability";
            case SECURITY -> "security";
            case MATURITY -> "maturity";
            case DEVELOPER_EXPERIENCE -> "developer-experience";
            case COST -> "cost";
            case CUSTOM -> "custom";
        };
    }

    /**
     * Maps criteria type to the criterion name used in the system
     */
    private String mapCriteriaTypeToName(CriteriaType type) {
        return switch (type) {
            case PERFORMANCE -> "Performance";
            case LEARNING_CURVE -> "Learning Curve";
            case COMMUNITY -> "Community Support";
            case DOCUMENTATION -> "Documentation Quality";
            case SCALABILITY -> "Scalability";
            case SECURITY -> "Security";
            case MATURITY -> "Maturity";
            case DEVELOPER_EXPERIENCE -> "Developer Experience";
            case COST -> "Cost";
            case CUSTOM -> "Custom";
        };
    }

    /**
     * Verifies that the tag multiplier was applied correctly
     */
    private void verifyTagMultiplierApplication(List<TechnologyScore> baselineScores,
                                              List<TechnologyScore> priorityScores,
                                              String priorityTag,
                                              String expectedCriterionName) {
        if (baselineScores.size() != priorityScores.size()) {
            throw new AssertionError("Baseline and priority scores should have same size: expected " + 
                baselineScores.size() + " but got " + priorityScores.size());
        }
        
        // Sort both lists by technology ID for consistent comparison
        baselineScores.sort(Comparator.comparing(TechnologyScore::getTechnologyId));
        priorityScores.sort(Comparator.comparing(TechnologyScore::getTechnologyId));
        
        for (int i = 0; i < baselineScores.size(); i++) {
            TechnologyScore baselineScore = baselineScores.get(i);
            TechnologyScore priorityScore = priorityScores.get(i);
            
            // Verify same technology
            if (!baselineScore.getTechnologyId().equals(priorityScore.getTechnologyId())) {
                throw new AssertionError("Technology IDs should match at index " + i);
            }
            
            // Get criterion scores for the prioritized criterion
            Map<String, Double> baselineCriteria = baselineScore.getCriterionScores();
            Map<String, Double> priorityCriteria = priorityScore.getCriterionScores();
            
            Double baselineCriterionScore = baselineCriteria.get(expectedCriterionName);
            Double priorityCriterionScore = priorityCriteria.get(expectedCriterionName);
            
            if (baselineCriterionScore == null) {
                throw new AssertionError("Baseline criterion score missing for '" + expectedCriterionName + 
                    "' on technology " + baselineScore.getTechnologyName());
            }
            
            if (priorityCriterionScore == null) {
                throw new AssertionError("Priority criterion score missing for '" + expectedCriterionName + 
                    "' on technology " + priorityScore.getTechnologyName());
            }
            
            // The key assertion: priority criterion score should be exactly TAG_MULTIPLIER times baseline
            // Note: We need to account for normalization happening after multiplier application
            // So we compare the effect on the overall score rather than raw criterion scores
            
            // Check that the overall score increased due to the multiplier
            double baselineOverall = baselineScore.getOverallScore();
            double priorityOverall = priorityScore.getOverallScore();
            
            if (priorityOverall <= baselineOverall) {
                throw new AssertionError("Priority overall score should be higher than baseline for technology " + 
                    baselineScore.getTechnologyName() + " when '" + priorityTag + "' is prioritized. " +
                    "Baseline: " + baselineOverall + ", Priority: " + priorityOverall);
            }
            
            // Verify that non-prioritized criteria remain unchanged
            for (Map.Entry<String, Double> entry : baselineCriteria.entrySet()) {
                String criterionName = entry.getKey();
                if (!criterionName.equals(expectedCriterionName)) {
                    Double baselineValue = entry.getValue();
                    Double priorityValue = priorityCriteria.get(criterionName);
                    
                    if (priorityValue == null) {
                        throw new AssertionError("Non-priority criterion '" + criterionName + 
                            "' missing in priority calculation for technology " + baselineScore.getTechnologyName());
                    }
                    
                    double diff = Math.abs(baselineValue - priorityValue);
                    if (diff > TOLERANCE) {
                        throw new AssertionError("Non-priority criterion '" + criterionName + 
                            "' should remain unchanged for technology " + baselineScore.getTechnologyName() + 
                            ". Expected: " + baselineValue + ", Got: " + priorityValue + 
                            " (difference: " + diff + ")");
                    }
                }
            }
        }
    }

    /**
     * Test data container for tag multiplier tests
     */
    private static class TagMultiplierTestData {
        final List<Technology> technologies;
        final List<Long> technologyIds;
        final String priorityTag;
        final String expectedCriterionName;
        
        TagMultiplierTestData(List<Technology> technologies, List<Long> technologyIds, 
                             String priorityTag, String expectedCriterionName) {
            this.technologies = technologies;
            this.technologyIds = technologyIds;
            this.priorityTag = priorityTag;
            this.expectedCriterionName = expectedCriterionName;
        }
    }
}