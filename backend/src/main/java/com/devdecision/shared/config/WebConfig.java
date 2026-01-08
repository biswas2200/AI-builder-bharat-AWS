package com.devdecision.shared.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.CommonsRequestLoggingFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:3000", "http://localhost:5173")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }

    /**
     * Configure request/response logging for monitoring and debugging.
     * Logs request details including URI, method, parameters, and payload.
     */
    @Bean
    public CommonsRequestLoggingFilter requestLoggingFilter() {
        CommonsRequestLoggingFilter loggingFilter = new CommonsRequestLoggingFilter();
        
        // Configure what to log
        loggingFilter.setIncludeClientInfo(true);
        loggingFilter.setIncludeQueryString(true);
        loggingFilter.setIncludePayload(true);
        loggingFilter.setIncludeHeaders(false); // Avoid logging sensitive headers
        
        // Set payload size limits
        loggingFilter.setMaxPayloadLength(1000); // Limit payload logging to 1KB
        
        // Configure log messages
        loggingFilter.setBeforeMessagePrefix("REQUEST: ");
        loggingFilter.setAfterMessagePrefix("RESPONSE: ");
        
        return loggingFilter;
    }
}