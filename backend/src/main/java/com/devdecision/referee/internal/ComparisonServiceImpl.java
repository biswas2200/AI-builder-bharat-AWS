package com.devdecision.referee.internal;

import com.devdecision.inventory.api.InventoryService;
import com.devdecision.inventory.domain.Criteria;
import com.devdecision.inventory.domain.Technology;
import com.devdecision.referee.api.ComparisonService;
import com.devdecision.referee.api.WeightedScoringService;
import com.devdecision.referee.domain.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.transaction.annotation.Transactional;
import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of ComparisonService that generates complete comparison results.
 * Orchestrates scoring, radar chart generation, and KPI metric calculation.
 */
public class ComparisonServiceImpl implements ComparisonService {
    
    private static final Logger log = LoggerFactory.getLogger(ComparisonServiceImpl.class);
    private static final DecimalFormat NUMBER_FORMAT = new DecimalFormat("#,###");
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.#");
    
    private final WeightedScoringService scoringService;
    private final InventoryService inventoryService;
    
    public ComparisonServiceImpl(WeightedScoringService scoringService, InventoryService inventoryService) {
        this.scoringService = scoringService;
        this.inventoryService = inventoryService;
    }

    @Override
    @Cacheable(value = "comparisons", key = "#technologyIds.toString() + ':' + #constraints.hashCode()")
    @Transactional(readOnly = true)
    public ComparisonResult generateComparison(List<Long> technologyIds, UserConstraints constraints) {
        log.info("Generating comparison for {} technologies", technologyIds.size());
        
        if (technologyIds == null || technologyIds.isEmpty()) {
            throw new IllegalArgumentException("Technology IDs cannot be null or empty");
        }
        
        if (technologyIds.size() > 5) {
            throw new IllegalArgumentException("Cannot compare more than 5 technologies at once");
        }
        
        // Validate technologies exist using optimized batch query
        List<Technology> technologies = inventoryService.findTechnologiesByIds(technologyIds);
        if (technologies.size() != technologyIds.size()) {
            throw new IllegalArgumentException("Some technology IDs not found");
        }
        
        // Calculate scores
        List<TechnologyScore> scores = scoringService.calculateScores(technologyIds, constraints);
        
        if (scores.isEmpty()) {
            throw new IllegalArgumentException("No valid technologies found for comparison");
        }
        
        // Generate radar chart data
        List<RadarChartData> radarData = generateRadarChartData(scores);
        
        // Calculate KPI metrics
        Map<String, List<KpiMetric>> kpiMetrics = generateKpiMetrics(scores);
        
        // Generate recommendation summary (placeholder for now)
        String recommendationSummary = generateRecommendationSummary(scores, constraints);
        
        log.info("Comparison generated successfully for {} technologies", scores.size());
        
        return ComparisonResult.builder()
            .scores(scores)
            .radarData(radarData)
            .kpiMetrics(kpiMetrics)
            .recommendationSummary(recommendationSummary)
            .constraints(constraints)
            .build();
    }

    @Override
    @Cacheable(value = "comparisons", key = "#technologyNames.toString() + ':' + #constraints.hashCode()")
    @Transactional(readOnly = true)
    public ComparisonResult generateComparisonByNames(List<String> technologyNames, UserConstraints constraints) {
        log.info("Generating comparison for technologies by names: {}", technologyNames);
        
        List<Long> technologyIds = technologyNames.stream()
            .map(inventoryService::findTechnologyByName)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .map(Technology::getId)
            .collect(Collectors.toList());
            
        if (technologyIds.size() != technologyNames.size()) {
            log.warn("Some technologies not found. Requested: {}, Found: {}", 
                    technologyNames.size(), technologyIds.size());
        }
        
        return generateComparison(technologyIds, constraints);
    }
    
    /**
     * Generate radar chart data from technology scores
     */
    private List<RadarChartData> generateRadarChartData(List<TechnologyScore> scores) {
        log.debug("Generating radar chart data for {} technologies", scores.size());
        
        // Get all criteria from the first technology's scores
        Set<String> allCriteria = scores.get(0).getCriterionScores().keySet();
        
        List<RadarChartData> radarData = new ArrayList<>();
        
        for (String criterion : allCriteria) {
            List<Double> criterionScores = scores.stream()
                .map(score -> score.getCriterionScore(criterion))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
                
            if (!criterionScores.isEmpty()) {
                RadarChartData chartData = createRadarChartDataPoint(criterion, criterionScores);
                radarData.add(chartData);
            }
        }
        
        log.debug("Generated {} radar chart data points", radarData.size());
        return radarData;
    }
    
    /**
     * Create a single radar chart data point for a criterion
     */
    private RadarChartData createRadarChartDataPoint(String criterion, List<Double> scores) {
        Double fullMark = 100.0; // All scores are normalized to 0-100
        
        return switch (scores.size()) {
            case 1 -> new RadarChartData(criterion, scores.get(0), 0.0, fullMark);
            case 2 -> new RadarChartData(criterion, scores.get(0), scores.get(1), fullMark);
            case 3 -> new RadarChartData(criterion, scores.get(0), scores.get(1), scores.get(2), fullMark);
            case 4 -> new RadarChartData(criterion, scores.get(0), scores.get(1), scores.get(2), scores.get(3), fullMark);
            case 5 -> new RadarChartData(criterion, scores.get(0), scores.get(1), scores.get(2), scores.get(3), scores.get(4), fullMark);
            default -> throw new IllegalArgumentException("Unsupported number of technologies: " + scores.size());
        };
    }
    
    /**
     * Generate KPI metrics for each technology
     */
    private Map<String, List<KpiMetric>> generateKpiMetrics(List<TechnologyScore> scores) {
        log.debug("Generating KPI metrics for {} technologies", scores.size());
        
        Map<String, List<KpiMetric>> kpiMetrics = new HashMap<>();
        
        for (TechnologyScore score : scores) {
            Technology technology = score.getTechnology();
            List<KpiMetric> metrics = new ArrayList<>();
            
            try {
                // Safely access metrics (they should always be available)
                Map<String, Double> techMetrics = technology.getMetrics();
                if (techMetrics == null) {
                    techMetrics = new HashMap<>();
                }
                
                // GitHub Stars
                Double githubStars = techMetrics.get("github_stars");
                if (githubStars != null) {
                    metrics.add(new KpiMetric(
                        "GitHub Stars",
                        githubStars,
                        NUMBER_FORMAT.format(githubStars.longValue()),
                        "stars",
                        "Community popularity on GitHub",
                        KpiMetric.KpiMetricType.COUNT
                    ));
                }
                
                // NPM Downloads
                Double npmDownloads = techMetrics.get("npm_downloads");
                if (npmDownloads != null) {
                    metrics.add(new KpiMetric(
                        "NPM Downloads",
                        npmDownloads,
                        formatLargeNumber(npmDownloads),
                        "downloads/month",
                        "Monthly NPM package downloads",
                        KpiMetric.KpiMetricType.COUNT
                    ));
                }
                
                // Job Openings
                Double jobOpenings = techMetrics.get("job_openings");
                if (jobOpenings != null) {
                    metrics.add(new KpiMetric(
                        "Job Openings",
                        jobOpenings,
                        NUMBER_FORMAT.format(jobOpenings.longValue()),
                        "jobs",
                        "Current job market demand",
                        KpiMetric.KpiMetricType.COUNT
                    ));
                }
                
                // Satisfaction Score
                Double satisfactionScore = techMetrics.get("satisfaction_score");
                if (satisfactionScore != null) {
                    metrics.add(new KpiMetric(
                        "Satisfaction",
                        satisfactionScore,
                        DECIMAL_FORMAT.format(satisfactionScore) + "/5",
                        "stars",
                        "Developer satisfaction rating",
                        KpiMetric.KpiMetricType.RATING
                    ));
                }
                
                // Overall Score - always add this
                metrics.add(new KpiMetric(
                    "Overall Score",
                    score.getOverallScore(),
                    DECIMAL_FORMAT.format(score.getOverallScore()),
                    "points",
                    "Weighted overall comparison score",
                    KpiMetric.KpiMetricType.NUMERIC
                ));
                
            } catch (Exception e) {
                log.warn("Error generating KPI metrics for technology {}: {}", technology.getName(), e.getMessage());
                // Add at least the overall score
                metrics.add(new KpiMetric(
                    "Overall Score",
                    score.getOverallScore(),
                    DECIMAL_FORMAT.format(score.getOverallScore()),
                    "points",
                    "Weighted overall comparison score",
                    KpiMetric.KpiMetricType.NUMERIC
                ));
            }
            
            kpiMetrics.put(technology.getName(), metrics);
        }
        
        log.debug("Generated KPI metrics for {} technologies", kpiMetrics.size());
        return kpiMetrics;
    }
    
    /**
     * Format large numbers with appropriate units (K, M, B)
     */
    private String formatLargeNumber(Double number) {
        if (number >= 1_000_000_000) {
            return DECIMAL_FORMAT.format(number / 1_000_000_000) + "B";
        } else if (number >= 1_000_000) {
            return DECIMAL_FORMAT.format(number / 1_000_000) + "M";
        } else if (number >= 1_000) {
            return DECIMAL_FORMAT.format(number / 1_000) + "K";
        } else {
            return NUMBER_FORMAT.format(number.longValue());
        }
    }
    
    /**
     * Generate a basic recommendation summary (placeholder implementation)
     */
    private String generateRecommendationSummary(List<TechnologyScore> scores, UserConstraints constraints) {
        if (scores.isEmpty()) {
            return "No technologies to compare.";
        }
        
        TechnologyScore topScore = scores.stream()
            .max(Comparator.comparing(TechnologyScore::getOverallScore))
            .orElse(scores.get(0));
            
        StringBuilder summary = new StringBuilder();
        summary.append("Based on your criteria, ")
               .append(topScore.getTechnologyName())
               .append(" scores highest with ")
               .append(DECIMAL_FORMAT.format(topScore.getOverallScore()))
               .append(" points.");
               
        if (constraints != null && !constraints.priorityTags().isEmpty()) {
            summary.append(" Your priority areas (")
                   .append(String.join(", ", constraints.priorityTags()))
                   .append(") were given 1.5x weight in the scoring.");
        }
        
        return summary.toString();
    }
}