package com.devdecision.inventory.infrastructure;

import com.devdecision.inventory.domain.Criteria;
import com.devdecision.inventory.domain.CriteriaType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Criteria entity operations.
 * Provides data access methods for criteria management.
 */
@Repository
public interface CriteriaRepository extends JpaRepository<Criteria, Long> {

    /**
     * Find criteria by name (case-insensitive)
     */
    Optional<Criteria> findByNameIgnoreCase(String name);

    /**
     * Find criteria by type
     */
    List<Criteria> findByType(CriteriaType type);

    /**
     * Find criteria by type ordered by weight descending
     */
    List<Criteria> findByTypeOrderByWeightDesc(CriteriaType type);

    /**
     * Find all criteria ordered by weight descending
     */
    List<Criteria> findAllByOrderByWeightDesc();

    /**
     * Find criteria with weight greater than specified value
     */
    @Query("SELECT c FROM Criteria c WHERE c.weight > :weight ORDER BY c.weight DESC")
    List<Criteria> findByWeightGreaterThan(@Param("weight") Double weight);

    /**
     * Find criteria by name containing text (case-insensitive)
     */
    List<Criteria> findByNameContainingIgnoreCase(String name);

    /**
     * Get all distinct criteria types
     */
    @Query("SELECT DISTINCT c.type FROM Criteria c ORDER BY c.type")
    List<CriteriaType> findAllTypes();
}