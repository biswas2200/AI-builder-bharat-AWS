package com.devdecision.insight.internal;

import com.devdecision.referee.domain.UserConstraints;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Engine for generating structured recommendations and trade-off analysis.
 * Provides rule-based analysis to complement AI-generated insights.
 */
@Component
public class RecommendationEngine {
    
    private static final Logger log = LoggerFactory.getLogger(RecommendationEngine.class);
    
    private final Map<String, TechnologyProfile> technologyProfiles;
    
    public RecommendationEngine() {
        this.technologyProfiles = initializeTechnologyProfiles();
    }

    /**
     * Generate personalized recommendation based on user constraints
     */
    public String generatePersonalizedRecommendation(List<String> technologyNames, 
                                                   UserConstraints constraints, 
                                                   String baseRecommendation) {
        StringBuilder recommendation = new StringBuilder();
        
        if (baseRecommendation != null && !baseRecommendation.trim().isEmpty()) {
            recommendation.append(baseRecommendation).append("\n\n");
        }
        
        recommendation.append("**Personalized Analysis:**\n");
        
        // Analyze based on priority tags
        if (constraints != null && !constraints.priorityTags().isEmpty()) {
            recommendation.append("Based on your priorities (")
                .append(String.join(", ", constraints.priorityTags()))
                .append("), here's what to consider:\n\n");
            
            for (String priority : constraints.priorityTags()) {
                String priorityAnalysis = analyzePriorityForTechnologies(priority, technologyNames);
                if (!priorityAnalysis.isEmpty()) {
                    recommendation.append("• **").append(priority.toUpperCase()).append("**: ")
                        .append(priorityAnalysis).append("\n");
                }
            }
            
            recommendation.append("\n");
        }
        
        // Project context analysis
        if (constraints != null) {
            if (constraints.projectType() != null) {
                String projectAnalysis = analyzeForProjectType(constraints.projectType(), technologyNames);
                if (!projectAnalysis.isEmpty()) {
                    recommendation.append("**Project Type Considerations (")
                        .append(constraints.projectType()).append("):**\n")
                        .append(projectAnalysis).append("\n\n");
                }
            }
            
            if (constraints.teamSize() != null) {
                String teamAnalysis = analyzeForTeamSize(constraints.teamSize(), technologyNames);
                if (!teamAnalysis.isEmpty()) {
                    recommendation.append("**Team Size Considerations (")
                        .append(constraints.teamSize()).append("):**\n")
                        .append(teamAnalysis).append("\n\n");
                }
            }
            
            if (constraints.timeline() != null) {
                String timelineAnalysis = analyzeForTimeline(constraints.timeline(), technologyNames);
                if (!timelineAnalysis.isEmpty()) {
                    recommendation.append("**Timeline Considerations (")
                        .append(constraints.timeline()).append("):**\n")
                        .append(timelineAnalysis).append("\n\n");
                }
            }
        }
        
        // Final recommendation
        String finalRecommendation = generateFinalRecommendation(technologyNames, constraints);
        recommendation.append("**Final Recommendation:**\n").append(finalRecommendation);
        
        return recommendation.toString();
    }
    
    /**
     * Generate structured trade-off analysis
     */
    public String generateStructuredTradeOffAnalysis(List<String> technologyNames) {
        StringBuilder analysis = new StringBuilder();
        analysis.append("**Structured Trade-off Analysis:**\n\n");
        
        // Performance vs Development Speed
        analysis.append("**Performance vs Development Speed:**\n");
        for (String tech : technologyNames) {
            TechnologyProfile profile = technologyProfiles.get(tech.toLowerCase());
            if (profile != null) {
                analysis.append("• ").append(tech).append(": ")
                    .append(profile.getPerformanceVsSpeedAnalysis()).append("\n");
            }
        }
        analysis.append("\n");
        
        // Learning Curve vs Long-term Benefits
        analysis.append("**Learning Curve vs Long-term Benefits:**\n");
        for (String tech : technologyNames) {
            TechnologyProfile profile = technologyProfiles.get(tech.toLowerCase());
            if (profile != null) {
                analysis.append("• ").append(tech).append(": ")
                    .append(profile.getLearningCurveAnalysis()).append("\n");
            }
        }
        analysis.append("\n");
        
        // Community vs Innovation
        analysis.append("**Community Support vs Innovation:**\n");
        for (String tech : technologyNames) {
            TechnologyProfile profile = technologyProfiles.get(tech.toLowerCase());
            if (profile != null) {
                analysis.append("• ").append(tech).append(": ")
                    .append(profile.getCommunityVsInnovationAnalysis()).append("\n");
            }
        }
        
        return analysis.toString();
    }
    
    /**
     * Generate analysis for a single technology
     */
    public String generateSingleTechnologyAnalysis(String technologyName) {
        StringBuilder analysis = new StringBuilder();
        analysis.append("**Technology Analysis for ").append(technologyName).append(":**\n\n");
        
        TechnologyProfile profile = technologyProfiles.get(technologyName.toLowerCase());
        
        if (profile != null) {
            analysis.append("**Key Considerations:**\n");
            analysis.append("• **Performance**: ").append(profile.getPerformanceVsSpeedAnalysis()).append("\n");
            analysis.append("• **Learning Curve**: ").append(profile.getLearningCurveAnalysis()).append("\n");
            analysis.append("• **Community & Innovation**: ").append(profile.getCommunityVsInnovationAnalysis()).append("\n\n");
            
            analysis.append("**Decision Factors:**\n");
            analysis.append("• Evaluate if this technology aligns with your team's current expertise\n");
            analysis.append("• Consider the long-term maintenance and support requirements\n");
            analysis.append("• Assess how well it fits your project's specific requirements\n");
            analysis.append("• Review the ecosystem and available tooling\n\n");
            
            analysis.append("**Recommendation**: ");
            analysis.append(technologyName).append(" can be a solid choice for your project. ");
            analysis.append("Consider your team's expertise, project requirements, and long-term goals when making the final decision.");
        } else {
            // Fallback for unknown technologies
            analysis.append("**Key Considerations:**\n");
            analysis.append("• **Performance**: Evaluate runtime performance benchmarks for your specific use case\n");
            analysis.append("• **Learning Curve**: Assess the time investment needed for team training and productivity\n");
            analysis.append("• **Community**: Research community size, activity, and long-term support prospects\n\n");
            
            analysis.append("**Decision Factors:**\n");
            analysis.append("• Research the technology's maturity and stability\n");
            analysis.append("• Evaluate available documentation and learning resources\n");
            analysis.append("• Consider the ecosystem and third-party integrations\n");
            analysis.append("• Assess the technology's roadmap and future prospects\n\n");
            
            analysis.append("**Recommendation**: ");
            analysis.append("Conduct thorough research on ").append(technologyName);
            analysis.append(" to ensure it meets your project requirements and team capabilities. ");
            analysis.append("Consider running a small proof-of-concept to validate the technology choice.");
        }
        
        return analysis.toString();
    }
    
    /**
     * Generate contextual questions based on technologies and constraints
     */
    public List<String> generateContextualQuestions(List<String> technologyNames, UserConstraints constraints) {
        List<String> questions = new ArrayList<>();
        
        // Technology-specific questions
        Set<String> categories = technologyNames.stream()
            .map(tech -> {
                TechnologyProfile profile = technologyProfiles.get(tech.toLowerCase());
                return profile != null ? profile.getCategory() : "general";
            })
            .collect(Collectors.toSet());
        
        if (categories.contains("frontend")) {
            questions.add("How important is SEO and server-side rendering for your application?");
            questions.add("Do you need mobile app development capabilities?");
            questions.add("What is your team's JavaScript/TypeScript experience level?");
        }
        
        if (categories.contains("backend")) {
            questions.add("What are your expected concurrent user loads?");
            questions.add("Do you need microservices architecture support?");
            questions.add("How important is type safety in your backend code?");
        }
        
        if (categories.contains("database")) {
            questions.add("Do you need ACID compliance for transactions?");
            questions.add("What is your expected data volume and growth rate?");
            questions.add("Do you need complex querying capabilities?");
        }
        
        // Constraint-specific questions
        if (constraints != null) {
            if (constraints.priorityTags().contains("performance")) {
                questions.add("What are your specific performance requirements (latency, throughput)?");
                questions.add("Are you willing to trade development time for performance gains?");
            }
            
            if (constraints.priorityTags().contains("learning-curve")) {
                questions.add("How much time can your team invest in learning new technologies?");
                questions.add("Do you have access to training resources or mentorship?");
            }
            
            if (constraints.priorityTags().contains("community")) {
                questions.add("How important is having local community support and meetups?");
                questions.add("Do you need enterprise support and consulting services?");
            }
        }
        
        return questions;
    }
    
    /**
     * Combine and deduplicate question lists
     */
    public List<String> combineAndDeduplicateQuestions(List<String> aiQuestions, List<String> contextQuestions) {
        Set<String> uniqueQuestions = new LinkedHashSet<>();
        
        // Add AI questions first (they're usually more comprehensive)
        if (aiQuestions != null) {
            uniqueQuestions.addAll(aiQuestions);
        }
        
        // Add context questions, avoiding duplicates
        if (contextQuestions != null) {
            for (String question : contextQuestions) {
                boolean isDuplicate = uniqueQuestions.stream()
                    .anyMatch(existing -> areSimilarQuestions(existing, question));
                
                if (!isDuplicate) {
                    uniqueQuestions.add(question);
                }
            }
        }
        
        return new ArrayList<>(uniqueQuestions);
    }
    
    /**
     * Analyze priority for technologies
     */
    private String analyzePriorityForTechnologies(String priority, List<String> technologyNames) {
        return switch (priority.toLowerCase()) {
            case "performance" -> analyzePerformancePriority(technologyNames);
            case "learning-curve", "learning_curve" -> analyzeLearningCurvePriority(technologyNames);
            case "community" -> analyzeCommunityPriority(technologyNames);
            case "documentation" -> analyzeDocumentationPriority(technologyNames);
            case "scalability" -> analyzeScalabilityPriority(technologyNames);
            default -> "";
        };
    }
    
    private String analyzePerformancePriority(List<String> technologies) {
        StringBuilder analysis = new StringBuilder();
        analysis.append("For performance-critical applications, consider: ");
        
        List<String> highPerformance = technologies.stream()
            .filter(tech -> {
                TechnologyProfile profile = technologyProfiles.get(tech.toLowerCase());
                return profile != null && profile.getPerformanceScore() > 85;
            })
            .toList();
        
        if (!highPerformance.isEmpty()) {
            analysis.append(String.join(", ", highPerformance))
                .append(" offer excellent performance characteristics.");
        } else {
            analysis.append("evaluate runtime performance benchmarks for your specific use case.");
        }
        
        return analysis.toString();
    }
    
    private String analyzeLearningCurvePriority(List<String> technologies) {
        StringBuilder analysis = new StringBuilder();
        analysis.append("For teams prioritizing quick adoption: ");
        
        List<String> easyToLearn = technologies.stream()
            .filter(tech -> {
                TechnologyProfile profile = technologyProfiles.get(tech.toLowerCase());
                return profile != null && profile.getLearningCurveScore() > 80;
            })
            .toList();
        
        if (!easyToLearn.isEmpty()) {
            analysis.append(String.join(", ", easyToLearn))
                .append(" have gentler learning curves and faster onboarding.");
        } else {
            analysis.append("consider the time investment needed for team training and productivity ramp-up.");
        }
        
        return analysis.toString();
    }
    
    private String analyzeCommunityPriority(List<String> technologies) {
        return "Strong community support ensures better long-term viability, extensive resources, and faster issue resolution.";
    }
    
    private String analyzeDocumentationPriority(List<String> technologies) {
        return "Good documentation reduces development time and helps with team onboarding and maintenance.";
    }
    
    private String analyzeScalabilityPriority(List<String> technologies) {
        return "Consider both horizontal and vertical scaling capabilities, as well as performance under load.";
    }
    
    private String analyzeForProjectType(String projectType, List<String> technologies) {
        return switch (projectType.toLowerCase()) {
            case "web-app", "web_app" -> "For web applications, prioritize SEO capabilities, performance, and development velocity.";
            case "mobile-app", "mobile_app" -> "Consider cross-platform capabilities and native performance requirements.";
            case "enterprise" -> "Focus on stability, security, long-term support, and integration capabilities.";
            case "startup" -> "Prioritize development speed, cost-effectiveness, and ability to pivot quickly.";
            case "api" -> "Focus on performance, scalability, and ease of integration with other systems.";
            default -> "Consider how each technology aligns with your specific project requirements.";
        };
    }
    
    private String analyzeForTeamSize(String teamSize, List<String> technologies) {
        return switch (teamSize.toLowerCase()) {
            case "small", "1-5" -> "Small teams benefit from technologies with good defaults, minimal configuration, and strong tooling.";
            case "medium", "5-20" -> "Medium teams can handle more complex technologies and benefit from modular architectures.";
            case "large", "20+" -> "Large teams need technologies that support good separation of concerns and parallel development.";
            default -> "Consider how the technology choice affects team coordination and development workflow.";
        };
    }
    
    private String analyzeForTimeline(String timeline, List<String> technologies) {
        return switch (timeline.toLowerCase()) {
            case "urgent", "weeks" -> "For urgent timelines, prioritize technologies your team already knows or has gentle learning curves.";
            case "normal", "months" -> "Normal timelines allow for some learning investment and more thorough technology evaluation.";
            case "long-term", "long_term" -> "Long-term projects can invest in technologies with steeper learning curves but better long-term benefits.";
            default -> "Consider how the timeline affects your ability to learn new technologies and deliver features.";
        };
    }
    
    private String generateFinalRecommendation(List<String> technologies, UserConstraints constraints) {
        if (technologies.size() == 1) {
            return String.format("%s appears to be a solid choice for your requirements. " +
                "Ensure your team has the necessary expertise or training plan in place.", technologies.get(0));
        }
        
        StringBuilder recommendation = new StringBuilder();
        recommendation.append("Based on the analysis above:\n\n");
        
        if (constraints != null && !constraints.priorityTags().isEmpty()) {
            String topPriority = constraints.priorityTags().iterator().next();
            recommendation.append("Given your top priority of **").append(topPriority)
                .append("**, focus on technologies that excel in this area while meeting your other requirements.\n\n");
        }
        
        recommendation.append("Consider running a small proof-of-concept with your top 2-3 choices to validate ")
            .append("the decision with real-world constraints and team dynamics.");
        
        return recommendation.toString();
    }
    
    /**
     * Check if two questions are similar enough to be considered duplicates
     */
    private boolean areSimilarQuestions(String q1, String q2) {
        // Simple similarity check - could be enhanced with more sophisticated NLP
        String normalized1 = q1.toLowerCase().replaceAll("[^a-z0-9\\s]", "");
        String normalized2 = q2.toLowerCase().replaceAll("[^a-z0-9\\s]", "");
        
        Set<String> words1 = Set.of(normalized1.split("\\s+"));
        Set<String> words2 = Set.of(normalized2.split("\\s+"));
        
        Set<String> intersection = new HashSet<>(words1);
        intersection.retainAll(words2);
        
        Set<String> union = new HashSet<>(words1);
        union.addAll(words2);
        
        // Consider similar if they share more than 60% of words
        return (double) intersection.size() / union.size() > 0.6;
    }
    
    /**
     * Initialize technology profiles for analysis
     */
    private Map<String, TechnologyProfile> initializeTechnologyProfiles() {
        Map<String, TechnologyProfile> profiles = new HashMap<>();
        
        profiles.put("react", new TechnologyProfile(
            "frontend", 85, 70, 95,
            "High performance with virtual DOM, but requires optimization knowledge",
            "Steep initial learning curve, but extensive resources available",
            "Massive community with Facebook backing, very innovative"
        ));
        
        profiles.put("vue", new TechnologyProfile(
            "frontend", 88, 85, 80,
            "Excellent performance with reactive system and good defaults",
            "Gentle learning curve with progressive adoption possible",
            "Growing community, good balance of stability and innovation"
        ));
        
        profiles.put("angular", new TechnologyProfile(
            "frontend", 82, 60, 85,
            "Good performance but can be heavy, requires careful optimization",
            "Steep learning curve due to comprehensive framework approach",
            "Strong enterprise community, Google backing, regular innovation cycles"
        ));
        
        return profiles;
    }
    
    /**
     * Internal class to hold technology profile data
     */
    private static class TechnologyProfile {
        private final String category;
        private final int performanceScore;
        private final int learningCurveScore;
        private final int communityScore;
        private final String performanceVsSpeedAnalysis;
        private final String learningCurveAnalysis;
        private final String communityVsInnovationAnalysis;
        
        public TechnologyProfile(String category, int performanceScore, int learningCurveScore, 
                               int communityScore, String performanceVsSpeedAnalysis,
                               String learningCurveAnalysis, String communityVsInnovationAnalysis) {
            this.category = category;
            this.performanceScore = performanceScore;
            this.learningCurveScore = learningCurveScore;
            this.communityScore = communityScore;
            this.performanceVsSpeedAnalysis = performanceVsSpeedAnalysis;
            this.learningCurveAnalysis = learningCurveAnalysis;
            this.communityVsInnovationAnalysis = communityVsInnovationAnalysis;
        }
        
        // Getters
        public String getCategory() { return category; }
        public int getPerformanceScore() { return performanceScore; }
        public int getLearningCurveScore() { return learningCurveScore; }
        public int getCommunityScore() { return communityScore; }
        public String getPerformanceVsSpeedAnalysis() { return performanceVsSpeedAnalysis; }
        public String getLearningCurveAnalysis() { return learningCurveAnalysis; }
        public String getCommunityVsInnovationAnalysis() { return communityVsInnovationAnalysis; }
    }
}