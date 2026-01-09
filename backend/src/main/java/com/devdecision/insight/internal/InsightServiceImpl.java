package com.devdecision.insight.internal;

import com.devdecision.insight.api.InsightService;
import com.devdecision.insight.domain.InsightAnalysis;
import com.devdecision.referee.domain.ComparisonResult;
import com.devdecision.referee.domain.UserConstraints;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Implementation of InsightService that coordinates AI analysis and recommendations.
 * Integrates Gemini AI with fallback mechanisms for reliable service.
 */
@Service
public class InsightServiceImpl implements InsightService {
    
    private static final Logger log = LoggerFactory.getLogger(InsightServiceImpl.class);
    
    private final GeminiService geminiService;
    private final InsightFallbackService fallbackService;
    private final RecommendationEngine recommendationEngine;

    public InsightServiceImpl(GeminiService geminiService, 
                             InsightFallbackService fallbackService,
                             RecommendationEngine recommendationEngine) {
        this.geminiService = geminiService;
        this.fallbackService = fallbackService;
        this.recommendationEngine = recommendationEngine;
    }

    @Override
    @Cacheable(value = "insights", key = "#technologyNames")
    public ComparisonResult generateInsights(List<String> technologyNames) {
        log.info("Generating insights for technologies: {}", technologyNames);
        
        if (technologyNames == null || technologyNames.isEmpty()) {
            throw new IllegalArgumentException("Technology names cannot be null or empty");
        }
        
        try {
            return geminiService.generateInsights(technologyNames);
        } catch (Exception e) {
            log.warn("Failed to generate insights via Gemini, using fallback: {}", e.getMessage());
            return fallbackService.getFallbackData(technologyNames);
        }
    }

    @Override
    @Cacheable(value = "personalizedInsights", key = "#technologyNames + '_' + #constraints.hashCode()")
    public ComparisonResult generatePersonalizedInsights(List<String> technologyNames, UserConstraints constraints) {
        log.info("Generating personalized insights for technologies: {} with constraints: {}", technologyNames, constraints);
        
        if (technologyNames == null || technologyNames.isEmpty()) {
            throw new IllegalArgumentException("Technology names cannot be null or empty");
        }
        
        try {
            ComparisonResult result = geminiService.generatePersonalizedInsights(technologyNames, constraints);
            
            // Enhance with recommendation engine analysis
            String enhancedRecommendation = recommendationEngine.generatePersonalizedRecommendation(
                technologyNames, constraints, result.getRecommendationSummary());
            
            return ComparisonResult.builder()
                .scores(result.getScores())
                .radarData(result.getRadarData())
                .kpiMetrics(result.getKpiMetrics())
                .recommendationSummary(enhancedRecommendation)
                .constraints(constraints)
                .build();
                
        } catch (Exception e) {
            log.warn("Failed to generate personalized insights via Gemini, using fallback: {}", e.getMessage());
            ComparisonResult fallbackResult = fallbackService.getFallbackData(technologyNames);
            
            // Still enhance with recommendation engine
            String enhancedRecommendation = recommendationEngine.generatePersonalizedRecommendation(
                technologyNames, constraints, fallbackResult.getRecommendationSummary());
            
            return ComparisonResult.builder()
                .scores(fallbackResult.getScores())
                .radarData(fallbackResult.getRadarData())
                .kpiMetrics(fallbackResult.getKpiMetrics())
                .recommendationSummary(enhancedRecommendation)
                .constraints(constraints)
                .build();
        }
    }

    @Override
    @Cacheable(value = "tradeOffAnalysis", key = "#technologyNames")
    public String generateTradeOffAnalysis(List<String> technologyNames) {
        log.info("Generating trade-off analysis for technologies: {}", technologyNames);
        
        if (technologyNames == null || technologyNames.isEmpty()) {
            throw new IllegalArgumentException("Technology names cannot be null or empty");
        }
        
        try {
            String aiAnalysis = geminiService.generateTradeOffAnalysis(technologyNames);
            
            // Enhance with structured analysis (works for both single and multiple technologies)
            String structuredAnalysis;
            if (technologyNames.size() == 1) {
                structuredAnalysis = recommendationEngine.generateSingleTechnologyAnalysis(technologyNames.get(0));
            } else {
                structuredAnalysis = recommendationEngine.generateStructuredTradeOffAnalysis(technologyNames);
            }
            
            return aiAnalysis + "\n\n" + structuredAnalysis;
            
        } catch (Exception e) {
            log.warn("Failed to generate trade-off analysis via Gemini, using fallback: {}", e.getMessage());
            return fallbackService.getFallbackTradeOffAnalysis(technologyNames);
        }
    }

    @Override
    @Cacheable(value = "decisionQuestions", key = "#technologyNames + '_' + #constraints.hashCode()")
    public List<String> generateDecisionQuestions(List<String> technologyNames, UserConstraints constraints) {
        log.info("Generating decision questions for technologies: {} with constraints: {}", technologyNames, constraints);
        
        if (technologyNames == null || technologyNames.isEmpty()) {
            throw new IllegalArgumentException("Technology names cannot be null or empty");
        }
        
        try {
            List<String> aiQuestions = geminiService.generateDecisionQuestions(technologyNames, constraints);
            
            // Enhance with context-specific questions
            List<String> contextQuestions = recommendationEngine.generateContextualQuestions(technologyNames, constraints);
            
            // Combine and deduplicate
            return recommendationEngine.combineAndDeduplicateQuestions(aiQuestions, contextQuestions);
            
        } catch (Exception e) {
            log.warn("Failed to generate decision questions via Gemini, using fallback: {}", e.getMessage());
            return fallbackService.getFallbackDecisionQuestions(technologyNames, constraints);
        }
    }
    
    /**
     * Get individual technology insights
     */
    public List<InsightAnalysis> getTechnologyInsights(List<String> technologyNames) {
        log.info("Getting individual technology insights for: {}", technologyNames);
        
        try {
            // For now, use fallback service for individual insights
            // This could be enhanced to call Gemini for individual technology analysis
            return fallbackService.getFallbackInsights(technologyNames);
        } catch (Exception e) {
            log.warn("Failed to get technology insights: {}", e.getMessage());
            return fallbackService.getFallbackInsights(technologyNames);
        }
    }
}