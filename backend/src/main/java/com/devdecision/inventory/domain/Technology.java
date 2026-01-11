package com.devdecision.inventory.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Objects;

/**
 * Technology entity representing a software technology, framework, or service
 * that can be compared in the DevDecision system.
 */
@Entity
@Table(name = "technologies", indexes = {
    @Index(name = "idx_technology_name", columnList = "name"),
    @Index(name = "idx_technology_category", columnList = "category")
})
public class Technology {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Technology name is required")
    @Size(max = 100, message = "Technology name must not exceed 100 characters")
    @Column(name = "name", nullable = false, unique = true, length = 100)
    private String name;

    @NotBlank(message = "Category is required")
    @Size(max = 50, message = "Category must not exceed 50 characters")
    @Column(name = "category", nullable = false, length = 50)
    private String category;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * Performance metrics stored as JSON map
     * Contains metrics like: github_stars, npm_downloads, job_openings, 
     * satisfaction_score, performance_score, learning_curve_score, community_score
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @Convert(converter = MapToJsonConverter.class)
    @Column(name = "metrics", columnDefinition = "TEXT")
    private Map<String, Double> metrics = new HashMap<>();

    /**
     * Technology tags for categorization and search
     */
    @JsonIgnore  // Ignore during JSON serialization to prevent lazy loading issues
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "technology_tags", 
                    joinColumns = @JoinColumn(name = "technology_id"))
    @Column(name = "tag")
    private Set<String> tags = new HashSet<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Default constructor for JPA
    protected Technology() {}

    public Technology(String name, String category) {
        this.name = name;
        this.category = category;
    }

    public Technology(String name, String category, String description) {
        this.name = name;
        this.category = category;
        this.description = description;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Map<String, Double> getMetrics() {
        return metrics;
    }

    public void setMetrics(Map<String, Double> metrics) {
        this.metrics = metrics != null ? metrics : new HashMap<>();
    }

    public Set<String> getTags() {
        return tags;
    }

    public void setTags(Set<String> tags) {
        this.tags = tags != null ? tags : new HashSet<>();
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    // Utility methods for metrics
    public void addMetric(String key, Double value) {
        this.metrics.put(key, value);
    }

    public Double getMetric(String key) {
        return this.metrics.get(key);
    }

    // Utility methods for tags
    public void addTag(String tag) {
        this.tags.add(tag.toLowerCase());
    }

    public void removeTag(String tag) {
        this.tags.remove(tag.toLowerCase());
    }

    public boolean hasTag(String tag) {
        return this.tags.contains(tag.toLowerCase());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Technology that = (Technology) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return "Technology{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", category='" + category + '\'' +
                ", description='" + description + '\'' +
                ", metricsCount=" + metrics.size() +
                ", tagsCount=" + tags.size() +
                ", createdAt=" + createdAt +
                '}';
    }

    /**
     * JPA Converter for Map<String, Double> to JSON string
     */
    @Converter
    public static class MapToJsonConverter implements AttributeConverter<Map<String, Double>, String> {
        
        private static final ObjectMapper objectMapper = new ObjectMapper();
        
        @Override
        public String convertToDatabaseColumn(Map<String, Double> attribute) {
            if (attribute == null || attribute.isEmpty()) {
                return "{}";
            }
            try {
                return objectMapper.writeValueAsString(attribute);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Error converting map to JSON", e);
            }
        }
        
        @Override
        public Map<String, Double> convertToEntityAttribute(String dbData) {
            if (dbData == null || dbData.trim().isEmpty()) {
                return new HashMap<>();
            }
            try {
                return objectMapper.readValue(dbData, 
                    objectMapper.getTypeFactory().constructMapType(HashMap.class, String.class, Double.class));
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Error converting JSON to map", e);
            }
        }
    }
}