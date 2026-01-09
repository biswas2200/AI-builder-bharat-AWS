package com.devdecision.insight.domain;

import com.devdecision.insight.api.InsightService;
import com.devdecision.referee.domain.ComparisonResult;
import com.devdecision.referee.domain.UserConstraints;
import com.devdecision.shared.config.QuickCheckTestBase;
import net.java.quickcheck.Generator;
import net.java.quickcheck.characteristic.AbstractCharacteristic;
import net.java.quickcheck.generator.PrimitiveGenerators;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.*;

/**
 * Property-based tests for personalized recommendations
 * Feature: dev-decision, Property 13: Personalized Recommendations
 */
@SpringBootTest
@ActiveProfiles("test")
public class PersonalizedRecommendationsPropertyTest extends QuickCheckTestBase {

    @Autowired
    private InsightService insightService;

    /**
     * Property 13: Personalized Recommendations
     * For any comparison with user constraints, the system should generate recommendations 
     * that reference the specified constraints and selected technologies
     * **Validates: Requirements 4.2**
     */
    @Test
    void personalizedRecommendations() {
        Generator<TestData> testDataGen = createTestDataGenerator();
        
        qt.forAll(testDataGen, new AbstractCharacteristic<TestData>() {
            @Override
            protected void doSpecify(TestData testData) throws Throwable {
                // Generate personalized insights for the technology names and constraints
                ComparisonResult result = insightService.generatePersonalizedInsights(
                    testData.technologyNames, testData.constraints);
                
                // Verify the result contains personalized recommendations
                assertPersonalizedRecommendations(result, testData.technologyNames, testData.constraints);
            }
        });
    }

    /**
     * Creates a generator for test data containing technology names and user constraints
     */
    private Generator<TestData> createTestDataGenerator() {
        return new Generator<TestData>() {
            @Override
            public TestData next() {
                List<String> technologyNames = generateTechnologyNames();
                UserConstraints constraints = generateUserConstraints();
                return new TestData(technologyNames, constraints);
            }
        };
    }

    /**
     * Generates a valid list of technology names (1-5 technologies)
     */
    private List<String> generateTechnologyNames() {
        int count = PrimitiveGenerators.integers(1, 5).next();
        Set<String> uniqueNames = new HashSet<>();
        
        while (uniqueNames.size() < count) {
            String techName = generateValidTechnologyName();
            uniqueNames.add(techName);
        }
        
        return new ArrayList<>(uniqueNames);
    }

    /**
     * Generates a valid technology name
     */
    private String generateValidTechnologyName() {
        String[] realTechNames = {
            "React", "Vue", "Angular", "Node.js", "Django", "Spring Boot",
            "PostgreSQL", "MongoDB", "Redis", "Docker", "Kubernetes",
            "AWS", "Azure", "GCP", "TypeScript", "Python", "Java",
            "Express", "FastAPI", "Laravel", "Ruby on Rails", "Flask"
        };
        
        // 70% chance to use a real tech name for better testing
        if (PrimitiveGenerators.integers(1, 10).next() <= 7) {
            int index = PrimitiveGenerators.integers(0, realTechNames.length - 1).next();
            return realTechNames[index];
        } else {
            String generated;
            do {
                generated = PrimitiveGenerators.strings().next();
            } while (generated == null || generated.trim().isEmpty() || generated.length() > 50);
            
            // Clean up the generated name
            generated = generated.trim().replaceAll("[^a-zA-Z0-9\\-_\\.]", "");
            if (generated.isEmpty()) {
                generated = "TestTech" + System.nanoTime() % 1000;
            }
            
            return generated;
        }
    }

    /**
     * Generates user constraints with various combinations of priority tags and context
     */
    private UserConstraints generateUserConstraints() {
        Set<String> priorityTags = generatePriorityTags();
        String projectType = generateProjectType();
        String teamSize = generateTeamSize();
        String timeline = generateTimeline();
        
        return new UserConstraints(priorityTags, projectType, teamSize, timeline);
    }

    /**
     * Generates a set of priority tags (0-4 tags)
     */
    private Set<String> generatePriorityTags() {
        String[] availableTags = {
            "performance", "learning-curve", "community", "documentation", 
            "scalability", "security", "cost", "maintenance", "popularity"
        };
        
        int tagCount = PrimitiveGenerators.integers(0, 4).next();
        Set<String> tags = new HashSet<>();
        
        while (tags.size() < tagCount && tags.size() < availableTags.length) {
            int index = PrimitiveGenerators.integers(0, availableTags.length - 1).next();
            tags.add(availableTags[index]);
        }
        
        return tags;
    }

    /**
     * Generates a project type (can be null)
     */
    private String generateProjectType() {
        String[] projectTypes = {
            "web-app", "mobile-app", "api", "microservice", "enterprise", 
            "startup", "prototype", "data-processing", null
        };
        
        int index = PrimitiveGenerators.integers(0, projectTypes.length - 1).next();
        return projectTypes[index];
    }

    /**
     * Generates a team size (can be null)
     */
    private String generateTeamSize() {
        String[] teamSizes = {
            "solo", "small", "medium", "large", "enterprise", null
        };
        
        int index = PrimitiveGenerators.integers(0, teamSizes.length - 1).next();
        return teamSizes[index];
    }

    /**
     * Generates a timeline (can be null)
     */
    private String generateTimeline() {
        String[] timelines = {
            "immediate", "weeks", "months", "long-term", "flexible", null
        };
        
        int index = PrimitiveGenerators.integers(0, timelines.length - 1).next();
        return timelines[index];
    }

    /**
     * Asserts that the comparison result contains personalized recommendations
     * that reference the specified constraints and selected technologies
     */
    private void assertPersonalizedRecommendations(ComparisonResult result, 
                                                  List<String> technologyNames, 
                                                  UserConstraints constraints) {
        // Verify result is not null
        if (result == null) {
            throw new AssertionError("ComparisonResult should not be null for technologies: " + 
                technologyNames + " with constraints: " + constraints);
        }
        
        // Verify the result has a recommendation summary
        String recommendation = result.getRecommendationSummary();
        if (recommendation == null || recommendation.trim().isEmpty()) {
            throw new AssertionError("Personalized recommendation should not be null or empty for technologies: " + 
                technologyNames + " with constraints: " + constraints);
        }
        
        String lowerRecommendation = recommendation.toLowerCase();
        
        // Verify the recommendation contains meaningful content (at least 100 characters for personalized content)
        if (recommendation.trim().length() < 100) {
            throw new AssertionError("Personalized recommendation should contain substantial content (at least 100 characters) for technologies: " + 
                technologyNames + " with constraints: " + constraints + ", but got: '" + recommendation + "'");
        }
        
        // Verify the recommendation references the selected technologies
        boolean mentionsTechnologies = false;
        for (String techName : technologyNames) {
            if (lowerRecommendation.contains(techName.toLowerCase())) {
                mentionsTechnologies = true;
                break;
            }
        }
        
        if (!mentionsTechnologies) {
            throw new AssertionError("Personalized recommendation should reference at least one of the selected technologies: " + 
                technologyNames + ", but got: '" + recommendation + "'");
        }
        
        // Verify the recommendation references user constraints when they exist
        if (!constraints.priorityTags().isEmpty()) {
            boolean mentionsPriorityTags = false;
            for (String tag : constraints.priorityTags()) {
                // Check for the tag itself or related terms
                if (lowerRecommendation.contains(tag) || 
                    (tag.equals("learning-curve") && (lowerRecommendation.contains("learning") || lowerRecommendation.contains("curve"))) ||
                    (tag.equals("performance") && lowerRecommendation.contains("performance")) ||
                    (tag.equals("community") && lowerRecommendation.contains("community")) ||
                    (tag.equals("documentation") && lowerRecommendation.contains("documentation")) ||
                    (tag.equals("scalability") && lowerRecommendation.contains("scalability")) ||
                    (tag.equals("security") && lowerRecommendation.contains("security"))) {
                    mentionsPriorityTags = true;
                    break;
                }
            }
            
            if (!mentionsPriorityTags) {
                throw new AssertionError("Personalized recommendation should reference user priority tags: " + 
                    constraints.priorityTags() + ", but got: '" + recommendation + "'");
            }
        }
        
        // Verify the recommendation references project context when available
        if (constraints.projectType() != null) {
            boolean mentionsProjectContext = lowerRecommendation.contains(constraints.projectType()) ||
                                           lowerRecommendation.contains("project") ||
                                           lowerRecommendation.contains("application") ||
                                           lowerRecommendation.contains("use case");
            
            if (!mentionsProjectContext) {
                throw new AssertionError("Personalized recommendation should reference project context when available. Project type: " + 
                    constraints.projectType() + ", but got: '" + recommendation + "'");
            }
        }
        
        // Verify the recommendation provides decision guidance
        boolean providesGuidance = lowerRecommendation.contains("recommend") ||
                                 lowerRecommendation.contains("suggest") ||
                                 lowerRecommendation.contains("consider") ||
                                 lowerRecommendation.contains("choose") ||
                                 lowerRecommendation.contains("decision") ||
                                 lowerRecommendation.contains("best") ||
                                 lowerRecommendation.contains("ideal") ||
                                 lowerRecommendation.contains("suitable");
        
        if (!providesGuidance) {
            throw new AssertionError("Personalized recommendation should provide decision guidance for technologies: " + 
                technologyNames + " with constraints: " + constraints + ", but got: '" + recommendation + "'");
        }
        
        // Verify the result preserves the user constraints
        UserConstraints resultConstraints = result.getConstraints();
        if (resultConstraints == null) {
            throw new AssertionError("ComparisonResult should preserve user constraints, but constraints were null");
        }
        
        if (!resultConstraints.equals(constraints)) {
            throw new AssertionError("ComparisonResult should preserve user constraints exactly. Expected: " + 
                constraints + ", but got: " + resultConstraints);
        }
        
        // Verify the result has the expected structure for personalized insights
        if (result.getScores() == null || result.getScores().isEmpty()) {
            throw new AssertionError("ComparisonResult should contain technology scores for technologies: " + technologyNames);
        }
        
        if (result.getRadarData() == null) {
            throw new AssertionError("ComparisonResult should contain radar chart data for visualization");
        }
        
        // For multiple technologies, verify comparative language
        if (technologyNames.size() > 1) {
            boolean hasComparativeLanguage = lowerRecommendation.contains("comparison") ||
                                           lowerRecommendation.contains("compared") ||
                                           lowerRecommendation.contains("versus") ||
                                           lowerRecommendation.contains("vs") ||
                                           lowerRecommendation.contains("between") ||
                                           lowerRecommendation.contains("while") ||
                                           lowerRecommendation.contains("whereas") ||
                                           lowerRecommendation.contains("better") ||
                                           lowerRecommendation.contains("alternative") ||
                                           lowerRecommendation.contains("differ") ||
                                           lowerRecommendation.contains("choice") ||
                                           lowerRecommendation.contains("option");
            
            if (!hasComparativeLanguage) {
                throw new AssertionError("Personalized recommendation for multiple technologies should include comparative language for technologies: " + 
                    technologyNames + ", but got: '" + recommendation + "'");
            }
        }
    }

    /**
     * Test data container for technology names and user constraints
     */
    private static class TestData {
        final List<String> technologyNames;
        final UserConstraints constraints;
        
        TestData(List<String> technologyNames, UserConstraints constraints) {
            this.technologyNames = technologyNames;
            this.constraints = constraints;
        }
        
        @Override
        public String toString() {
            return "TestData{technologies=" + technologyNames + ", constraints=" + constraints + "}";
        }
    }
}