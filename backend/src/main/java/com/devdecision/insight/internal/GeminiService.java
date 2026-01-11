package com.devdecision.insight.internal;

import com.devdecision.insight.domain.GeminiResponse;
import com.devdecision.referee.domain.ComparisonResult;
import com.devdecision.referee.domain.RadarChartData;
import com.devdecision.referee.domain.UserConstraints;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.vertexai.VertexAI;
import com.google.cloud.vertexai.api.GenerateContentResponse;
import com.google.cloud.vertexai.generativeai.GenerativeModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for integrating with Google Gemini 1.5 Flash AI model.
 * Handles prompt engineering, API calls, and response parsing.
 */
@Service
public class GeminiService {
    
    private static final Logger log = LoggerFactory.getLogger(GeminiService.class);
    
    private final VertexAI vertexAI;
    private final GenerativeModel model;
    private final ObjectMapper objectMapper;
    private final InsightFallbackService fallbackService;
    private final String projectId;
    private final String location;
    private final String credentialsPath;

    public GeminiService(@Qualifier("geminiObjectMapper") ObjectMapper objectMapper, 
                        InsightFallbackService fallbackService,
                        @Value("${google.cloud.project-id:ai-refree-aws}") String projectId,
                        @Value("${google.cloud.location:us-central1}") String location,
                        @Value("${google.cloud.credentials:ai-refree-aws-814936425b85.json}") String credentialsPath) {
        this.objectMapper = objectMapper;
        this.fallbackService = fallbackService;
        this.projectId = projectId;
        this.location = location;
        this.credentialsPath = credentialsPath;
        
        // Set the credentials path as environment variable if not already set
        if (System.getenv("GOOGLE_APPLICATION_CREDENTIALS") == null) {
            System.setProperty("GOOGLE_APPLICATION_CREDENTIALS", credentialsPath);
        }
        
        this.vertexAI = new VertexAI(projectId, location);
        this.model = new GenerativeModel("gemini-1.5-flash", vertexAI);
    }

    /**
     * Generate AI insights for technology comparison
     */
    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public ComparisonResult generateInsights(List<String> technologyNames) throws Exception {
        try {
            String prompt = buildAnalysisPrompt(technologyNames, null);
            GenerateContentResponse response = model.generateContent(prompt);
            return parseJsonResponse(response.getCandidates(0).getContent().getParts(0).getText());
        } catch (Exception e) {
            log.warn("Gemini API attempt failed for technologies: {}, error: {}", technologyNames, e.getMessage());
            throw e; // Retry will handle this
        }
    }
    
    /**
     * Recover method for generateInsights when all retries fail
     */
    @Recover
    public ComparisonResult recoverFromInsightsFailure(Exception ex, List<String> technologyNames) {
        log.error("All Gemini API attempts failed for insights, using fallback data", ex);
        return fallbackService.getFallbackData(technologyNames);
    }
    
    /**
     * Generate personalized insights based on user constraints
     */
    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public ComparisonResult generatePersonalizedInsights(List<String> technologyNames, UserConstraints constraints) throws Exception {
        try {
            String prompt = buildAnalysisPrompt(technologyNames, constraints);
            GenerateContentResponse response = model.generateContent(prompt);
            return parseJsonResponse(response.getCandidates(0).getContent().getParts(0).getText());
        } catch (Exception e) {
            log.warn("Gemini API attempt failed for personalized insights: {}, error: {}", technologyNames, e.getMessage());
            throw e; // Retry will handle this
        }
    }
    
    /**
     * Recover method for generatePersonalizedInsights when all retries fail
     */
    @Recover
    public ComparisonResult recoverFromPersonalizedInsightsFailure(Exception ex, List<String> technologyNames, UserConstraints constraints) {
        log.error("All Gemini API attempts failed for personalized insights, using fallback data", ex);
        return fallbackService.getFallbackData(technologyNames);
    }
    
    /**
     * Generate trade-off analysis between technologies
     */
    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public String generateTradeOffAnalysis(List<String> technologyNames) throws Exception {
        try {
            String prompt = buildTradeOffPrompt(technologyNames);
            GenerateContentResponse response = model.generateContent(prompt);
            return response.getCandidates(0).getContent().getParts(0).getText();
        } catch (Exception e) {
            log.warn("Gemini API attempt failed for trade-off analysis: {}, error: {}", technologyNames, e.getMessage());
            throw e; // Retry will handle this
        }
    }
    
    /**
     * Recover method for generateTradeOffAnalysis when all retries fail
     */
    @Recover
    public String recoverFromTradeOffAnalysisFailure(Exception ex, List<String> technologyNames) {
        log.error("All Gemini API attempts failed for trade-off analysis, using fallback data", ex);
        return fallbackService.getFallbackTradeOffAnalysis(technologyNames);
    }
    
    /**
     * Generate decision support questions
     */
    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public List<String> generateDecisionQuestions(List<String> technologyNames, UserConstraints constraints) throws Exception {
        try {
            String prompt = buildDecisionQuestionsPrompt(technologyNames, constraints);
            GenerateContentResponse response = model.generateContent(prompt);
            String responseText = response.getCandidates(0).getContent().getParts(0).getText();
            
            // Parse the response as a simple list of questions
            return List.of(responseText.split("\\n"))
                .stream()
                .map(String::trim)
                .filter(line -> !line.isEmpty() && (line.startsWith("-") || line.startsWith("•") || line.matches("\\d+\\.")))
                .map(line -> line.replaceFirst("^[-•]\\s*|^\\d+\\.\\s*", ""))
                .toList();
        } catch (Exception e) {
            log.warn("Gemini API attempt failed for decision questions: {}, error: {}", technologyNames, e.getMessage());
            throw e; // Retry will handle this
        }
    }
    
    /**
     * Recover method for generateDecisionQuestions when all retries fail
     */
    @Recover
    public List<String> recoverFromDecisionQuestionsFailure(Exception ex, List<String> technologyNames, UserConstraints constraints) {
        log.error("All Gemini API attempts failed for decision questions, using fallback data", ex);
        return fallbackService.getFallbackDecisionQuestions(technologyNames, constraints);
    }

    /**
     * Build the main analysis prompt for Gemini
     */
    private String buildAnalysisPrompt(List<String> technologies, UserConstraints constraints) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("You are a Senior Tech Lead with 15+ years of experience. ");
        prompt.append("Analyze these technologies: ").append(String.join(", ", technologies)).append("\n\n");
        
        if (constraints != null && !constraints.priorityTags().isEmpty()) {
            prompt.append("User priorities: ").append(String.join(", ", constraints.priorityTags())).append("\n");
            if (constraints.projectType() != null) {
                prompt.append("Project type: ").append(constraints.projectType()).append("\n");
            }
            if (constraints.teamSize() != null) {
                prompt.append("Team size: ").append(constraints.teamSize()).append("\n");
            }
            if (constraints.timeline() != null) {
                prompt.append("Timeline: ").append(constraints.timeline()).append("\n");
            }
            prompt.append("\n");
        }
        
        prompt.append("Return the response in strict JSON format with no markdown formatting.\n");
        prompt.append("The JSON must contain:\n");
        prompt.append("- 'radarChartData': array with fields: subject, A, B, C (optional), D (optional), E (optional), fullMark\n");
        prompt.append("- 'insights': array of objects with: technology, strengths (array), weaknesses (array), recommendation, useCase\n");
        prompt.append("- 'tradeOffs': array of key decision factors as strings\n");
        prompt.append("- 'recommendations': overall recommendation as string\n");
        prompt.append("- 'decisionQuestions': array of questions to help decision making\n\n");
        
        prompt.append("JSON Schema:\n");
        prompt.append(getJsonSchema());
        
        return prompt.toString();
    }
    
    /**
     * Build prompt for trade-off analysis
     */
    private String buildTradeOffPrompt(List<String> technologies) {
        return String.format("""
            You are a Senior Tech Lead. Analyze the key trade-offs between these technologies: %s
            
            Focus on:
            - Performance vs Development Speed
            - Learning Curve vs Long-term Benefits  
            - Community Support vs Innovation
            - Scalability vs Simplicity
            - Cost vs Features
            
            Provide a detailed analysis of the main trade-offs developers should consider.
            """, String.join(", ", technologies));
    }
    
    /**
     * Build prompt for decision support questions
     */
    private String buildDecisionQuestionsPrompt(List<String> technologies, UserConstraints constraints) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("You are a Senior Tech Lead. Generate 5-7 decision support questions ");
        prompt.append("to help choose between these technologies: ").append(String.join(", ", technologies)).append("\n\n");
        
        if (constraints != null) {
            if (!constraints.priorityTags().isEmpty()) {
                prompt.append("User priorities: ").append(String.join(", ", constraints.priorityTags())).append("\n");
            }
            if (constraints.projectType() != null) {
                prompt.append("Project type: ").append(constraints.projectType()).append("\n");
            }
        }
        
        prompt.append("\nGenerate questions that help evaluate:\n");
        prompt.append("- Technical requirements and constraints\n");
        prompt.append("- Team capabilities and preferences\n");
        prompt.append("- Project timeline and budget\n");
        prompt.append("- Long-term maintenance considerations\n");
        prompt.append("- Integration with existing systems\n\n");
        prompt.append("Format as a simple list with one question per line, starting with - or •");
        
        return prompt.toString();
    }

    /**
     * Parse JSON response from Gemini into ComparisonResult
     */
    private ComparisonResult parseJsonResponse(String jsonText) {
        try {
            // Clean the response text - remove markdown formatting if present
            String cleanJson = jsonText.trim();
            if (cleanJson.startsWith("```json")) {
                cleanJson = cleanJson.substring(7);
            }
            if (cleanJson.endsWith("```")) {
                cleanJson = cleanJson.substring(0, cleanJson.length() - 3);
            }
            cleanJson = cleanJson.trim();
            
            GeminiResponse geminiResponse = objectMapper.readValue(cleanJson, GeminiResponse.class);
            
            // Convert to ComparisonResult
            List<RadarChartData> radarData = convertRadarData(geminiResponse.getRadarChartData());
            
            return ComparisonResult.builder()
                .radarData(radarData)
                .kpiMetrics(new HashMap<>()) // Will be populated by other services
                .recommendationSummary(geminiResponse.getRecommendations())
                .scores(new ArrayList<>()) // Will be populated by scoring service
                .build();
                
        } catch (Exception e) {
            log.error("Failed to parse Gemini JSON response: {}", jsonText, e);
            throw new GeminiServiceException("Failed to parse AI response", e);
        }
    }
    
    /**
     * Convert Gemini radar data to domain objects
     */
    private List<RadarChartData> convertRadarData(List<GeminiResponse.RadarDataPoint> geminiData) {
        if (geminiData == null) {
            return new ArrayList<>();
        }
        
        return geminiData.stream()
            .map(point -> new RadarChartData(
                point.getSubject(),
                point.getA(),
                point.getB(),
                point.getC(),
                point.getD(),
                point.getE(),
                point.getFullMark() != null ? point.getFullMark() : 100.0
            ))
            .toList();
    }

    /**
     * Get the JSON schema for Gemini response
     */
    private String getJsonSchema() {
        return """
            {
              "radarChartData": [
                {
                  "subject": "Performance",
                  "A": 85.0,
                  "B": 75.0,
                  "C": 90.0,
                  "fullMark": 100.0
                }
              ],
              "insights": [
                {
                  "technology": "React",
                  "strengths": ["Large ecosystem", "Strong community"],
                  "weaknesses": ["Learning curve", "Frequent updates"],
                  "recommendation": "Best for large-scale applications",
                  "useCase": "Enterprise web applications"
                }
              ],
              "tradeOffs": [
                "Performance vs Development Speed",
                "Learning Curve vs Long-term Benefits"
              ],
              "recommendations": "Overall recommendation based on analysis",
              "decisionQuestions": [
                "What is your team's experience level?",
                "How important is performance vs development speed?"
              ]
            }
            """;
    }
    
    /**
     * Custom exception for Gemini service errors
     */
    public static class GeminiServiceException extends RuntimeException {
        public GeminiServiceException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}