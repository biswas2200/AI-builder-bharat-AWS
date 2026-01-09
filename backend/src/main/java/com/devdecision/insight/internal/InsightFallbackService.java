package com.devdecision.insight.internal;

import com.devdecision.insight.domain.InsightAnalysis;
import com.devdecision.inventory.domain.Technology;
import com.devdecision.referee.domain.ComparisonResult;
import com.devdecision.referee.domain.KpiMetric;
import com.devdecision.referee.domain.RadarChartData;
import com.devdecision.referee.domain.TechnologyScore;
import com.devdecision.referee.domain.UserConstraints;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Fallback service that provides mock data when Gemini AI fails.
 * Ensures the UI never breaks due to AI service failures.
 */
@Component
public class InsightFallbackService {
    
    private static final Logger log = LoggerFactory.getLogger(InsightFallbackService.class);
    
    private final Map<String, TechnologyFallbackData> fallbackDatabase;
    
    public InsightFallbackService() {
        this.fallbackDatabase = initializeFallbackData();
    }

    /**
     * Get fallback comparison result when Gemini fails
     */
    public ComparisonResult getFallbackData(List<String> technologies) {
        log.info("Generating fallback data for technologies: {}", technologies);
        
        List<TechnologyScore> scores = generateMockTechnologyScores(technologies);
        List<RadarChartData> radarData = generateMockRadarData(technologies);
        Map<String, List<KpiMetric>> kpiMetrics = generateMockKpiMetrics(technologies);
        String recommendation = generateMockRecommendation(technologies);
        
        return ComparisonResult.builder()
            .scores(scores)
            .radarData(radarData)
            .kpiMetrics(kpiMetrics)
            .recommendationSummary(recommendation)
            .build();
    }
    
    /**
     * Get fallback insights for individual technologies
     */
    public List<InsightAnalysis> getFallbackInsights(List<String> technologies) {
        return technologies.stream()
            .map(this::getFallbackInsightForTechnology)
            .toList();
    }
    
    /**
     * Get fallback trade-off analysis
     */
    public String getFallbackTradeOffAnalysis(List<String> technologies) {
        if (technologies.size() == 1) {
            return getFallbackSingleTechnologyAnalysis(technologies.get(0));
        }
        
        StringBuilder analysis = new StringBuilder();
        analysis.append("Key Trade-offs to Consider:\n\n");
        
        analysis.append("• Performance vs Development Speed: ");
        analysis.append("Consider whether you need maximum performance or faster development cycles.\n\n");
        
        analysis.append("• Learning Curve vs Long-term Benefits: ");
        analysis.append("Evaluate the time investment required against long-term productivity gains.\n\n");
        
        analysis.append("• Community Support vs Innovation: ");
        analysis.append("Balance established community support with cutting-edge features.\n\n");
        
        analysis.append("• Scalability vs Simplicity: ");
        analysis.append("Consider future growth needs against current simplicity requirements.\n\n");
        
        analysis.append("Recommendation: Evaluate these factors based on your specific project requirements, ");
        analysis.append("team expertise, and long-term goals.");
        
        return analysis.toString();
    }
    
    /**
     * Get fallback analysis for a single technology
     */
    private String getFallbackSingleTechnologyAnalysis(String technology) {
        StringBuilder analysis = new StringBuilder();
        analysis.append("Technology Analysis for ").append(technology).append(":\n\n");
        
        analysis.append("Key Considerations:\n");
        analysis.append("• Performance: Evaluate how well this technology meets your performance requirements\n");
        analysis.append("• Learning Curve: Consider your team's current expertise and time available for learning\n");
        analysis.append("• Community Support: Research the size and activity of the community\n");
        analysis.append("• Documentation: Assess the quality and completeness of available documentation\n");
        analysis.append("• Ecosystem: Review available tools, libraries, and integrations\n\n");
        
        analysis.append("Decision Factors:\n");
        analysis.append("• Does this technology align with your project requirements?\n");
        analysis.append("• Can your team effectively use and maintain it?\n");
        analysis.append("• Is there adequate long-term support and community?\n");
        analysis.append("• How does it integrate with your existing technology stack?\n\n");
        
        analysis.append("Recommendation: ").append(technology).append(" can be a good choice if it aligns with your ");
        analysis.append("team's expertise and project needs. Consider running a small proof-of-concept to validate ");
        analysis.append("the technology choice before full commitment.");
        
        return analysis.toString();
    }
    
    /**
     * Get fallback decision questions
     */
    public List<String> getFallbackDecisionQuestions(List<String> technologies, UserConstraints constraints) {
        List<String> questions = new ArrayList<>();
        
        questions.add("What is your team's current experience level with these technologies?");
        questions.add("How important is performance versus development speed for your project?");
        questions.add("What is your project timeline and budget constraints?");
        questions.add("How critical is long-term maintenance and support?");
        questions.add("Do you need to integrate with existing systems or technologies?");
        questions.add("What is the expected scale and growth of your application?");
        
        if (constraints != null && !constraints.priorityTags().isEmpty()) {
            questions.add("How do these technologies align with your priority areas: " + 
                String.join(", ", constraints.priorityTags()) + "?");
        }
        
        return questions;
    }

    /**
     * Generate mock technology scores
     */
    private List<TechnologyScore> generateMockTechnologyScores(List<String> technologies) {
        List<TechnologyScore> scores = new ArrayList<>();
        
        for (int i = 0; i < technologies.size(); i++) {
            String techName = technologies.get(i);
            Technology mockTechnology = createMockTechnology(techName, i + 1);
            
            TechnologyFallbackData fallbackData = fallbackDatabase.get(techName.toLowerCase());
            Map<String, Double> criterionScores = new HashMap<>();
            double overallScore;
            
            if (fallbackData != null) {
                criterionScores = fallbackData.getCriterionScores();
                overallScore = criterionScores.values().stream()
                    .mapToDouble(Double::doubleValue)
                    .average()
                    .orElse(75.0);
            } else {
                // Generate reasonable scores for unknown technologies
                criterionScores.put("Performance", 70.0 + (Math.random() * 20.0));
                criterionScores.put("Learning Curve", 65.0 + (Math.random() * 25.0));
                criterionScores.put("Community", 60.0 + (Math.random() * 30.0));
                criterionScores.put("Documentation", 70.0 + (Math.random() * 20.0));
                criterionScores.put("Scalability", 65.0 + (Math.random() * 25.0));
                
                overallScore = criterionScores.values().stream()
                    .mapToDouble(Double::doubleValue)
                    .average()
                    .orElse(75.0);
            }
            
            String explanation = String.format("Mock analysis for %s: This technology shows balanced performance across criteria.", techName);
            TechnologyScore score = new TechnologyScore(mockTechnology, overallScore, criterionScores, explanation);
            scores.add(score);
        }
        
        return scores;
    }
    
    /**
     * Create a mock Technology entity for testing
     */
    private Technology createMockTechnology(String name, long id) {
        Technology technology = new Technology(name, "Mock Category");
        technology.setDescription("Mock technology for testing: " + name);
        
        // Use reflection to set the ID for testing purposes
        try {
            var idField = Technology.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(technology, id);
        } catch (Exception e) {
            log.warn("Could not set mock technology ID: {}", e.getMessage());
        }
        
        // Add some mock metrics
        Map<String, Double> metrics = new HashMap<>();
        metrics.put("github_stars", 10000.0 + (Math.random() * 50000.0));
        metrics.put("npm_downloads", 100000.0 + (Math.random() * 1000000.0));
        metrics.put("job_openings", 500.0 + (Math.random() * 5000.0));
        metrics.put("satisfaction_score", 3.5 + (Math.random() * 1.5));
        technology.setMetrics(metrics);
        
        // Add some mock tags
        Set<String> tags = new HashSet<>();
        tags.add("mock");
        tags.add("testing");
        tags.add(name.toLowerCase().replaceAll("[^a-z0-9]", ""));
        technology.setTags(tags);
        
        return technology;
    }

    /**
     * Generate mock radar chart data
     */
    private List<RadarChartData> generateMockRadarData(List<String> technologies) {
        List<String> criteria = List.of("Performance", "Learning Curve", "Community", "Documentation", "Scalability");
        List<RadarChartData> radarData = new ArrayList<>();
        
        for (String criterion : criteria) {
            List<Double> scores = new ArrayList<>();
            
            for (String tech : technologies) {
                TechnologyFallbackData fallbackData = fallbackDatabase.get(tech.toLowerCase());
                if (fallbackData != null) {
                    scores.add(fallbackData.getScoreForCriterion(criterion));
                } else {
                    // Generate reasonable random score for unknown technologies
                    scores.add(60.0 + (Math.random() * 30.0)); // 60-90 range
                }
            }
            
            // Create radar data point based on number of technologies
            RadarChartData dataPoint = switch (technologies.size()) {
                case 1 -> new RadarChartData(criterion, scores.get(0), null, 100.0);
                case 2 -> new RadarChartData(criterion, scores.get(0), scores.get(1), 100.0);
                case 3 -> new RadarChartData(criterion, scores.get(0), scores.get(1), scores.get(2), 100.0);
                case 4 -> new RadarChartData(criterion, scores.get(0), scores.get(1), scores.get(2), scores.get(3), 100.0);
                default -> new RadarChartData(criterion, scores.get(0), scores.get(1), scores.get(2), scores.get(3), scores.get(4), 100.0);
            };
            
            radarData.add(dataPoint);
        }
        
        return radarData;
    }
    
    /**
     * Generate mock KPI metrics
     */
    private Map<String, List<KpiMetric>> generateMockKpiMetrics(List<String> technologies) {
        Map<String, List<KpiMetric>> kpiMetrics = new HashMap<>();
        
        for (String tech : technologies) {
            List<KpiMetric> metrics = new ArrayList<>();
            TechnologyFallbackData fallbackData = fallbackDatabase.get(tech.toLowerCase());
            
            if (fallbackData != null) {
                metrics.addAll(fallbackData.getKpiMetrics());
            } else {
                // Generate default metrics for unknown technologies
                metrics.add(new KpiMetric("GitHub Stars", 15000, "15K", "stars"));
                metrics.add(new KpiMetric("NPM Downloads", 500000, "500K", "weekly"));
                metrics.add(new KpiMetric("Job Openings", 1200, "1.2K", "positions"));
                metrics.add(new KpiMetric("Satisfaction", 4.2, "4.2/5", "rating"));
            }
            
            kpiMetrics.put(tech, metrics);
        }
        
        return kpiMetrics;
    }
    
    /**
     * Generate mock recommendation
     */
    private String generateMockRecommendation(List<String> technologies) {
        if (technologies.isEmpty()) {
            return "No technologies selected for comparison.";
        }
        
        if (technologies.size() == 1) {
            return String.format("%s is a solid choice for most projects. Consider your team's expertise and project requirements when making the final decision.", 
                technologies.get(0));
        }
        
        return String.format("Based on the comparison of %s, each technology has its strengths. " +
            "Consider your specific project requirements, team expertise, and long-term goals when making your decision. " +
            "The analysis above highlights key differences to help guide your choice.",
            String.join(", ", technologies));
    }
    
    /**
     * Get fallback insight for a single technology
     */
    private InsightAnalysis getFallbackInsightForTechnology(String technology) {
        TechnologyFallbackData fallbackData = fallbackDatabase.get(technology.toLowerCase());
        
        if (fallbackData != null) {
            return new InsightAnalysis(
                technology,
                fallbackData.getStrengths(),
                fallbackData.getWeaknesses(),
                fallbackData.getRecommendation(),
                fallbackData.getUseCase(),
                null
            );
        }
        
        // Default insight for unknown technologies
        return new InsightAnalysis(
            technology,
            List.of("Established technology", "Active development", "Community support"),
            List.of("Learning curve may vary", "Consider long-term support"),
            "Evaluate based on your specific project needs and team expertise",
            "General purpose development",
            null
        );
    }
    
    /**
     * Initialize fallback data for common technologies
     */
    private Map<String, TechnologyFallbackData> initializeFallbackData() {
        Map<String, TechnologyFallbackData> data = new HashMap<>();
        
        // React
        data.put("react", new TechnologyFallbackData(
            Map.of(
                "Performance", 85.0,
                "Learning Curve", 70.0,
                "Community", 95.0,
                "Documentation", 90.0,
                "Scalability", 88.0
            ),
            List.of("Large ecosystem", "Strong community", "Flexible architecture", "Excellent tooling"),
            List.of("Steep learning curve", "Frequent updates", "JSX syntax"),
            "Excellent for large-scale web applications with complex UIs",
            "Enterprise web applications, SPAs, mobile apps with React Native",
            List.of(
                new KpiMetric("GitHub Stars", 220000, "220K", "stars"),
                new KpiMetric("NPM Downloads", 20000000, "20M", "weekly"),
                new KpiMetric("Job Openings", 15000, "15K", "positions"),
                new KpiMetric("Satisfaction", 4.5, "4.5/5", "rating")
            )
        ));
        
        // Vue
        data.put("vue", new TechnologyFallbackData(
            Map.of(
                "Performance", 88.0,
                "Learning Curve", 85.0,
                "Community", 80.0,
                "Documentation", 95.0,
                "Scalability", 82.0
            ),
            List.of("Easy to learn", "Excellent documentation", "Progressive framework", "Great performance"),
            List.of("Smaller ecosystem", "Less job market", "Composition API learning curve"),
            "Perfect for teams wanting a balance of simplicity and power",
            "Medium to large web applications, progressive enhancement",
            List.of(
                new KpiMetric("GitHub Stars", 207000, "207K", "stars"),
                new KpiMetric("NPM Downloads", 4200000, "4.2M", "weekly"),
                new KpiMetric("Job Openings", 3500, "3.5K", "positions"),
                new KpiMetric("Satisfaction", 4.6, "4.6/5", "rating")
            )
        ));
        
        // Angular
        data.put("angular", new TechnologyFallbackData(
            Map.of(
                "Performance", 82.0,
                "Learning Curve", 60.0,
                "Community", 85.0,
                "Documentation", 88.0,
                "Scalability", 92.0
            ),
            List.of("Full framework", "TypeScript by default", "Enterprise ready", "Comprehensive tooling"),
            List.of("Steep learning curve", "Complex for small projects", "Frequent major updates"),
            "Best for large enterprise applications with complex requirements",
            "Enterprise applications, large-scale SPAs, complex business applications",
            List.of(
                new KpiMetric("GitHub Stars", 93000, "93K", "stars"),
                new KpiMetric("NPM Downloads", 3100000, "3.1M", "weekly"),
                new KpiMetric("Job Openings", 8500, "8.5K", "positions"),
                new KpiMetric("Satisfaction", 4.1, "4.1/5", "rating")
            )
        ));
        
        return data;
    }
    
    /**
     * Internal class to hold fallback data for technologies
     */
    private static class TechnologyFallbackData {
        private final Map<String, Double> criterionScores;
        private final List<String> strengths;
        private final List<String> weaknesses;
        private final String recommendation;
        private final String useCase;
        private final List<KpiMetric> kpiMetrics;
        
        public TechnologyFallbackData(Map<String, Double> criterionScores, List<String> strengths, 
                                    List<String> weaknesses, String recommendation, String useCase,
                                    List<KpiMetric> kpiMetrics) {
            this.criterionScores = new HashMap<>(criterionScores);
            this.strengths = new ArrayList<>(strengths);
            this.weaknesses = new ArrayList<>(weaknesses);
            this.recommendation = recommendation;
            this.useCase = useCase;
            this.kpiMetrics = new ArrayList<>(kpiMetrics);
        }
        
        public Double getScoreForCriterion(String criterion) {
            return criterionScores.getOrDefault(criterion, 75.0); // Default score
        }
        
        public Map<String, Double> getCriterionScores() {
            return new HashMap<>(criterionScores);
        }
        
        public List<String> getStrengths() { return new ArrayList<>(strengths); }
        public List<String> getWeaknesses() { return new ArrayList<>(weaknesses); }
        public String getRecommendation() { return recommendation; }
        public String getUseCase() { return useCase; }
        public List<KpiMetric> getKpiMetrics() { return new ArrayList<>(kpiMetrics); }
    }
}