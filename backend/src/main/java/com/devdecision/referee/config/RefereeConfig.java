package com.devdecision.referee.config;

import com.devdecision.inventory.api.InventoryService;
import com.devdecision.referee.api.ComparisonService;
import com.devdecision.referee.api.WeightedScoringService;
import com.devdecision.referee.internal.ComparisonServiceImpl;
import com.devdecision.referee.internal.WeightedScoringServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for the Referee module.
 * Defines beans and module-specific configuration.
 */
@Configuration
public class RefereeConfig {
    
    /**
     * Configure the WeightedScoringService implementation
     */
    @Bean
    public WeightedScoringService weightedScoringService(InventoryService inventoryService) {
        return new WeightedScoringServiceImpl(inventoryService);
    }
    
    /**
     * Configure the ComparisonService implementation
     */
    @Bean
    public ComparisonService comparisonService(WeightedScoringService scoringService, InventoryService inventoryService) {
        return new ComparisonServiceImpl(scoringService, inventoryService);
    }
}