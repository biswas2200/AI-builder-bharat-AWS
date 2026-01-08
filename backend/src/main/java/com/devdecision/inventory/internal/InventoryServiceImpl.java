package com.devdecision.inventory.internal;

import com.devdecision.inventory.api.InventoryService;
import com.devdecision.inventory.domain.Criteria;
import com.devdecision.inventory.domain.CriteriaType;
import com.devdecision.inventory.domain.Technology;
import com.devdecision.inventory.infrastructure.CriteriaRepository;
import com.devdecision.inventory.infrastructure.TechnologyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

/**
 * Implementation of the InventoryService interface.
 * Provides technology and criteria management with caching support.
 */
@Service
@Transactional(readOnly = true)
public class InventoryServiceImpl implements InventoryService {

    private static final Logger logger = LoggerFactory.getLogger(InventoryServiceImpl.class);

    private final TechnologyRepository technologyRepository;
    private final CriteriaRepository criteriaRepository;

    public InventoryServiceImpl(TechnologyRepository technologyRepository, 
                               CriteriaRepository criteriaRepository) {
        this.technologyRepository = technologyRepository;
        this.criteriaRepository = criteriaRepository;
    }

    // Technology operations

    @Override
    @Cacheable(value = "technologies", key = "'category:' + #category")
    public List<Technology> findTechnologiesByCategory(String category) {
        logger.debug("Finding technologies by category: {}", category);
        
        if (!StringUtils.hasText(category)) {
            return List.of();
        }
        
        return technologyRepository.findByCategoryIgnoreCase(category.trim());
    }

    @Override
    @Cacheable(value = "technologies", key = "'search:' + #query")
    public List<Technology> searchTechnologies(String query) {
        logger.debug("Searching technologies with query: {}", query);
        
        if (!StringUtils.hasText(query)) {
            return getAllTechnologies();
        }
        
        String trimmedQuery = query.trim();
        return technologyRepository.searchTechnologies(trimmedQuery);
    }

    @Override
    @Cacheable(value = "technologies", key = "'id:' + #id")
    public Optional<Technology> findTechnologyById(Long id) {
        logger.debug("Finding technology by ID: {}", id);
        
        if (id == null) {
            return Optional.empty();
        }
        
        return technologyRepository.findById(id);
    }

    @Override
    @Cacheable(value = "technologies", key = "'name:' + #name")
    public Optional<Technology> findTechnologyByName(String name) {
        logger.debug("Finding technology by name: {}", name);
        
        if (!StringUtils.hasText(name)) {
            return Optional.empty();
        }
        
        return technologyRepository.findByNameIgnoreCase(name.trim());
    }

    @Override
    @Cacheable(value = "technologies", key = "'ids:' + #ids.toString()")
    public List<Technology> findTechnologiesByIds(List<Long> ids) {
        logger.debug("Finding technologies by IDs: {}", ids);
        
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        
        // Use optimized batch query for comparison operations
        return technologyRepository.findByIdsWithTags(ids);
    }

    @Override
    @Cacheable(value = "technologies", key = "'all'")
    public List<Technology> getAllTechnologies() {
        logger.debug("Getting all technologies");
        return technologyRepository.findAll();
    }

    @Override
    @Transactional
    @CacheEvict(value = "technologies", allEntries = true)
    public Technology saveTechnology(Technology technology) {
        logger.info("Saving technology: {}", technology.getName());
        
        if (technology == null) {
            throw new IllegalArgumentException("Technology cannot be null");
        }
        
        if (!StringUtils.hasText(technology.getName())) {
            throw new IllegalArgumentException("Technology name is required");
        }
        
        if (!StringUtils.hasText(technology.getCategory())) {
            throw new IllegalArgumentException("Technology category is required");
        }
        
        return technologyRepository.save(technology);
    }

    @Override
    @Transactional
    @CacheEvict(value = "technologies", allEntries = true)
    public Technology createCustomTechnology(String name, String category, String description) {
        logger.info("Creating custom technology: {} in category: {}", name, category);
        
        if (!StringUtils.hasText(name)) {
            throw new IllegalArgumentException("Technology name is required");
        }
        
        if (!StringUtils.hasText(category)) {
            throw new IllegalArgumentException("Technology category is required");
        }
        
        // Check if technology with same name already exists
        Optional<Technology> existing = findTechnologyByName(name);
        if (existing.isPresent()) {
            throw new IllegalArgumentException("Technology with name '" + name + "' already exists");
        }
        
        Technology technology = new Technology(name.trim(), category.trim(), description);
        return saveTechnology(technology);
    }

    @Override
    @Transactional
    @CacheEvict(value = "technologies", allEntries = true)
    public void deleteTechnology(Long id) {
        logger.info("Deleting technology with ID: {}", id);
        
        if (id == null) {
            throw new IllegalArgumentException("Technology ID cannot be null");
        }
        
        if (!technologyRepository.existsById(id)) {
            throw new IllegalArgumentException("Technology with ID " + id + " not found");
        }
        
        technologyRepository.deleteById(id);
    }

    @Override
    @Cacheable(value = "technologies", key = "'tag:' + #tag")
    public List<Technology> findTechnologiesByTag(String tag) {
        logger.debug("Finding technologies by tag: {}", tag);
        
        if (!StringUtils.hasText(tag)) {
            return List.of();
        }
        
        return technologyRepository.findByTagIgnoreCase(tag.trim());
    }

    @Override
    @Cacheable(value = "technologies", key = "'categories'")
    public List<String> getAllCategories() {
        logger.debug("Getting all categories");
        return technologyRepository.findAllCategories();
    }

    @Override
    @Cacheable(value = "technologies", key = "'tags'")
    public List<String> getAllTags() {
        logger.debug("Getting all tags");
        return technologyRepository.findAllTags();
    }

    // Criteria operations

    @Override
    @Cacheable(value = "criteria", key = "'all'")
    public List<Criteria> getAllCriteria() {
        logger.debug("Getting all criteria");
        return criteriaRepository.findAllByOrderByWeightDesc();
    }

    @Override
    @Cacheable(value = "criteria", key = "'id:' + #id")
    public Optional<Criteria> findCriteriaById(Long id) {
        logger.debug("Finding criteria by ID: {}", id);
        
        if (id == null) {
            return Optional.empty();
        }
        
        return criteriaRepository.findById(id);
    }

    @Override
    @Cacheable(value = "criteria", key = "'name:' + #name")
    public Optional<Criteria> findCriteriaByName(String name) {
        logger.debug("Finding criteria by name: {}", name);
        
        if (!StringUtils.hasText(name)) {
            return Optional.empty();
        }
        
        return criteriaRepository.findByNameIgnoreCase(name.trim());
    }

    @Override
    @Cacheable(value = "criteria", key = "'type:' + #type")
    public List<Criteria> findCriteriaByType(CriteriaType type) {
        logger.debug("Finding criteria by type: {}", type);
        
        if (type == null) {
            return List.of();
        }
        
        return criteriaRepository.findByTypeOrderByWeightDesc(type);
    }

    @Override
    @Transactional
    @CacheEvict(value = "criteria", allEntries = true)
    public Criteria saveCriteria(Criteria criteria) {
        logger.info("Saving criteria: {}", criteria.getName());
        
        if (criteria == null) {
            throw new IllegalArgumentException("Criteria cannot be null");
        }
        
        if (!StringUtils.hasText(criteria.getName())) {
            throw new IllegalArgumentException("Criteria name is required");
        }
        
        if (criteria.getType() == null) {
            throw new IllegalArgumentException("Criteria type is required");
        }
        
        if (criteria.getWeight() == null || criteria.getWeight() < 0) {
            throw new IllegalArgumentException("Criteria weight must be non-negative");
        }
        
        return criteriaRepository.save(criteria);
    }

    @Override
    @Transactional
    @CacheEvict(value = "criteria", allEntries = true)
    public Criteria createCustomCriteria(String name, String description, Double weight, CriteriaType type) {
        logger.info("Creating custom criteria: {} of type: {}", name, type);
        
        if (!StringUtils.hasText(name)) {
            throw new IllegalArgumentException("Criteria name is required");
        }
        
        if (type == null) {
            throw new IllegalArgumentException("Criteria type is required");
        }
        
        if (weight == null || weight < 0) {
            throw new IllegalArgumentException("Criteria weight must be non-negative");
        }
        
        // Check if criteria with same name already exists
        Optional<Criteria> existing = findCriteriaByName(name);
        if (existing.isPresent()) {
            throw new IllegalArgumentException("Criteria with name '" + name + "' already exists");
        }
        
        Criteria criteria = new Criteria(name.trim(), description, weight, type);
        return saveCriteria(criteria);
    }

    @Override
    @Transactional
    @CacheEvict(value = "criteria", allEntries = true)
    public void deleteCriteria(Long id) {
        logger.info("Deleting criteria with ID: {}", id);
        
        if (id == null) {
            throw new IllegalArgumentException("Criteria ID cannot be null");
        }
        
        if (!criteriaRepository.existsById(id)) {
            throw new IllegalArgumentException("Criteria with ID " + id + " not found");
        }
        
        criteriaRepository.deleteById(id);
    }
}