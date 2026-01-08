package com.devdecision.insight.api;

import com.devdecision.referee.domain.ComparisonResult;
import com.devdecision.referee.domain.UserConstraints;

import java.util.List;

/**
 * Public API for the Insight module.
 * Provides AI-powered analysis and recommendations for technology comparisons.
 */
public interface InsightService {
    
    /**
     * Generate AI-powered insights for a list of technologies
     * 
     * @param technologyNames List of technology names to analyze
     * @return ComparisonResult with AI-generated insights and recommendations
     */
    ComparisonResult generateInsights(List<String> technologyNames);
    
    /**
     * Generate personalized recommendations based on user constraints
     * 
     * @param technologyNames List of technology names to analyze
     * @param constraints User constraints and preferences
     * @return ComparisonResult with personalized recommendations
     */
    ComparisonResult generatePersonalizedInsights(List<String> technologyNames, UserConstraints constraints);
    
    /**
     * Generate trade-off analysis between technologies
     * 
     * @param technologyNames List of technology names to compare
     * @return String containing detailed trade-off analysis
     */
    String generateTradeOffAnalysis(List<String> technologyNames);
    
    /**
     * Generate decision support questions for technology selection
     * 
     * @param technologyNames List of technology names being considered
     * @param constraints User constraints and preferences
     * @return List of questions to help with decision making
     */
    List<String> generateDecisionQuestions(List<String> technologyNames, UserConstraints constraints);
}