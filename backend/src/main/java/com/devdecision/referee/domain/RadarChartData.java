package com.devdecision.referee.domain;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Objects;

/**
 * Data structure for radar chart visualization.
 * Contains subject (criterion name) and scores for each technology being compared.
 */
public class RadarChartData {
    
    @NotBlank
    private final String subject;
    
    @NotNull
    @DecimalMin("0.0")
    @DecimalMax("100.0")
    private final Double A;
    
    @DecimalMin("0.0")
    @DecimalMax("100.0")
    private final Double B;
    
    @DecimalMin("0.0")
    @DecimalMax("100.0")
    private final Double C;
    
    @DecimalMin("0.0")
    @DecimalMax("100.0")
    private final Double D;
    
    @DecimalMin("0.0")
    @DecimalMax("100.0")
    private final Double E;
    
    @NotNull
    @DecimalMin("0.0")
    @DecimalMax("100.0")
    private final Double fullMark;

    // Constructor for 2 technologies
    public RadarChartData(String subject, Double A, Double B, Double fullMark) {
        this(subject, A, B, null, null, null, fullMark);
    }
    
    // Constructor for 3 technologies
    public RadarChartData(String subject, Double A, Double B, Double C, Double fullMark) {
        this(subject, A, B, C, null, null, fullMark);
    }
    
    // Constructor for 4 technologies
    public RadarChartData(String subject, Double A, Double B, Double C, Double D, Double fullMark) {
        this(subject, A, B, C, D, null, fullMark);
    }
    
    // Full constructor for up to 5 technologies
    public RadarChartData(String subject, Double A, Double B, Double C, Double D, Double E, Double fullMark) {
        this.subject = Objects.requireNonNull(subject, "Subject cannot be null");
        this.A = Objects.requireNonNull(A, "Technology A score cannot be null");
        this.B = B;
        this.C = C;
        this.D = D;
        this.E = E;
        this.fullMark = Objects.requireNonNull(fullMark, "Full mark cannot be null");
        
        // Validate score ranges
        validateScore("A", A);
        if (B != null) validateScore("B", B);
        if (C != null) validateScore("C", C);
        if (D != null) validateScore("D", D);
        if (E != null) validateScore("E", E);
        validateScore("fullMark", fullMark);
    }
    
    private void validateScore(String name, Double score) {
        if (score < 0.0 || score > 100.0) {
            throw new IllegalArgumentException(name + " score must be between 0.0 and 100.0, got: " + score);
        }
    }

    // Getters
    public String getSubject() {
        return subject;
    }

    public Double getA() {
        return A;
    }

    public Double getB() {
        return B;
    }

    public Double getC() {
        return C;
    }

    public Double getD() {
        return D;
    }

    public Double getE() {
        return E;
    }

    public Double getFullMark() {
        return fullMark;
    }
    
    /**
     * Get the number of technologies being compared
     */
    public int getTechnologyCount() {
        int count = 1; // A is always present
        if (B != null) count++;
        if (C != null) count++;
        if (D != null) count++;
        if (E != null) count++;
        return count;
    }
    
    /**
     * Get score for technology by index (0-based)
     */
    public Double getScoreByIndex(int index) {
        return switch (index) {
            case 0 -> A;
            case 1 -> B;
            case 2 -> C;
            case 3 -> D;
            case 4 -> E;
            default -> throw new IllegalArgumentException("Invalid technology index: " + index);
        };
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RadarChartData that = (RadarChartData) o;
        return Objects.equals(subject, that.subject) &&
               Objects.equals(A, that.A) &&
               Objects.equals(B, that.B) &&
               Objects.equals(C, that.C) &&
               Objects.equals(D, that.D) &&
               Objects.equals(E, that.E) &&
               Objects.equals(fullMark, that.fullMark);
    }

    @Override
    public int hashCode() {
        return Objects.hash(subject, A, B, C, D, E, fullMark);
    }

    @Override
    public String toString() {
        return "RadarChartData{" +
                "subject='" + subject + '\'' +
                ", A=" + A +
                ", B=" + B +
                ", C=" + C +
                ", D=" + D +
                ", E=" + E +
                ", fullMark=" + fullMark +
                '}';
    }
}