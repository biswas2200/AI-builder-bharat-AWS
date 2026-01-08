package com.devdecision.referee.domain;

import com.devdecision.inventory.domain.Technology;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.util.Map;
import java.util.HashMap;
import java.util.Objects;

/**
 * Represents the calculated score for a technology across multiple criteria.
 * Contains the overall score, individual criterion scores, and the source technology.
 */
public class TechnologyScore {
    
    @NotNull
    private final Technology technology;
    
    @NotNull
    @DecimalMin("0.0")
    @DecimalMax("100.0")
    private final Double overallScore;
    
    @NotNull
    private final Map<String, Double> criterionScores;
    
    private final String explanation;

    public TechnologyScore(Technology technology, Double overallScore, Map<String, Double> criterionScores) {
        this(technology, overallScore, criterionScores, null);
    }
    
    public TechnologyScore(Technology technology, Double overallScore, Map<String, Double> criterionScores, String explanation) {
        this.technology = Objects.requireNonNull(technology, "Technology cannot be null");
        this.overallScore = Objects.requireNonNull(overallScore, "Overall score cannot be null");
        this.criterionScores = criterionScores != null ? new HashMap<>(criterionScores) : new HashMap<>();
        this.explanation = explanation;
        
        // Validate score range
        if (overallScore < 0.0 || overallScore > 100.0) {
            throw new IllegalArgumentException("Overall score must be between 0.0 and 100.0, got: " + overallScore);
        }
    }

    // Getters
    public Technology getTechnology() {
        return technology;
    }

    public Double getOverallScore() {
        return overallScore;
    }

    public Map<String, Double> getCriterionScores() {
        return new HashMap<>(criterionScores);
    }

    public String getExplanation() {
        return explanation;
    }
    
    /**
     * Get score for a specific criterion
     */
    public Double getCriterionScore(String criterionName) {
        return criterionScores.get(criterionName);
    }
    
    /**
     * Check if this technology has a score for the given criterion
     */
    public boolean hasCriterionScore(String criterionName) {
        return criterionScores.containsKey(criterionName);
    }
    
    /**
     * Get the technology name for convenience
     */
    public String getTechnologyName() {
        return technology.getName();
    }
    
    /**
     * Get the technology ID for convenience
     */
    public Long getTechnologyId() {
        return technology.getId();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TechnologyScore that = (TechnologyScore) o;
        return Objects.equals(technology.getId(), that.technology.getId()) &&
               Objects.equals(overallScore, that.overallScore);
    }

    @Override
    public int hashCode() {
        return Objects.hash(technology.getId(), overallScore);
    }

    @Override
    public String toString() {
        return "TechnologyScore{" +
                "technology=" + technology.getName() +
                ", overallScore=" + overallScore +
                ", criterionCount=" + criterionScores.size() +
                '}';
    }
}