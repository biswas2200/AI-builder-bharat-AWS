package com.devdecision.inventory.internal;

import com.devdecision.inventory.domain.Criteria;
import com.devdecision.inventory.domain.CriteriaType;
import com.devdecision.inventory.domain.Technology;
import com.devdecision.inventory.infrastructure.CriteriaRepository;
import com.devdecision.inventory.infrastructure.TechnologyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Service responsible for seeding the database with comprehensive technology and criteria data.
 * Runs on application startup to ensure the system has a rich dataset for comparisons.
 */
@Service
public class DataSeedingService implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeedingService.class);

    private final TechnologyRepository technologyRepository;
    private final CriteriaRepository criteriaRepository;

    public DataSeedingService(TechnologyRepository technologyRepository, 
                             CriteriaRepository criteriaRepository) {
        this.technologyRepository = technologyRepository;
        this.criteriaRepository = criteriaRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        log.info("Starting database seeding process...");
        
        try {
            seedCriteria();
            seedTechnologies();
            log.info("Database seeding completed successfully");
        } catch (Exception e) {
            log.error("Database seeding failed", e);
            throw e;
        }
    }

    private void seedCriteria() {
        if (criteriaRepository.count() > 0) {
            log.info("Criteria already exist, skipping criteria seeding");
            return;
        }

        log.info("Seeding criteria data...");

        List<Criteria> criteria = List.of(
            new Criteria("Performance", "Speed, throughput, and efficiency metrics", 1.0, CriteriaType.PERFORMANCE),
            new Criteria("Learning Curve", "Ease of adoption and time to productivity", 1.0, CriteriaType.LEARNING_CURVE),
            new Criteria("Community Support", "Active community, forums, and ecosystem", 1.0, CriteriaType.COMMUNITY),
            new Criteria("Documentation Quality", "Completeness and clarity of documentation", 1.0, CriteriaType.DOCUMENTATION),
            new Criteria("Scalability", "Ability to handle growth and scale", 1.0, CriteriaType.SCALABILITY),
            new Criteria("Security", "Security features and track record", 1.0, CriteriaType.SECURITY),
            new Criteria("Maturity", "Stability and production readiness", 1.0, CriteriaType.MATURITY),
            new Criteria("Developer Experience", "Development velocity and tooling", 1.0, CriteriaType.DEVELOPER_EXPERIENCE),
            new Criteria("Cost", "Licensing, hosting, and operational costs", 1.0, CriteriaType.COST)
        );

        criteriaRepository.saveAll(criteria);
        log.info("Seeded {} criteria", criteria.size());
    }

    private void seedTechnologies() {
        if (technologyRepository.count() > 0) {
            log.info("Technologies already exist, skipping technology seeding");
            return;
        }

        log.info("Seeding technology data...");

        List<Technology> technologies = List.of(
            createReact(),
            createVue(),
            createAngular(),
            createNodeJS(),
            createSpringBoot(),
            createPostgreSQL(),
            createRedis(),
            createDocker(),
            createAWS()
        );

        technologyRepository.saveAll(technologies);
        log.info("Seeded {} technologies", technologies.size());
    }

    private Technology createReact() {
        Technology tech = new Technology("React", "frontend-framework", 
            "A JavaScript library for building user interfaces with component-based architecture");
        
        Map<String, Double> metrics = new HashMap<>();
        metrics.put("github_stars", 220000.0);
        metrics.put("npm_downloads", 20500000.0);
        metrics.put("job_openings", 85000.0);
        metrics.put("satisfaction_score", 8.7);
        metrics.put("performance_score", 8.5);
        metrics.put("learning_curve_score", 7.3);
        metrics.put("community_score", 9.8);
        tech.setMetrics(metrics);
        // tech.setTags(Set.of("javascript", "frontend", "popular")); // Temporarily disabled for faster startup
        
        return tech;
    }
    
    private Technology createVue() {
        Technology tech = new Technology("Vue.js", "frontend-framework", 
            "Progressive JavaScript framework for building user interfaces");
        
        Map<String, Double> metrics = new HashMap<>();
        metrics.put("github_stars", 206000.0);
        metrics.put("npm_downloads", 4200000.0);
        metrics.put("job_openings", 35000.0);
        metrics.put("satisfaction_score", 9.1);
        metrics.put("performance_score", 8.8);
        metrics.put("learning_curve_score", 8.9);
        metrics.put("community_score", 8.7);
        tech.setMetrics(metrics);
        // tech.setTags(Set.of("javascript", "frontend", "progressive")); // Temporarily disabled for faster startup
        
        return tech;
    }
    
    private Technology createAngular() {
        Technology tech = new Technology("Angular", "frontend-framework", 
            "Platform for building mobile and desktop web applications with TypeScript");
        
        Map<String, Double> metrics = new HashMap<>();
        metrics.put("github_stars", 93000.0);
        metrics.put("npm_downloads", 3100000.0);
        metrics.put("job_openings", 42000.0);
        metrics.put("satisfaction_score", 7.8);
        metrics.put("performance_score", 8.4);
        metrics.put("learning_curve_score", 6.2);
        metrics.put("community_score", 8.9);
        tech.setMetrics(metrics);
        // tech.setTags(Set.of("typescript", "frontend", "enterprise")); // Temporarily disabled for faster startup
        
        return tech;
    }
    
    private Technology createNodeJS() {
        Technology tech = new Technology("Node.js", "backend-runtime", 
            "JavaScript runtime built on Chrome's V8 engine for server-side development");
        
        Map<String, Double> metrics = new HashMap<>();
        metrics.put("github_stars", 104000.0);
        metrics.put("npm_downloads", 45000000.0);
        metrics.put("job_openings", 78000.0);
        metrics.put("satisfaction_score", 8.6);
        metrics.put("performance_score", 8.7);
        metrics.put("learning_curve_score", 8.1);
        metrics.put("community_score", 9.6);
        tech.setMetrics(metrics);
        // tech.setTags(Set.of("javascript", "backend", "runtime")); // Temporarily disabled for faster startup
        
        return tech;
    }
    
    private Technology createSpringBoot() {
        Technology tech = new Technology("Spring Boot", "backend-framework", 
            "Java framework that makes it easy to create stand-alone, production-grade applications");
        
        Map<String, Double> metrics = new HashMap<>();
        metrics.put("github_stars", 72000.0);
        metrics.put("job_openings", 55000.0);
        metrics.put("satisfaction_score", 8.1);
        metrics.put("performance_score", 8.6);
        metrics.put("learning_curve_score", 6.8);
        metrics.put("community_score", 9.0);
        tech.setMetrics(metrics);
        // tech.setTags(Set.of("java", "backend", "enterprise")); // Temporarily disabled for faster startup
        
        return tech;
    }
    
    private Technology createPostgreSQL() {
        Technology tech = new Technology("PostgreSQL", "relational-database", 
            "Advanced open source relational database with strong ACID compliance");
        
        Map<String, Double> metrics = new HashMap<>();
        metrics.put("github_stars", 15000.0);
        metrics.put("job_openings", 28000.0);
        metrics.put("satisfaction_score", 8.8);
        metrics.put("performance_score", 8.9);
        metrics.put("learning_curve_score", 7.4);
        metrics.put("community_score", 9.1);
        tech.setMetrics(metrics);
        // tech.setTags(Set.of("sql", "relational", "open-source")); // Temporarily disabled for faster startup
        
        return tech;
    }
    
    private Technology createRedis() {
        Technology tech = new Technology("Redis", "cache-database", 
            "In-memory data structure store used as database, cache, and message broker");
        
        Map<String, Double> metrics = new HashMap<>();
        metrics.put("github_stars", 64000.0);
        metrics.put("job_openings", 18000.0);
        metrics.put("satisfaction_score", 9.0);
        metrics.put("performance_score", 9.8);
        metrics.put("learning_curve_score", 8.5);
        metrics.put("community_score", 8.9);
        tech.setMetrics(metrics);
        // tech.setTags(Set.of("cache", "in-memory", "fast")); // Temporarily disabled for faster startup
        
        return tech;
    }
    
    private Technology createDocker() {
        Technology tech = new Technology("Docker", "containerization", 
            "Platform for developing, shipping, and running applications in containers");
        
        Map<String, Double> metrics = new HashMap<>();
        metrics.put("github_stars", 67000.0);
        metrics.put("job_openings", 45000.0);
        metrics.put("satisfaction_score", 8.9);
        metrics.put("performance_score", 8.6);
        metrics.put("learning_curve_score", 7.8);
        metrics.put("community_score", 9.5);
        tech.setMetrics(metrics);
        // tech.setTags(Set.of("containers", "devops", "deployment")); // Temporarily disabled for faster startup
        
        return tech;
    }
    
    private Technology createAWS() {
        Technology tech = new Technology("Amazon Web Services", "cloud-platform", 
            "Comprehensive cloud computing platform with extensive service portfolio");
        
        Map<String, Double> metrics = new HashMap<>();
        metrics.put("github_stars", 85000.0);
        metrics.put("job_openings", 45000.0);
        metrics.put("satisfaction_score", 8.2);
        metrics.put("performance_score", 9.1);
        metrics.put("learning_curve_score", 6.5);
        metrics.put("community_score", 9.3);
        tech.setMetrics(metrics);
        // tech.setTags(Set.of("cloud", "infrastructure", "enterprise")); // Temporarily disabled for faster startup
        
        return tech;
    }
}