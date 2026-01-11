package com.devdecision.inventory.api;

import com.devdecision.inventory.domain.Technology;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

/**
 * Data Transfer Object for Technology to avoid serialization issues
 */
public record TechnologyDTO(
    Long id,
    String name,
    String category,
    String description
) {
    
    public static TechnologyDTO from(Technology technology) {
        return new TechnologyDTO(
            technology.getId(),
            technology.getName(),
            technology.getCategory(),
            technology.getDescription()
        );
    }
}