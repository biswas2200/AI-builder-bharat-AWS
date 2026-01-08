package com.devdecision.referee.domain;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Complete result of a technology comparison including scores, visualizations, and metrics.
 * Contains all data needed for displaying comparison results to users.
 */
public class ComparisonResult {
    
    @NotNull
    @Size(min = 1, message = "At least one technology score is required")
    private final List<TechnologyScore> scores;
    
    @NotNull
    private final List<RadarChartData> radarData;
    
    @NotNull
    private final Map<String, List<KpiMetric>> kpiMetrics;
    
    private final String recommendationSummary;
    private final LocalDateTime generatedAt;
    private final UserConstraints constraints;

    public ComparisonResult(List<TechnologyScore> scores, 
                           List<RadarChartData> radarData, 
                           Map<String, List<KpiMetric>> kpiMetrics) {
        this(scores, radarData, kpiMetrics, null, null);
    }
    
    public ComparisonResult(List<TechnologyScore> scores, 
                           List<RadarChartData> radarData, 
                           Map<String, List<KpiMetric>> kpiMetrics,
                           String recommendationSummary,
                           UserConstraints constraints) {
        this.scores = Objects.requireNonNull(scores, "Scores cannot be null");
        this.radarData = Objects.requireNonNull(radarData, "Radar data cannot be null");
        this.kpiMetrics = kpiMetrics != null ? new HashMap<>(kpiMetrics) : new HashMap<>();
        this.recommendationSummary = recommendationSummary;
        this.constraints = constraints;
        this.generatedAt = LocalDateTime.now();
        
        if (scores.isEmpty()) {
            throw new IllegalArgumentException("At least one technology score is required");
        }
    }

    // Getters
    public List<TechnologyScore> getScores() {
        return new ArrayList<>(scores);
    }

    public List<RadarChartData> getRadarData() {
        return new ArrayList<>(radarData);
    }

    public Map<String, List<KpiMetric>> getKpiMetrics() {
        return new HashMap<>(kpiMetrics);
    }

    public String getRecommendationSummary() {
        return recommendationSummary;
    }

    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }

    public UserConstraints getConstraints() {
        return constraints;
    }
    
    /**
     * Get scores sorted by overall score (highest first)
     */
    public List<TechnologyScore> getSortedScores() {
        return scores.stream()
            .sorted((a, b) -> Double.compare(b.getOverallScore(), a.getOverallScore()))
            .toList();
    }
    
    /**
     * Get the highest scoring technology
     */
    public TechnologyScore getTopScore() {
        return scores.stream()
            .max(Comparator.comparing(TechnologyScore::getOverallScore))
            .orElse(null);
    }
    
    /**
     * Get KPI metrics for a specific technology
     */
    public List<KpiMetric> getKpiMetricsForTechnology(String technologyName) {
        return kpiMetrics.getOrDefault(technologyName, Collections.emptyList());
    }
    
    /**
     * Get all technology names in the comparison
     */
    public List<String> getTechnologyNames() {
        return scores.stream()
            .map(TechnologyScore::getTechnologyName)
            .toList();
    }
    
    /**
     * Get the number of technologies being compared
     */
    public int getTechnologyCount() {
        return scores.size();
    }
    
    /**
     * Check if the comparison has a recommendation summary
     */
    public boolean hasRecommendation() {
        return recommendationSummary != null && !recommendationSummary.trim().isEmpty();
    }
    
    /**
     * Get score for a specific technology by name
     */
    public Optional<TechnologyScore> getScoreForTechnology(String technologyName) {
        return scores.stream()
            .filter(score -> score.getTechnologyName().equalsIgnoreCase(technologyName))
            .findFirst();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ComparisonResult that = (ComparisonResult) o;
        return Objects.equals(scores, that.scores) &&
               Objects.equals(radarData, that.radarData) &&
               Objects.equals(generatedAt, that.generatedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(scores, radarData, generatedAt);
    }

    @Override
    public String toString() {
        return "ComparisonResult{" +
                "technologyCount=" + scores.size() +
                ", radarDataPoints=" + radarData.size() +
                ", kpiMetricsCount=" + kpiMetrics.size() +
                ", hasRecommendation=" + hasRecommendation() +
                ", generatedAt=" + generatedAt +
                '}';
    }
    
    /**
     * Builder pattern for creating ComparisonResult instances
     */
    public static class Builder {
        private List<TechnologyScore> scores;
        private List<RadarChartData> radarData;
        private Map<String, List<KpiMetric>> kpiMetrics;
        private String recommendationSummary;
        private UserConstraints constraints;
        
        public Builder scores(List<TechnologyScore> scores) {
            this.scores = scores;
            return this;
        }
        
        public Builder radarData(List<RadarChartData> radarData) {
            this.radarData = radarData;
            return this;
        }
        
        public Builder kpiMetrics(Map<String, List<KpiMetric>> kpiMetrics) {
            this.kpiMetrics = kpiMetrics;
            return this;
        }
        
        public Builder recommendationSummary(String recommendationSummary) {
            this.recommendationSummary = recommendationSummary;
            return this;
        }
        
        public Builder constraints(UserConstraints constraints) {
            this.constraints = constraints;
            return this;
        }
        
        public ComparisonResult build() {
            return new ComparisonResult(scores, radarData, kpiMetrics, recommendationSummary, constraints);
        }
    }
    
    public static Builder builder() {
        return new Builder();
    }
}