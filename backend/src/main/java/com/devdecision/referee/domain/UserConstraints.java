package com.devdecision.referee.domain;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Set;
import java.util.HashSet;
import java.util.Objects;

/**
 * User constraints and preferences for technology comparison.
 * Contains priority tags, project context, and other user-specified requirements.
 */
public record UserConstraints(
    @NotNull
    Set<String> priorityTags,
    
    String projectType,
    String teamSize,
    String timeline
) {
    
    /**
     * Constructor with validation and normalization
     */
    public UserConstraints {
        // Normalize priority tags to lowercase and ensure non-null
        if (priorityTags == null) {
            priorityTags = new HashSet<>();
        } else {
            priorityTags = priorityTags.stream()
                .map(String::toLowerCase)
                .collect(java.util.stream.Collectors.toSet());
        }
    }
    
    /**
     * Create UserConstraints with only priority tags
     */
    public static UserConstraints withPriorityTags(Set<String> priorityTags) {
        return new UserConstraints(priorityTags, null, null, null);
    }
    
    /**
     * Create empty UserConstraints
     */
    public static UserConstraints empty() {
        return new UserConstraints(new HashSet<>(), null, null, null);
    }
    
    /**
     * Check if a specific tag is prioritized by the user
     */
    public boolean hasPriorityTag(String tag) {
        return priorityTags.contains(tag.toLowerCase());
    }
    
    /**
     * Get the number of priority tags
     */
    public int getPriorityTagCount() {
        return priorityTags.size();
    }
}