package com.devdecision.inventory.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Objects;

/**
 * Criteria entity representing evaluation dimensions for technology comparison.
 * Examples: performance, learning curve, community support, documentation quality.
 */
@Entity
@Table(name = "criteria", indexes = {
    @Index(name = "idx_criteria_name", columnList = "name"),
    @Index(name = "idx_criteria_type", columnList = "criteria_type")
})
public class Criteria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Criteria name is required")
    @Size(max = 100, message = "Criteria name must not exceed 100 characters")
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @NotNull(message = "Weight is required")
    @DecimalMin(value = "0.0", message = "Weight must be non-negative")
    @DecimalMax(value = "10.0", message = "Weight must not exceed 10.0")
    @Column(name = "weight", nullable = false)
    private Double weight = 1.0;

    @NotNull(message = "Criteria type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "criteria_type", nullable = false, length = 50)
    private CriteriaType type;

    // Default constructor for JPA
    protected Criteria() {}

    public Criteria(String name, CriteriaType type) {
        this.name = name;
        this.type = type;
    }

    public Criteria(String name, String description, Double weight, CriteriaType type) {
        this.name = name;
        this.description = description;
        this.weight = weight;
        this.type = type;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    public CriteriaType getType() {
        return type;
    }

    public void setType(CriteriaType type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Criteria criteria = (Criteria) o;
        return Objects.equals(name, criteria.name) && type == criteria.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, type);
    }

    @Override
    public String toString() {
        return "Criteria{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", weight=" + weight +
                ", type=" + type +
                '}';
    }
}