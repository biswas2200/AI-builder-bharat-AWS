package com.devdecision.insight.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Objects;

/**
 * AI-generated analysis for a technology including strengths, weaknesses, and recommendations.
 */
public class InsightAnalysis {
    
    @NotBlank
    private final String technologyName;
    
    @NotNull
    private final List<String> strengths;
    
    @NotNull
    private final List<String> weaknesses;
    
    @NotBlank
    private final String recommendation;
    
    private final String useCase;
    private final Double confidenceScore;

    public InsightAnalysis(String technologyName, List<String> strengths, List<String> weaknesses, String recommendation) {
        this(technologyName, strengths, weaknesses, recommendation, null, null);
    }
    
    public InsightAnalysis(String technologyName, List<String> strengths, List<String> weaknesses, 
                          String recommendation, String useCase, Double confidenceScore) {
        this.technologyName = Objects.requireNonNull(technologyName, "Technology name cannot be null");
        this.strengths = Objects.requireNonNull(strengths, "Strengths cannot be null");
        this.weaknesses = Objects.requireNonNull(weaknesses, "Weaknesses cannot be null");
        this.recommendation = Objects.requireNonNull(recommendation, "Recommendation cannot be null");
        this.useCase = useCase;
        this.confidenceScore = confidenceScore;
    }

    // Getters
    public String getTechnologyName() {
        return technologyName;
    }

    public List<String> getStrengths() {
        return List.copyOf(strengths);
    }

    public List<String> getWeaknesses() {
        return List.copyOf(weaknesses);
    }

    public String getRecommendation() {
        return recommendation;
    }

    public String getUseCase() {
        return useCase;
    }

    public Double getConfidenceScore() {
        return confidenceScore;
    }
    
    /**
     * Check if this analysis has a use case specified
     */
    public boolean hasUseCase() {
        return useCase != null && !useCase.trim().isEmpty();
    }
    
    /**
     * Check if this analysis has a confidence score
     */
    public boolean hasConfidenceScore() {
        return confidenceScore != null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InsightAnalysis that = (InsightAnalysis) o;
        return Objects.equals(technologyName, that.technologyName) &&
               Objects.equals(strengths, that.strengths) &&
               Objects.equals(weaknesses, that.weaknesses) &&
               Objects.equals(recommendation, that.recommendation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(technologyName, strengths, weaknesses, recommendation);
    }

    @Override
    public String toString() {
        return "InsightAnalysis{" +
                "technologyName='" + technologyName + '\'' +
                ", strengthsCount=" + strengths.size() +
                ", weaknessesCount=" + weaknesses.size() +
                ", hasUseCase=" + hasUseCase() +
                ", hasConfidenceScore=" + hasConfidenceScore() +
                '}';
    }
}