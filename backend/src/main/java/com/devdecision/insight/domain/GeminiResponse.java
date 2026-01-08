package com.devdecision.insight.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Map;

/**
 * Response structure for Gemini API calls.
 * Maps the JSON response from Gemini to Java objects.
 */
public class GeminiResponse {
    
    @JsonProperty("radarChartData")
    private List<RadarDataPoint> radarChartData;
    
    @JsonProperty("insights")
    private List<TechnologyInsight> insights;
    
    @JsonProperty("tradeOffs")
    private List<String> tradeOffs;
    
    @JsonProperty("recommendations")
    private String recommendations;
    
    @JsonProperty("decisionQuestions")
    private List<String> decisionQuestions;

    // Default constructor for Jackson
    public GeminiResponse() {}

    // Getters and setters
    public List<RadarDataPoint> getRadarChartData() {
        return radarChartData;
    }

    public void setRadarChartData(List<RadarDataPoint> radarChartData) {
        this.radarChartData = radarChartData;
    }

    public List<TechnologyInsight> getInsights() {
        return insights;
    }

    public void setInsights(List<TechnologyInsight> insights) {
        this.insights = insights;
    }

    public List<String> getTradeOffs() {
        return tradeOffs;
    }

    public void setTradeOffs(List<String> tradeOffs) {
        this.tradeOffs = tradeOffs;
    }

    public String getRecommendations() {
        return recommendations;
    }

    public void setRecommendations(String recommendations) {
        this.recommendations = recommendations;
    }

    public List<String> getDecisionQuestions() {
        return decisionQuestions;
    }

    public void setDecisionQuestions(List<String> decisionQuestions) {
        this.decisionQuestions = decisionQuestions;
    }
    
    /**
     * Nested class for radar chart data points from Gemini
     */
    public static class RadarDataPoint {
        @JsonProperty("subject")
        private String subject;
        
        @JsonProperty("A")
        private Double A;
        
        @JsonProperty("B")
        private Double B;
        
        @JsonProperty("C")
        private Double C;
        
        @JsonProperty("D")
        private Double D;
        
        @JsonProperty("E")
        private Double E;
        
        @JsonProperty("fullMark")
        private Double fullMark;

        // Default constructor
        public RadarDataPoint() {}

        // Getters and setters
        public String getSubject() {
            return subject;
        }

        public void setSubject(String subject) {
            this.subject = subject;
        }

        public Double getA() {
            return A;
        }

        public void setA(Double a) {
            A = a;
        }

        public Double getB() {
            return B;
        }

        public void setB(Double b) {
            B = b;
        }

        public Double getC() {
            return C;
        }

        public void setC(Double c) {
            C = c;
        }

        public Double getD() {
            return D;
        }

        public void setD(Double d) {
            D = d;
        }

        public Double getE() {
            return E;
        }

        public void setE(Double e) {
            E = e;
        }

        public Double getFullMark() {
            return fullMark;
        }

        public void setFullMark(Double fullMark) {
            this.fullMark = fullMark;
        }
    }
    
    /**
     * Nested class for technology insights from Gemini
     */
    public static class TechnologyInsight {
        @JsonProperty("technology")
        private String technology;
        
        @JsonProperty("strengths")
        private List<String> strengths;
        
        @JsonProperty("weaknesses")
        private List<String> weaknesses;
        
        @JsonProperty("recommendation")
        private String recommendation;
        
        @JsonProperty("useCase")
        private String useCase;

        // Default constructor
        public TechnologyInsight() {}

        // Getters and setters
        public String getTechnology() {
            return technology;
        }

        public void setTechnology(String technology) {
            this.technology = technology;
        }

        public List<String> getStrengths() {
            return strengths;
        }

        public void setStrengths(List<String> strengths) {
            this.strengths = strengths;
        }

        public List<String> getWeaknesses() {
            return weaknesses;
        }

        public void setWeaknesses(List<String> weaknesses) {
            this.weaknesses = weaknesses;
        }

        public String getRecommendation() {
            return recommendation;
        }

        public void setRecommendation(String recommendation) {
            this.recommendation = recommendation;
        }

        public String getUseCase() {
            return useCase;
        }

        public void setUseCase(String useCase) {
            this.useCase = useCase;
        }
    }
}