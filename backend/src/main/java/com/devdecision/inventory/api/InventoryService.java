package com.devdecision.inventory.api;

import com.devdecision.inventory.domain.Criteria;
import com.devdecision.inventory.domain.CriteriaType;
import com.devdecision.inventory.domain.Technology;

import java.util.List;
import java.util.Optional;

/**
 * Public API interface for the Inventory module.
 * This interface defines the contract for technology and criteria management
 * that other modules can depend on.
 */
public interface InventoryService {

    // Technology operations
    
    /**
     * Find all technologies by category
     * @param category the technology category to search for
     * @return list of technologies in the specified category
     */
    List<Technology> findTechnologiesByCategory(String category);

    /**
     * Search technologies by query string (searches name, category, and tags)
     * @param query the search query
     * @return list of technologies matching the query
     */
    List<Technology> searchTechnologies(String query);

    /**
     * Find technology by ID
     * @param id the technology ID
     * @return optional containing the technology if found
     */
    Optional<Technology> findTechnologyById(Long id);

    /**
     * Find technology by name (case-insensitive)
     * @param name the technology name
     * @return optional containing the technology if found
     */
    Optional<Technology> findTechnologyByName(String name);

    /**
     * Find technologies by IDs (optimized for batch operations)
     * @param ids list of technology IDs
     * @return list of technologies with the specified IDs
     */
    List<Technology> findTechnologiesByIds(List<Long> ids);

    /**
     * Get all technologies
     * @return list of all technologies
     */
    List<Technology> getAllTechnologies();

    /**
     * Save or update a technology
     * @param technology the technology to save
     * @return the saved technology
     */
    Technology saveTechnology(Technology technology);

    /**
     * Create a new custom technology
     * @param name technology name
     * @param category technology category
     * @param description technology description
     * @return the created technology
     */
    Technology createCustomTechnology(String name, String category, String description);

    /**
     * Delete technology by ID
     * @param id the technology ID to delete
     */
    void deleteTechnology(Long id);

    /**
     * Find technologies by tag
     * @param tag the tag to search for
     * @return list of technologies with the specified tag
     */
    List<Technology> findTechnologiesByTag(String tag);

    /**
     * Get all available categories
     * @return list of all technology categories
     */
    List<String> getAllCategories();

    /**
     * Get all available tags
     * @return list of all technology tags
     */
    List<String> getAllTags();

    // Criteria operations

    /**
     * Get all criteria
     * @return list of all evaluation criteria
     */
    List<Criteria> getAllCriteria();

    /**
     * Find criteria by ID
     * @param id the criteria ID
     * @return optional containing the criteria if found
     */
    Optional<Criteria> findCriteriaById(Long id);

    /**
     * Find criteria by name (case-insensitive)
     * @param name the criteria name
     * @return optional containing the criteria if found
     */
    Optional<Criteria> findCriteriaByName(String name);

    /**
     * Find criteria by type
     * @param type the criteria type
     * @return list of criteria of the specified type
     */
    List<Criteria> findCriteriaByType(CriteriaType type);

    /**
     * Save or update criteria
     * @param criteria the criteria to save
     * @return the saved criteria
     */
    Criteria saveCriteria(Criteria criteria);

    /**
     * Create new custom criteria
     * @param name criteria name
     * @param description criteria description
     * @param weight criteria weight
     * @param type criteria type
     * @return the created criteria
     */
    Criteria createCustomCriteria(String name, String description, Double weight, CriteriaType type);

    /**
     * Delete criteria by ID
     * @param id the criteria ID to delete
     */
    void deleteCriteria(Long id);
}