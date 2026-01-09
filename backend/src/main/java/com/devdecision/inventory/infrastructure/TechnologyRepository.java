package com.devdecision.inventory.infrastructure;

import com.devdecision.inventory.domain.Technology;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.QueryHint;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Technology entity operations.
 * Provides data access methods for technology search and retrieval with performance optimizations.
 */
@Repository
public interface TechnologyRepository extends JpaRepository<Technology, Long> {

    /**
     * Find technology by name (case-insensitive) with query hints for performance
     */
    @QueryHints({
        @QueryHint(name = "org.hibernate.cacheable", value = "true"),
        @QueryHint(name = "org.hibernate.cacheMode", value = "NORMAL")
    })
    Optional<Technology> findByNameIgnoreCase(String name);

    /**
     * Find technologies by category (case-insensitive) with performance optimization
     */
    @QueryHints({
        @QueryHint(name = "org.hibernate.cacheable", value = "true"),
        @QueryHint(name = "org.hibernate.fetchSize", value = "50")
    })
    List<Technology> findByCategoryIgnoreCase(String category);

    /**
     * Find technologies by category containing text (case-insensitive)
     */
    List<Technology> findByCategoryContainingIgnoreCase(String category);

    /**
     * Find technologies by name containing text (case-insensitive)
     */
    List<Technology> findByNameContainingIgnoreCase(String name);

    /**
     * Optimized search query for comparison operations.
     * Uses batch fetching and query hints for performance.
     */
    @Query("""
        SELECT DISTINCT t FROM Technology t 
        LEFT JOIN FETCH t.tags 
        WHERE t.id IN :ids
        ORDER BY t.name
        """)
    @QueryHints({
        @QueryHint(name = "org.hibernate.cacheable", value = "true"),
        @QueryHint(name = "org.hibernate.fetchSize", value = "5")
    })
    List<Technology> findByIdsWithTags(@Param("ids") List<Long> ids);

    /**
     * Complex search query that searches across name, category, and tags
     * Optimized with proper indexing hints and fetch strategy
     */
    @Query("""
        SELECT DISTINCT t FROM Technology t 
        LEFT JOIN t.tags tag 
        WHERE LOWER(t.name) LIKE LOWER(CONCAT('%', :query, '%')) 
           OR LOWER(t.category) LIKE LOWER(CONCAT('%', :query, '%'))
           OR LOWER(tag) LIKE LOWER(CONCAT('%', :query, '%'))
        ORDER BY 
            CASE WHEN LOWER(t.name) = LOWER(:query) THEN 1
                 WHEN LOWER(t.name) LIKE LOWER(CONCAT(:query, '%')) THEN 2
                 WHEN LOWER(t.category) = LOWER(:query) THEN 3
                 ELSE 4 END,
            t.name
        """)
    @QueryHints({
        @QueryHint(name = "org.hibernate.cacheable", value = "true"),
        @QueryHint(name = "org.hibernate.fetchSize", value = "20")
    })
    List<Technology> searchTechnologies(@Param("query") String query);

    /**
     * Simplified search query for testing with H2 database
     * Removes complex ORDER BY to avoid H2 DISTINCT compatibility issues
     */
    @Query("""
        SELECT DISTINCT t FROM Technology t 
        LEFT JOIN t.tags tag 
        WHERE LOWER(t.name) LIKE LOWER(CONCAT('%', :query, '%')) 
           OR LOWER(t.category) LIKE LOWER(CONCAT('%', :query, '%'))
           OR LOWER(tag) LIKE LOWER(CONCAT('%', :query, '%'))
        ORDER BY t.name
        """)
    @QueryHints({
        @QueryHint(name = "org.hibernate.cacheable", value = "true"),
        @QueryHint(name = "org.hibernate.fetchSize", value = "20")
    })
    List<Technology> searchTechnologiesSimple(@Param("query") String query);

    /**
     * Find technologies that have a specific tag (case-insensitive)
     * Optimized for tag-based filtering
     */
    @Query("""
        SELECT t FROM Technology t 
        JOIN t.tags tag 
        WHERE LOWER(tag) = LOWER(:tag)
        ORDER BY t.name
        """)
    @QueryHints({
        @QueryHint(name = "org.hibernate.cacheable", value = "true")
    })
    List<Technology> findByTagIgnoreCase(@Param("tag") String tag);

    /**
     * Find technologies by multiple tags (case-insensitive)
     * Returns technologies that have ANY of the specified tags
     * Optimized for multiple tag queries
     */
    @Query("""
        SELECT DISTINCT t FROM Technology t 
        JOIN t.tags tag 
        WHERE LOWER(tag) IN :tags
        ORDER BY t.name
        """)
    @QueryHints({
        @QueryHint(name = "org.hibernate.cacheable", value = "true"),
        @QueryHint(name = "org.hibernate.fetchSize", value = "50")
    })
    List<Technology> findByTagsInIgnoreCase(@Param("tags") List<String> tags);

    /**
     * Get all distinct categories with caching
     */
    @Query("SELECT DISTINCT t.category FROM Technology t ORDER BY t.category")
    @QueryHints({
        @QueryHint(name = "org.hibernate.cacheable", value = "true"),
        @QueryHint(name = "org.hibernate.cacheRegion", value = "categories")
    })
    List<String> findAllCategories();

    /**
     * Get all distinct tags with caching
     */
    @Query("SELECT DISTINCT tag FROM Technology t JOIN t.tags tag ORDER BY tag")
    @QueryHints({
        @QueryHint(name = "org.hibernate.cacheable", value = "true"),
        @QueryHint(name = "org.hibernate.cacheRegion", value = "tags")
    })
    List<String> findAllTags();

    /**
     * Count technologies by category for analytics
     */
    @Query("SELECT t.category, COUNT(t) FROM Technology t GROUP BY t.category ORDER BY COUNT(t) DESC")
    @QueryHints({
        @QueryHint(name = "org.hibernate.cacheable", value = "true")
    })
    List<Object[]> countTechnologiesByCategory();

    /**
     * Find top N technologies by a specific metric for performance comparisons
     */
    @Query(value = """
        SELECT * FROM technologies t 
        WHERE t.metrics ->> :metricKey IS NOT NULL 
        ORDER BY CAST(t.metrics ->> :metricKey AS NUMERIC) DESC 
        LIMIT :limit
        """, nativeQuery = true)
    @QueryHints({
        @QueryHint(name = "org.hibernate.cacheable", value = "true")
    })
    List<Technology> findTopByMetric(@Param("metricKey") String metricKey, @Param("limit") int limit);
}