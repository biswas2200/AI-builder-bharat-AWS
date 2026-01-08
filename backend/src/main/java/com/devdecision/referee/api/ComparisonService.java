package com.devdecision.referee.api;

import com.devdecision.referee.domain.ComparisonResult;
import com.devdecision.referee.domain.UserConstraints;

import java.util.List;

/**
 * Public API interface for generating complete technology comparison results.
 * Orchestrates scoring, radar chart generation, and KPI metric calculation.
 */
public interface ComparisonService {
    
    /**
     * Generate a complete comparison result for the given technologies.
     * 
     * @param technologyIds List of technology IDs to compare
     * @param constraints User constraints and preferences
     * @return Complete comparison result with scores, charts, and metrics
     */
    ComparisonResult generateComparison(List<Long> technologyIds, UserConstraints constraints);
    
    /**
     * Generate a complete comparison result for technologies by names.
     * 
     * @param technologyNames List of technology names to compare
     * @param constraints User constraints and preferences
     * @return Complete comparison result with scores, charts, and metrics
     */
    ComparisonResult generateComparisonByNames(List<String> technologyNames, UserConstraints constraints);
}