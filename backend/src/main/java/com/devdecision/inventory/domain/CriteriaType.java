package com.devdecision.inventory.domain;

/**
 * Enumeration of criteria types for technology evaluation.
 * These types correspond to the standard evaluation dimensions
 * supported by the DevDecision system.
 */
public enum CriteriaType {
    
    /**
     * Performance-related metrics (speed, throughput, efficiency)
     */
    PERFORMANCE("Performance"),
    
    /**
     * Learning curve and ease of adoption
     */
    LEARNING_CURVE("Learning Curve"),
    
    /**
     * Community support and ecosystem
     */
    COMMUNITY("Community Support"),
    
    /**
     * Documentation quality and availability
     */
    DOCUMENTATION("Documentation Quality"),
    
    /**
     * Scalability characteristics
     */
    SCALABILITY("Scalability"),
    
    /**
     * Security features and track record
     */
    SECURITY("Security"),
    
    /**
     * Maturity and stability
     */
    MATURITY("Maturity"),
    
    /**
     * Development velocity and productivity
     */
    DEVELOPER_EXPERIENCE("Developer Experience"),
    
    /**
     * Cost considerations (licensing, hosting, etc.)
     */
    COST("Cost"),
    
    /**
     * Custom criteria defined by users
     */
    CUSTOM("Custom");

    private final String displayName;

    CriteriaType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Get CriteriaType from string, case-insensitive
     */
    public static CriteriaType fromString(String value) {
        if (value == null) {
            return null;
        }
        
        for (CriteriaType type : CriteriaType.values()) {
            if (type.name().equalsIgnoreCase(value) || 
                type.displayName.equalsIgnoreCase(value)) {
                return type;
            }
        }
        
        throw new IllegalArgumentException("Unknown criteria type: " + value);
    }

    @Override
    public String toString() {
        return displayName;
    }
}