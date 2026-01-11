package com.devdecision.api;

import com.devdecision.inventory.api.InventoryService;
import com.devdecision.inventory.domain.Criteria;
import com.devdecision.inventory.domain.Technology;
import com.devdecision.referee.api.ComparisonService;
import com.devdecision.insight.api.InsightService;
import com.devdecision.referee.domain.ComparisonResult;
import com.devdecision.referee.domain.UserConstraints;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Main REST controller for technology comparison operations.
 * Orchestrates all modules (Inventory, Referee, Insight) to provide complete comparison functionality.
 * 
 * Requirements: 2.1, 4.2, 8.4
 */
@RestController
@RequestMapping("/api/comparison")
@CrossOrigin(origins = "http://localhost:3000")
@Validated
public class ComparisonController {

    private static final Logger logger = LoggerFactory.getLogger(ComparisonController.class);

    private final InventoryService inventoryService;
    private final ComparisonService comparisonService;
    private final InsightService insightService;

    public ComparisonController(InventoryService inventoryService,
                               ComparisonService comparisonService,
                               InsightService insightService) {
        this.inventoryService = inventoryService;
        this.comparisonService = comparisonService;
        this.insightService = insightService;
    }

    /**
     * Simple test endpoint to verify basic functionality
     */
    @PostMapping("/test")
    public ResponseEntity<Map<String, Object>> testComparison(@Valid @RequestBody ComparisonRequest request) {
        logger.info("POST /api/comparison/test - Testing with {} technologies", request.technologyIds().size());
        
        try {
            // Simple response without complex logic
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Test successful");
            response.put("technologyIds", request.technologyIds());
            response.put("priorityTags", request.priorityTags());
            response.put("projectType", request.projectType());
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error in test comparison", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Debug endpoint to test individual service components
     */
    @PostMapping("/debug")
    public ResponseEntity<Map<String, Object>> debugComparison(@Valid @RequestBody ComparisonRequest request) {
        logger.info("POST /api/comparison/debug - Debugging with {} technologies", request.technologyIds().size());
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Test 1: Check if technologies exist
            List<Technology> technologies = request.technologyIds().stream()
                .map(id -> inventoryService.findTechnologyById(id))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
            
            response.put("step1_technologies_found", technologies.size());
            response.put("step1_technology_names", technologies.stream().map(Technology::getName).collect(Collectors.toList()));
            
            // Test 2: Create user constraints
            UserConstraints constraints = new UserConstraints(
                request.priorityTags(),
                request.projectType(),
                request.teamSize(),
                request.timeline()
            );
            
            response.put("step2_constraints_created", true);
            response.put("step2_priority_tags", constraints.priorityTags());
            
            // Test 3: Try to get all criteria
            List<Criteria> allCriteria = inventoryService.getAllCriteria();
            response.put("step3_criteria_found", allCriteria.size());
            
            response.put("status", "debug_successful");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error in debug comparison", e);
            response.put("error", e.getMessage());
            response.put("error_class", e.getClass().getSimpleName());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Generate a complete technology comparison with scoring, charts, and AI insights.
     * 
     * @param request Comparison request with technology IDs and user constraints
     * @return Complete comparison result with scores, visualizations, and recommendations
     */
    @PostMapping("/generate")
    public ResponseEntity<ComparisonResult> generateComparison(@Valid @RequestBody ComparisonRequest request) {
        logger.info("POST /api/comparison/generate - Comparing {} technologies with {} priority tags", 
                   request.technologyIds().size(), request.priorityTags().size());
        
        long startTime = System.currentTimeMillis();
        
        try {
            // Validate technology IDs exist
            List<Technology> technologies = request.technologyIds().stream()
                .map(id -> inventoryService.findTechnologyById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Technology not found with ID: " + id)))
                .collect(Collectors.toList());
            
            logger.debug("Found technologies: {}", 
                        technologies.stream().map(Technology::getName).collect(Collectors.toList()));
            
            // Create user constraints
            UserConstraints constraints = new UserConstraints(
                request.priorityTags(),
                request.projectType(),
                request.teamSize(),
                request.timeline()
            );
            
            // Generate comparison using Referee module
            ComparisonResult comparisonResult = comparisonService.generateComparison(
                request.technologyIds(), 
                constraints
            );
            
            // Enhance with AI insights if requested
            if (request.includeInsights()) {
                List<String> technologyNames = technologies.stream()
                    .map(Technology::getName)
                    .collect(Collectors.toList());
                
                ComparisonResult insightResult = insightService.generatePersonalizedInsights(
                    technologyNames, 
                    constraints
                );
                
                // Merge insights into comparison result
                comparisonResult = mergeInsights(comparisonResult, insightResult);
            }
            
            long duration = System.currentTimeMillis() - startTime;
            logger.info("Comparison generated successfully in {}ms for {} technologies", 
                       duration, request.technologyIds().size());
            
            return ResponseEntity.ok(comparisonResult);
            
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid comparison request: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Error generating comparison", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Generate comparison by technology names instead of IDs.
     * 
     * @param request Comparison request with technology names
     * @return Complete comparison result
     */
    @PostMapping("/generate-by-names")
    public ResponseEntity<ComparisonResult> generateComparisonByNames(@Valid @RequestBody ComparisonByNamesRequest request) {
        logger.info("POST /api/comparison/generate-by-names - Comparing technologies: {}", request.technologyNames());
        
        long startTime = System.currentTimeMillis();
        
        try {
            // Validate technology names exist
            List<Technology> technologies = request.technologyNames().stream()
                .map(name -> inventoryService.findTechnologyByName(name)
                    .orElseThrow(() -> new IllegalArgumentException("Technology not found with name: " + name)))
                .collect(Collectors.toList());
            
            // Create user constraints
            UserConstraints constraints = new UserConstraints(
                request.priorityTags(),
                request.projectType(),
                request.teamSize(),
                request.timeline()
            );
            
            // Generate comparison using Referee module
            ComparisonResult comparisonResult = comparisonService.generateComparisonByNames(
                request.technologyNames(), 
                constraints
            );
            
            // Enhance with AI insights if requested
            if (request.includeInsights()) {
                ComparisonResult insightResult = insightService.generatePersonalizedInsights(
                    request.technologyNames(), 
                    constraints
                );
                
                // Merge insights into comparison result
                comparisonResult = mergeInsights(comparisonResult, insightResult);
            }
            
            long duration = System.currentTimeMillis() - startTime;
            logger.info("Comparison by names generated successfully in {}ms", duration);
            
            return ResponseEntity.ok(comparisonResult);
            
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid comparison by names request: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Error generating comparison by names", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Simple test endpoint for insights service
     */
    @PostMapping("/insights-test")
    public ResponseEntity<Map<String, Object>> testInsights(@Valid @RequestBody InsightRequest request) {
        logger.info("POST /api/comparison/insights-test - Testing insights with: {}", request.technologyNames());
        
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Insights test successful");
            response.put("technologyNames", request.technologyNames());
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error in insights test", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            errorResponse.put("errorClass", e.getClass().getSimpleName());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Generate AI insights for a list of technologies without full comparison.
     * 
     * @param request Insight request with technology names
     * @return AI-generated insights and recommendations
     */
    @PostMapping("/insights")
    public ResponseEntity<ComparisonResult> generateInsights(@Valid @RequestBody InsightRequest request) {
        logger.info("POST /api/comparison/insights - Generating insights for: {}", request.technologyNames());
        
        try {
            UserConstraints constraints = request.constraints() != null ? request.constraints() : UserConstraints.empty();
            
            logger.debug("About to call insightService.generatePersonalizedInsights with technologies: {} and constraints: {}", 
                        request.technologyNames(), constraints);
            
            ComparisonResult insights = insightService.generatePersonalizedInsights(
                request.technologyNames(), 
                constraints
            );
            
            logger.info("Successfully generated insights for: {}", request.technologyNames());
            return ResponseEntity.ok(insights);
            
        } catch (Exception e) {
            logger.error("Error generating insights for technologies: {}", request.technologyNames(), e);
            
            // Return detailed error information for debugging
            Map<String, Object> errorDetails = new HashMap<>();
            errorDetails.put("error", e.getMessage());
            errorDetails.put("errorClass", e.getClass().getSimpleName());
            errorDetails.put("technologyNames", request.technologyNames());
            errorDetails.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Generate trade-off analysis between technologies.
     * 
     * @param request Trade-off analysis request
     * @return Trade-off analysis text
     */
    @PostMapping("/trade-offs")
    public ResponseEntity<TradeOffResponse> generateTradeOffs(@Valid @RequestBody TradeOffRequest request) {
        logger.info("POST /api/comparison/trade-offs - Analyzing trade-offs for: {}", request.technologyNames());
        
        try {
            String tradeOffAnalysis = insightService.generateTradeOffAnalysis(request.technologyNames());
            
            return ResponseEntity.ok(new TradeOffResponse(tradeOffAnalysis));
            
        } catch (Exception e) {
            logger.error("Error generating trade-off analysis", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Generate decision support questions for technology selection.
     * 
     * @param request Decision questions request
     * @return List of decision support questions
     */
    @PostMapping("/decision-questions")
    public ResponseEntity<DecisionQuestionsResponse> generateDecisionQuestions(@Valid @RequestBody DecisionQuestionsRequest request) {
        logger.info("POST /api/comparison/decision-questions - Generating questions for: {}", request.technologyNames());
        
        try {
            UserConstraints constraints = request.constraints() != null ? request.constraints() : UserConstraints.empty();
            
            List<String> questions = insightService.generateDecisionQuestions(
                request.technologyNames(), 
                constraints
            );
            
            return ResponseEntity.ok(new DecisionQuestionsResponse(questions));
            
        } catch (Exception e) {
            logger.error("Error generating decision questions", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Health check endpoint for monitoring comparison service performance.
     * 
     * @return Service health status
     */
    @GetMapping("/health")
    public ResponseEntity<HealthResponse> healthCheck() {
        logger.debug("GET /api/comparison/health");
        
        try {
            // Quick validation that all services are available
            long technologyCount = inventoryService.getAllTechnologies().size();
            long criteriaCount = inventoryService.getAllCriteria().size();
            
            return ResponseEntity.ok(new HealthResponse(
                "healthy",
                technologyCount,
                criteriaCount,
                System.currentTimeMillis()
            ));
            
        } catch (Exception e) {
            logger.error("Health check failed", e);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new HealthResponse("unhealthy", 0, 0, System.currentTimeMillis()));
        }
    }

    /**
     * Merge AI insights into the main comparison result.
     */
    private ComparisonResult mergeInsights(ComparisonResult mainResult, ComparisonResult insightResult) {
        // Create a new result with insights merged in
        return ComparisonResult.builder()
            .scores(mainResult.getScores())
            .radarData(mainResult.getRadarData())
            .kpiMetrics(mainResult.getKpiMetrics())
            .recommendationSummary(insightResult.getRecommendationSummary())
            .constraints(mainResult.getConstraints())
            .build();
    }

    // Request/Response DTOs

    public record ComparisonRequest(
        @NotEmpty(message = "At least one technology ID is required")
        @Size(max = 5, message = "Maximum 5 technologies can be compared at once")
        List<Long> technologyIds,
        
        Set<String> priorityTags,
        String projectType,
        String teamSize,
        String timeline,
        boolean includeInsights
    ) {
        public ComparisonRequest {
            if (priorityTags == null) {
                priorityTags = new HashSet<>();
            }
        }
    }

    public record ComparisonByNamesRequest(
        @NotEmpty(message = "At least one technology name is required")
        @Size(max = 5, message = "Maximum 5 technologies can be compared at once")
        List<String> technologyNames,
        
        Set<String> priorityTags,
        String projectType,
        String teamSize,
        String timeline,
        boolean includeInsights
    ) {
        public ComparisonByNamesRequest {
            if (priorityTags == null) {
                priorityTags = new HashSet<>();
            }
        }
    }

    public record InsightRequest(
        @NotEmpty(message = "At least one technology name is required")
        List<String> technologyNames,
        
        UserConstraints constraints
    ) {}

    public record TradeOffRequest(
        @NotEmpty(message = "At least two technology names are required for trade-off analysis")
        @Size(min = 2, message = "At least two technologies are required for trade-off analysis")
        List<String> technologyNames
    ) {}

    public record TradeOffResponse(
        String analysis
    ) {}

    public record DecisionQuestionsRequest(
        @NotEmpty(message = "At least one technology name is required")
        List<String> technologyNames,
        
        UserConstraints constraints
    ) {}

    public record DecisionQuestionsResponse(
        List<String> questions
    ) {}

    public record HealthResponse(
        String status,
        long technologyCount,
        long criteriaCount,
        long timestamp
    ) {}
}