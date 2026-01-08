package com.devdecision.referee.api;

import com.devdecision.referee.domain.TechnologyScore;
import com.devdecision.referee.domain.UserConstraints;

import java.util.List;

/**
 * Public API interface for the Referee module's scoring functionality.
 * Provides weighted scoring algorithms for technology comparison.
 */
public interface WeightedScoringService {
    
    /**
     * Calculate weighted scores for a list of technologies based on user constraints.
     * 
     * @param technologyIds List of technology IDs to score
     * @param constraints User constraints including priority tags
     * @return List of TechnologyScore objects with calculated scores
     */
    List<TechnologyScore> calculateScores(List<Long> technologyIds, UserConstraints constraints);
    
    /**
     * Calculate weighted scores for technologies by names.
     * 
     * @param technologyNames List of technology names to score
     * @param constraints User constraints including priority tags
     * @return List of TechnologyScore objects with calculated scores
     */
    List<TechnologyScore> calculateScoresByNames(List<String> technologyNames, UserConstraints constraints);
    
    /**
     * Calculate score for a single technology.
     * 
     * @param technologyId Technology ID to score
     * @param constraints User constraints including priority tags
     * @return TechnologyScore object with calculated score
     */
    TechnologyScore calculateScore(Long technologyId, UserConstraints constraints);
}