package com.devdecision.insight.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;

/**
 * Configuration for the Insight module.
 * Sets up beans and configuration needed for AI integration.
 */
@Configuration
@EnableRetry
public class InsightConfig {
    
    /**
     * ObjectMapper configured for Gemini API responses
     */
    @Bean("geminiObjectMapper")
    public ObjectMapper geminiObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setPropertyNamingStrategy(PropertyNamingStrategies.LOWER_CAMEL_CASE);
        return mapper;
    }
}