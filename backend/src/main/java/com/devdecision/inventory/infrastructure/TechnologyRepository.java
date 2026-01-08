package com.devdecision.inventory.infrastructure;

import com.devdecision.inventory.domain.Technology;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Technology entity operations.
 * Provides data access methods for technology search and retrieval.
 */
@Repository
public interface TechnologyRepository extends JpaRepository<Technology, Long> {

    /**
     * Find technology by name (case-insensitive)
     */
    Optional<Technology> findByNameIgnoreCase(String name);

    /**
     * Find technologies by category (case-insensitive)
     */
    List<Technology> findByCategoryIgnoreCase(String category);

    /**
     * Find technologies by category containing text (case-insensitive)
     */
    List<Technology> findByCategoryContainingIgnoreCase(String category);

    /**
     * Search technologies by name containing text (case-insensitive)
     */
    List<Technology> findByNameContainingIgnoreCase(String name);

    /**
     * Complex search query that searches across name, category, and tags
     * Uses JPQL with LOWER function for case-insensitive search
     */
    @Query("""
        SELECT DISTINCT t FROM Technology t 
        LEFT JOIN t.tags tag 
        WHERE LOWER(t.name) LIKE LOWER(CONCAT('%', :query, '%')) 
           OR LOWER(t.category) LIKE LOWER(CONCAT('%', :query, '%'))
           OR LOWER(tag) LIKE LOWER(CONCAT('%', :query, '%'))
        ORDER BY t.name
        """)
    List<Technology> searchTechnologies(@Param("query") String query);

    /**
     * Find technologies that have a specific tag (case-insensitive)
     */
    @Query("""
        SELECT t FROM Technology t 
        JOIN t.tags tag 
        WHERE LOWER(tag) = LOWER(:tag)
        ORDER BY t.name
        """)
    List<Technology> findByTagIgnoreCase(@Param("tag") String tag);

    /**
     * Find technologies by multiple tags (case-insensitive)
     * Returns technologies that have ANY of the specified tags
     */
    @Query("""
        SELECT DISTINCT t FROM Technology t 
        JOIN t.tags tag 
        WHERE LOWER(tag) IN :tags
        ORDER BY t.name
        """)
    List<Technology> findByTagsInIgnoreCase(@Param("tags") List<String> tags);

    /**
     * Get all distinct categories
     */
    @Query("SELECT DISTINCT t.category FROM Technology t ORDER BY t.category")
    List<String> findAllCategories();

    /**
     * Get all distinct tags
     */
    @Query("SELECT DISTINCT tag FROM Technology t JOIN t.tags tag ORDER BY tag")
    List<String> findAllTags();
}