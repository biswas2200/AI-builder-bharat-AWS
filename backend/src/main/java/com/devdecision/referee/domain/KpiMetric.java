package com.devdecision.referee.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Objects;

/**
 * Key Performance Indicator metric for technology comparison.
 * Contains metric name, value, and display formatting information.
 */
public class KpiMetric {
    
    @NotBlank
    private final String name;
    
    @NotNull
    private final Object value;
    
    @NotBlank
    private final String displayValue;
    
    private final String unit;
    private final String description;
    private final KpiMetricType type;

    public KpiMetric(String name, Object value, String displayValue) {
        this(name, value, displayValue, null, null, KpiMetricType.NUMERIC);
    }
    
    public KpiMetric(String name, Object value, String displayValue, String unit) {
        this(name, value, displayValue, unit, null, KpiMetricType.NUMERIC);
    }
    
    public KpiMetric(String name, Object value, String displayValue, String unit, String description, KpiMetricType type) {
        this.name = Objects.requireNonNull(name, "Name cannot be null");
        this.value = Objects.requireNonNull(value, "Value cannot be null");
        this.displayValue = Objects.requireNonNull(displayValue, "Display value cannot be null");
        this.unit = unit;
        this.description = description;
        this.type = type != null ? type : KpiMetricType.NUMERIC;
    }

    // Getters
    public String getName() {
        return name;
    }

    public Object getValue() {
        return value;
    }

    public String getDisplayValue() {
        return displayValue;
    }

    public String getUnit() {
        return unit;
    }

    public String getDescription() {
        return description;
    }

    public KpiMetricType getType() {
        return type;
    }
    
    /**
     * Get numeric value if the value is a number
     */
    public Double getNumericValue() {
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return null;
    }
    
    /**
     * Check if this metric has a unit
     */
    public boolean hasUnit() {
        return unit != null && !unit.trim().isEmpty();
    }
    
    /**
     * Get formatted display string with unit if available
     */
    public String getFormattedDisplay() {
        if (hasUnit()) {
            return displayValue + " " + unit;
        }
        return displayValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        KpiMetric kpiMetric = (KpiMetric) o;
        return Objects.equals(name, kpiMetric.name) &&
               Objects.equals(value, kpiMetric.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, value);
    }

    @Override
    public String toString() {
        return "KpiMetric{" +
                "name='" + name + '\'' +
                ", value=" + value +
                ", displayValue='" + displayValue + '\'' +
                ", unit='" + unit + '\'' +
                ", type=" + type +
                '}';
    }
    
    /**
     * Enum for different types of KPI metrics
     */
    public enum KpiMetricType {
        NUMERIC,        // Numeric values (stars, downloads, etc.)
        PERCENTAGE,     // Percentage values
        RATING,         // Rating scales (1-5 stars, etc.)
        COUNT,          // Count values (job openings, etc.)
        TREND,          // Trend indicators (up/down/stable)
        CATEGORICAL     // Categorical values (high/medium/low)
    }
}