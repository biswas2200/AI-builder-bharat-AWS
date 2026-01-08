package com.devdecision.inventory.api;

import com.devdecision.inventory.domain.Criteria;
import com.devdecision.inventory.domain.CriteriaType;
import com.devdecision.inventory.domain.Technology;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * REST controller for inventory operations.
 * Provides endpoints for technology and criteria management.
 */
@RestController
@RequestMapping("/api/inventory")
@CrossOrigin(origins = "http://localhost:3000")
public class InventoryController {

    private static final Logger logger = LoggerFactory.getLogger(InventoryController.class);

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    // Technology endpoints

    @GetMapping("/technologies")
    public ResponseEntity<List<Technology>> getAllTechnologies() {
        logger.debug("GET /api/inventory/technologies");
        List<Technology> technologies = inventoryService.getAllTechnologies();
        return ResponseEntity.ok(technologies);
    }

    @GetMapping("/technologies/{id}")
    public ResponseEntity<Technology> getTechnologyById(@PathVariable Long id) {
        logger.debug("GET /api/inventory/technologies/{}", id);
        
        Optional<Technology> technology = inventoryService.findTechnologyById(id);
        return technology.map(ResponseEntity::ok)
                        .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/technologies/search")
    public ResponseEntity<List<Technology>> searchTechnologies(@RequestParam String query) {
        logger.debug("GET /api/inventory/technologies/search?query={}", query);
        
        List<Technology> technologies = inventoryService.searchTechnologies(query);
        return ResponseEntity.ok(technologies);
    }

    @GetMapping("/technologies/category/{category}")
    public ResponseEntity<List<Technology>> getTechnologiesByCategory(@PathVariable String category) {
        logger.debug("GET /api/inventory/technologies/category/{}", category);
        
        List<Technology> technologies = inventoryService.findTechnologiesByCategory(category);
        return ResponseEntity.ok(technologies);
    }

    @GetMapping("/technologies/tag/{tag}")
    public ResponseEntity<List<Technology>> getTechnologiesByTag(@PathVariable String tag) {
        logger.debug("GET /api/inventory/technologies/tag/{}", tag);
        
        List<Technology> technologies = inventoryService.findTechnologiesByTag(tag);
        return ResponseEntity.ok(technologies);
    }

    @PostMapping("/technologies")
    public ResponseEntity<Technology> createTechnology(@Valid @RequestBody CreateTechnologyRequest request) {
        logger.info("POST /api/inventory/technologies - Creating technology: {}", request.name());
        
        try {
            Technology technology = inventoryService.createCustomTechnology(
                request.name(), 
                request.category(), 
                request.description()
            );
            
            // Add metrics and tags if provided
            if (request.metrics() != null) {
                technology.setMetrics(request.metrics());
            }
            if (request.tags() != null) {
                technology.setTags(request.tags());
            }
            
            Technology savedTechnology = inventoryService.saveTechnology(technology);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedTechnology);
            
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid technology creation request: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/technologies/{id}")
    public ResponseEntity<Technology> updateTechnology(@PathVariable Long id, 
                                                      @Valid @RequestBody UpdateTechnologyRequest request) {
        logger.info("PUT /api/inventory/technologies/{}", id);
        
        Optional<Technology> existingTechnology = inventoryService.findTechnologyById(id);
        if (existingTechnology.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        try {
            Technology technology = existingTechnology.get();
            
            if (request.name() != null) {
                technology.setName(request.name());
            }
            if (request.category() != null) {
                technology.setCategory(request.category());
            }
            if (request.description() != null) {
                technology.setDescription(request.description());
            }
            if (request.metrics() != null) {
                technology.setMetrics(request.metrics());
            }
            if (request.tags() != null) {
                technology.setTags(request.tags());
            }
            
            Technology savedTechnology = inventoryService.saveTechnology(technology);
            return ResponseEntity.ok(savedTechnology);
            
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid technology update request: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/technologies/{id}")
    public ResponseEntity<Void> deleteTechnology(@PathVariable Long id) {
        logger.info("DELETE /api/inventory/technologies/{}", id);
        
        try {
            inventoryService.deleteTechnology(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            logger.warn("Technology deletion failed: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/categories")
    public ResponseEntity<List<String>> getAllCategories() {
        logger.debug("GET /api/inventory/categories");
        List<String> categories = inventoryService.getAllCategories();
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/tags")
    public ResponseEntity<List<String>> getAllTags() {
        logger.debug("GET /api/inventory/tags");
        List<String> tags = inventoryService.getAllTags();
        return ResponseEntity.ok(tags);
    }

    // Criteria endpoints

    @GetMapping("/criteria")
    public ResponseEntity<List<Criteria>> getAllCriteria() {
        logger.debug("GET /api/inventory/criteria");
        List<Criteria> criteria = inventoryService.getAllCriteria();
        return ResponseEntity.ok(criteria);
    }

    @GetMapping("/criteria/{id}")
    public ResponseEntity<Criteria> getCriteriaById(@PathVariable Long id) {
        logger.debug("GET /api/inventory/criteria/{}", id);
        
        Optional<Criteria> criteria = inventoryService.findCriteriaById(id);
        return criteria.map(ResponseEntity::ok)
                      .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/criteria/type/{type}")
    public ResponseEntity<List<Criteria>> getCriteriaByType(@PathVariable CriteriaType type) {
        logger.debug("GET /api/inventory/criteria/type/{}", type);
        
        List<Criteria> criteria = inventoryService.findCriteriaByType(type);
        return ResponseEntity.ok(criteria);
    }

    @PostMapping("/criteria")
    public ResponseEntity<Criteria> createCriteria(@Valid @RequestBody CreateCriteriaRequest request) {
        logger.info("POST /api/inventory/criteria - Creating criteria: {}", request.name());
        
        try {
            Criteria criteria = inventoryService.createCustomCriteria(
                request.name(),
                request.description(),
                request.weight(),
                request.type()
            );
            
            return ResponseEntity.status(HttpStatus.CREATED).body(criteria);
            
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid criteria creation request: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/criteria/{id}")
    public ResponseEntity<Void> deleteCriteria(@PathVariable Long id) {
        logger.info("DELETE /api/inventory/criteria/{}", id);
        
        try {
            inventoryService.deleteCriteria(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            logger.warn("Criteria deletion failed: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    // DTOs for request/response

    public record CreateTechnologyRequest(
        String name,
        String category,
        String description,
        java.util.Map<String, Double> metrics,
        java.util.Set<String> tags
    ) {}

    public record UpdateTechnologyRequest(
        String name,
        String category,
        String description,
        java.util.Map<String, Double> metrics,
        java.util.Set<String> tags
    ) {}

    public record CreateCriteriaRequest(
        String name,
        String description,
        Double weight,
        CriteriaType type
    ) {}
}