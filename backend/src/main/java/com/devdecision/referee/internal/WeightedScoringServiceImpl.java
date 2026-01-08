package com.devdecision.referee.internal;

import com.devdecision.inventory.api.InventoryService;
import com.devdecision.inventory.domain.Criteria;
import com.devdecision.inventory.domain.Technology;
import com.devdecision.referee.api.WeightedScoringService;
import com.devdecision.referee.domain.TechnologyScore;
import com.devdecision.referee.domain.UserConstraints;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of the weighted scoring algorithm for technology comparison.
 * Applies user-selected tag multipliers and normalizes scores to 0-100 scale.
 */
public class WeightedScoringServiceImpl implements WeightedScoringService {
    
    private static final Logger log = LoggerFactory.getLogger(WeightedScoringServiceImpl.class);
    private static final double TAG_MULTIPLIER = 1.5;
    private static final double MIN_SCORE = 0.0;
    private static final double MAX_SCORE = 100.0;
    
    private final InventoryService inventoryService;
    
    public WeightedScoringServiceImpl(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @Override
    @Cacheable(value = "comparisons", key = "'scores:' + #technologyIds.toString() + ':' + #constraints.hashCode()")
    public List<TechnologyScore> calculateScores(List<Long> technologyIds, UserConstraints constraints) {
        log.debug("Calculating scores for {} technologies with constraints: {}", 
                 technologyIds.size(), constraints);
        
        if (technologyIds == null || technologyIds.isEmpty()) {
            return Collections.emptyList();
        }
        
        // Fetch technologies and criteria
        List<Technology> technologies = technologyIds.stream()
            .map(inventoryService::findTechnologyById)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(Collectors.toList());
            
        List<Criteria> allCriteria = inventoryService.getAllCriteria();
        
        if (technologies.isEmpty()) {
            log.warn("No technologies found for IDs: {}", technologyIds);
            return Collections.emptyList();
        }
        
        return technologies.stream()
            .map(tech -> calculateScoreForTechnology(tech, allCriteria, constraints))
            .collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "comparisons", key = "'scores-names:' + #technologyNames.toString() + ':' + #constraints.hashCode()")
    public List<TechnologyScore> calculateScoresByNames(List<String> technologyNames, UserConstraints constraints) {
        log.debug("Calculating scores for technologies by names: {}", technologyNames);
        
        if (technologyNames == null || technologyNames.isEmpty()) {
            return Collections.emptyList();
        }
        
        List<Long> technologyIds = technologyNames.stream()
            .map(inventoryService::findTechnologyByName)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .map(Technology::getId)
            .collect(Collectors.toList());
            
        return calculateScores(technologyIds, constraints);
    }

    @Override
    public TechnologyScore calculateScore(Long technologyId, UserConstraints constraints) {
        Optional<Technology> technologyOpt = inventoryService.findTechnologyById(technologyId);
        if (technologyOpt.isEmpty()) {
            throw new IllegalArgumentException("Technology not found with ID: " + technologyId);
        }
        
        List<Criteria> allCriteria = inventoryService.getAllCriteria();
        return calculateScoreForTechnology(technologyOpt.get(), allCriteria, constraints);
    }
    
    /**
     * Calculate score for a single technology using weighted scoring algorithm
     */
    private TechnologyScore calculateScoreForTechnology(Technology technology, 
                                                       List<Criteria> criteria, 
                                                       UserConstraints constraints) {
        log.debug("Calculating score for technology: {}", technology.getName());
        
        Map<String, Double> criterionScores = new HashMap<>();
        double totalWeightedScore = 0.0;
        double totalWeight = 0.0;
        
        for (Criteria criterion : criteria) {
            String criterionKey = mapCriteriaTypeToMetricKey(criterion.getType().name());
            Double rawScore = technology.getMetric(criterionKey);
            
            if (rawScore != null) {
                // Apply tag multiplier if user has prioritized this criterion type
                double adjustedScore = applyTagMultipliers(rawScore, constraints, criterion.getType().name());
                
                // Normalize score to 0-100 scale
                double normalizedScore = normalizeScore(adjustedScore);
                
                // Apply criterion weight
                double weightedScore = normalizedScore * criterion.getWeight();
                
                criterionScores.put(criterion.getName(), normalizedScore);
                totalWeightedScore += weightedScore;
                totalWeight += criterion.getWeight();
                
                log.debug("Criterion {}: raw={}, adjusted={}, normalized={}, weighted={}", 
                         criterion.getName(), rawScore, adjustedScore, normalizedScore, weightedScore);
            }
        }
        
        // Calculate overall score as weighted average
        double overallScore = totalWeight > 0 ? totalWeightedScore / totalWeight : 0.0;
        overallScore = Math.max(MIN_SCORE, Math.min(MAX_SCORE, overallScore));
        
        log.debug("Technology {} final score: {}", technology.getName(), overallScore);
        
        return new TechnologyScore(technology, overallScore, criterionScores);
    }
    
    /**
     * Apply tag multipliers based on user priority tags
     */
    private double applyTagMultipliers(double baseScore, UserConstraints constraints, String criteriaType) {
        if (constraints == null || constraints.priorityTags().isEmpty()) {
            return baseScore;
        }
        
        // Check if user has prioritized this criteria type
        String normalizedCriteriaType = criteriaType.toLowerCase().replace("_", "-");
        boolean isPrioritized = constraints.hasPriorityTag(normalizedCriteriaType) ||
                               constraints.hasPriorityTag(criteriaType.toLowerCase());
        
        if (isPrioritized) {
            log.debug("Applying {}x multiplier to {} (user priority)", TAG_MULTIPLIER, criteriaType);
            return baseScore * TAG_MULTIPLIER;
        }
        
        return baseScore;
    }
    
    /**
     * Normalize score to 0-100 scale
     * Assumes input scores are already in a reasonable range (0-100 or 0-5 stars, etc.)
     */
    private double normalizeScore(double score) {
        // Handle different score ranges commonly found in technology metrics
        if (score <= 5.0) {
            // Assume 5-star rating system, convert to 0-100
            return (score / 5.0) * 100.0;
        } else if (score <= 10.0) {
            // Assume 10-point scale, convert to 0-100
            return score * 10.0;
        } else if (score <= 100.0) {
            // Already in 0-100 scale
            return score;
        } else {
            // For larger numbers (like GitHub stars), use logarithmic scaling
            // Cap at reasonable maximum and scale logarithmically
            double maxValue = 100000.0; // 100k stars as maximum
            double cappedScore = Math.min(score, maxValue);
            return (Math.log10(cappedScore + 1) / Math.log10(maxValue + 1)) * 100.0;
        }
    }
    
    /**
     * Map criteria type enum names to metric keys in technology data
     */
    private String mapCriteriaTypeToMetricKey(String criteriaType) {
        return switch (criteriaType.toUpperCase()) {
            case "PERFORMANCE" -> "performance_score";
            case "LEARNING_CURVE" -> "learning_curve_score";
            case "COMMUNITY" -> "community_score";
            case "DOCUMENTATION" -> "documentation_score";
            case "SCALABILITY" -> "scalability_score";
            case "SECURITY" -> "security_score";
            case "POPULARITY" -> "github_stars";
            case "ADOPTION" -> "npm_downloads";
            case "JOB_MARKET" -> "job_openings";
            case "SATISFACTION" -> "satisfaction_score";
            default -> criteriaType.toLowerCase() + "_score";
        };
    }
}