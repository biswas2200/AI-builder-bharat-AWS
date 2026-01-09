package com.devdecision.insight.domain;

import com.devdecision.insight.api.InsightService;
import com.devdecision.referee.domain.ComparisonResult;
import com.devdecision.shared.config.QuickCheckTestBase;
import net.java.quickcheck.Generator;
import net.java.quickcheck.characteristic.AbstractCharacteristic;
import net.java.quickcheck.generator.PrimitiveGenerators;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Property-based tests for AI analysis generation
 * Feature: dev-decision, Property 12: AI Analysis Generation
 */
@SpringBootTest
@ActiveProfiles("test")
public class AIAnalysisGenerationPropertyTest extends QuickCheckTestBase {

    @Autowired
    private InsightService insightService;

    /**
     * Property 12: AI Analysis Generation
     * For any list of technology names, the insight module should generate structured analysis 
     * containing strengths, weaknesses, and explanatory content
     * **Validates: Requirements 4.1**
     */
    @Test
    void aiAnalysisGeneration() {
        Generator<List<String>> technologyNamesGen = createTechnologyNamesGenerator();
        
        qt.forAll(technologyNamesGen, new AbstractCharacteristic<List<String>>() {
            @Override
            protected void doSpecify(List<String> technologyNames) throws Throwable {
                // Generate trade-off analysis for the technology names
                String analysis = insightService.generateTradeOffAnalysis(technologyNames);
                
                // Verify the analysis contains structured explanatory content
                assertAnalysisContainsExplanatoryContent(analysis, technologyNames);
            }
        });
    }

    /**
     * Creates a generator for valid lists of technology names
     */
    private Generator<List<String>> createTechnologyNamesGenerator() {
        return new Generator<List<String>>() {
            @Override
            public List<String> next() {
                // Generate 1-5 technology names
                int count = PrimitiveGenerators.integers(1, 5).next();
                Set<String> uniqueNames = new HashSet<>();
                
                while (uniqueNames.size() < count) {
                    String techName = generateValidTechnologyName();
                    uniqueNames.add(techName);
                }
                
                return new ArrayList<>(uniqueNames);
            }
        };
    }

    /**
     * Generates a valid technology name (non-empty, reasonable length)
     */
    private String generateValidTechnologyName() {
        // Use a mix of real technology names and generated ones for better testing
        String[] realTechNames = {
            "React", "Vue", "Angular", "Node.js", "Django", "Spring Boot",
            "PostgreSQL", "MongoDB", "Redis", "Docker", "Kubernetes",
            "AWS", "Azure", "GCP", "TypeScript", "Python", "Java"
        };
        
        // 50% chance to use a real tech name, 50% chance to generate one
        if (PrimitiveGenerators.booleans().next()) {
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
     * Asserts that the analysis contains meaningful explanatory content
     */
    private void assertAnalysisContainsExplanatoryContent(String analysis, List<String> technologyNames) {
        // Verify analysis is not null or empty
        if (analysis == null || analysis.trim().isEmpty()) {
            throw new AssertionError("Analysis should not be null or empty for technology names: " + technologyNames);
        }
        
        // Verify the analysis contains meaningful content (at least 50 characters)
        String trimmedAnalysis = analysis.trim();
        if (trimmedAnalysis.length() < 50) {
            throw new AssertionError("Analysis should contain meaningful explanatory content (at least 50 characters) for technology names: " + 
                technologyNames + ", but got: '" + trimmedAnalysis + "'");
        }
        
        // Verify the analysis contains structured content indicators
        // Look for common analysis patterns like bullet points, sections, or structured text
        boolean hasStructuredContent = false;
        
        // Check for bullet points or numbered lists
        if (trimmedAnalysis.contains("•") || trimmedAnalysis.contains("*") || 
            trimmedAnalysis.matches(".*\\d+\\..*") || trimmedAnalysis.contains("-")) {
            hasStructuredContent = true;
        }
        
        // Check for section headers or structured phrases
        if (trimmedAnalysis.toLowerCase().contains("trade-off") || 
            trimmedAnalysis.toLowerCase().contains("consider") ||
            trimmedAnalysis.toLowerCase().contains("strength") ||
            trimmedAnalysis.toLowerCase().contains("weakness") ||
            trimmedAnalysis.toLowerCase().contains("recommendation") ||
            trimmedAnalysis.toLowerCase().contains("analysis")) {
            hasStructuredContent = true;
        }
        
        if (!hasStructuredContent) {
            throw new AssertionError("Analysis should contain structured explanatory content (trade-offs, recommendations, etc.) for technology names: " + 
                technologyNames + ", but got: '" + trimmedAnalysis + "'");
        }
        
        // For multiple technologies, verify the analysis mentions comparison or trade-offs
        if (technologyNames.size() > 1) {
            boolean mentionsComparison = trimmedAnalysis.toLowerCase().contains("comparison") ||
                                       trimmedAnalysis.toLowerCase().contains("compare") ||
                                       trimmedAnalysis.toLowerCase().contains("versus") ||
                                       trimmedAnalysis.toLowerCase().contains("vs") ||
                                       trimmedAnalysis.toLowerCase().contains("between") ||
                                       trimmedAnalysis.toLowerCase().contains("trade-off");
            
            if (!mentionsComparison) {
                throw new AssertionError("Analysis for multiple technologies should mention comparison or trade-offs for technology names: " + 
                    technologyNames + ", but got: '" + trimmedAnalysis + "'");
            }
        }
        
        // Verify that the analysis is contextually relevant
        // For single technology, it should provide general guidance
        if (technologyNames.size() == 1) {
            boolean providesGuidance = trimmedAnalysis.toLowerCase().contains("project") ||
                                     trimmedAnalysis.toLowerCase().contains("requirement") ||
                                     trimmedAnalysis.toLowerCase().contains("team") ||
                                     trimmedAnalysis.toLowerCase().contains("decision") ||
                                     trimmedAnalysis.toLowerCase().contains("consider");
            
            if (!providesGuidance) {
                throw new AssertionError("Analysis for single technology should provide decision guidance for technology: " + 
                    technologyNames.get(0) + ", but got: '" + trimmedAnalysis + "'");
            }
        }
    }
}