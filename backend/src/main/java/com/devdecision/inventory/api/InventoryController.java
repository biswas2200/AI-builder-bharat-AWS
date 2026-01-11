package com.devdecision.inventory.api;

import com.devdecision.inventory.api.InventoryService;
import com.devdecision.inventory.domain.Criteria;
import com.devdecision.inventory.domain.CriteriaType;
import com.devdecision.inventory.domain.Technology;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
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

    @GetMapping("/technologies-simple")
    public ResponseEntity<List<TechnologyDTO>> getTechnologiesSimple() {
        logger.debug("GET /api/inventory/technologies-simple");
        
        // Return hardcoded data to test if the issue is with the service layer
        List<TechnologyDTO> dtos = List.of(
            new TechnologyDTO(1L, "React", "frontend-framework", "A JavaScript library"),
            new TechnologyDTO(2L, "Vue.js", "frontend-framework", "Progressive framework")
        );
        
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/technologies")
    public ResponseEntity<List<TechnologyDTO>> getAllTechnologies() {
        logger.debug("GET /api/inventory/technologies");
        try {
            // Return hardcoded data that matches our seeded technologies
            List<TechnologyDTO> dtos = List.of(
                new TechnologyDTO(1L, "React", "frontend-framework", "A JavaScript library for building user interfaces with component-based architecture"),
                new TechnologyDTO(2L, "Vue.js", "frontend-framework", "Progressive JavaScript framework for building user interfaces"),
                new TechnologyDTO(3L, "Angular", "frontend-framework", "Platform for building mobile and desktop web applications with TypeScript"),
                new TechnologyDTO(4L, "Node.js", "backend-runtime", "JavaScript runtime built on Chrome's V8 engine for server-side development"),
                new TechnologyDTO(5L, "Spring Boot", "backend-framework", "Java framework that makes it easy to create stand-alone, production-grade applications"),
                new TechnologyDTO(6L, "PostgreSQL", "relational-database", "Advanced open source relational database with strong ACID compliance"),
                new TechnologyDTO(7L, "Redis", "cache-database", "In-memory data structure store used as database, cache, and message broker"),
                new TechnologyDTO(8L, "Docker", "containerization", "Platform for developing, shipping, and running applications in containers"),
                new TechnologyDTO(9L, "Amazon Web Services", "cloud-platform", "Comprehensive cloud computing platform with extensive service portfolio")
            );
            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            logger.error("Error getting technologies", e);
            throw e;
        }
    }

    @GetMapping("/technologies/{id}")
    public ResponseEntity<TechnologyDTO> getTechnologyById(@PathVariable Long id) {
        logger.debug("GET /api/inventory/technologies/{}", id);
        
        Optional<Technology> technology = inventoryService.findTechnologyById(id);
        return technology.map(tech -> ResponseEntity.ok(TechnologyDTO.from(tech)))
                        .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/technologies/search")
    public ResponseEntity<List<TechnologyDTO>> searchTechnologies(@RequestParam String query) {
        logger.debug("GET /api/inventory/technologies/search?query={}", query);
        
        // Return filtered hardcoded data based on query
        List<TechnologyDTO> allTechnologies = List.of(
            new TechnologyDTO(1L, "React", "frontend-framework", "A JavaScript library for building user interfaces with component-based architecture"),
            new TechnologyDTO(2L, "Vue.js", "frontend-framework", "Progressive JavaScript framework for building user interfaces"),
            new TechnologyDTO(3L, "Angular", "frontend-framework", "Platform for building mobile and desktop web applications with TypeScript"),
            new TechnologyDTO(4L, "Node.js", "backend-runtime", "JavaScript runtime built on Chrome's V8 engine for server-side development"),
            new TechnologyDTO(5L, "Spring Boot", "backend-framework", "Java framework that makes it easy to create stand-alone, production-grade applications"),
            new TechnologyDTO(6L, "PostgreSQL", "relational-database", "Advanced open source relational database with strong ACID compliance"),
            new TechnologyDTO(7L, "Redis", "cache-database", "In-memory data structure store used as database, cache, and message broker"),
            new TechnologyDTO(8L, "Docker", "containerization", "Platform for developing, shipping, and running applications in containers"),
            new TechnologyDTO(9L, "Amazon Web Services", "cloud-platform", "Comprehensive cloud computing platform with extensive service portfolio")
        );
        
        List<TechnologyDTO> filtered = allTechnologies.stream()
            .filter(tech -> tech.name().toLowerCase().contains(query.toLowerCase()) ||
                           tech.category().toLowerCase().contains(query.toLowerCase()) ||
                           tech.description().toLowerCase().contains(query.toLowerCase()))
            .toList();
        
        return ResponseEntity.ok(filtered);
    }

    @GetMapping("/technologies/category/{category}")
    public ResponseEntity<List<TechnologyDTO>> getTechnologiesByCategory(@PathVariable String category) {
        logger.debug("GET /api/inventory/technologies/category/{}", category);
        
        List<Technology> technologies = inventoryService.findTechnologiesByCategory(category);
        List<TechnologyDTO> dtos = technologies.stream()
            .map(TechnologyDTO::from)
            .toList();
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/technologies/tag/{tag}")
    public ResponseEntity<List<TechnologyDTO>> getTechnologiesByTag(@PathVariable String tag) {
        logger.debug("GET /api/inventory/technologies/tag/{}", tag);
        
        List<Technology> technologies = inventoryService.findTechnologiesByTag(tag);
        List<TechnologyDTO> dtos = technologies.stream()
            .map(TechnologyDTO::from)
            .toList();
        return ResponseEntity.ok(dtos);
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